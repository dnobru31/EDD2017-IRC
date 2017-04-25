package com.cfranc.irc.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.ProtocoleIRC;

// Publication des messages a tous les clients
public class BroadcastThread extends Thread {

	// On conserve en static la liste des users avec le thread qui leur est
	// associé
	public static HashMap<User, ServerToClientThread> clientTreadsMap = new HashMap<User, ServerToClientThread>();
	static {
		// Instructions a faire au moment de l'init des variables static
		// On synchronise les différents thread !! Cyril
		Collections.synchronizedMap(clientTreadsMap);
	}
	
	public static SalonLst listeDesSalons=new SalonLst();
	
	public static String dernierMessageEnvoye=""; // pour Test
	private static ProtocoleIRC unMessageIRC = new ProtocoleIRC();

	// Ajout d'un client si existe pas déja
	// On recoit un user et le thread qui lui a été attribué par le serveur

	
	public static boolean addClient(User newUser, ServerToClientThread newServerToClientThread) {
		boolean res = true;
		if (clientTreadsMap.containsKey(newUser)) {
			res = false;
		} else {
			// a) On mémorise reellement le nouveau user et son thread
			avertirUserMouvement(newUser, IfClientServerProtocol.ADD);
			clientTreadsMap.put(newUser, newServerToClientThread);
			diffuserListeUsers(newServerToClientThread);
			// b) On envoie au nouvel User tous les salons déja crées
			emettreListeSalonsAuClient(newServerToClientThread);

			newUser.setIdSalon(0);// Initialisé sur salon général
									

			// c) on précise qu'il entre dans le salon général
			mouvementDansSalon(newUser, newServerToClientThread, 0, IfClientServerProtocol.AJ_SAL);

		}
		return res;

	}

	private static void avertirUserMouvement(User newUser, String typeMouvement) {
		User unUser;
		ServerToClientThread threadClient;

		for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			threadClient = entry.getValue();
			unUser = entry.getKey();

			threadClient.post(unMessageIRC.encode(newUser.getLogin(), typeMouvement, "", "", ""));

		}

	}

	private static void diffuserListeUsers(ServerToClientThread newServerToClientThread) {

		
		// On retourne au nouveau thread tous les users existants
		for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			newServerToClientThread
					.post(unMessageIRC.encode(entry.getKey().getLogin(), IfClientServerProtocol.ADD, "", "", ""));
		}
	}

	public static boolean mouvementDansSalon(User newUser, ServerToClientThread newServerToClientThread,
			int numeroSalon, String typeMouvement) {
		boolean res = true;
		System.out.println("BroadCast:mouvement dans salon");
		// Appelé pour rejoindre ou quitter un salon
		if (typeMouvement.equals(IfClientServerProtocol.REJOINT_SAL)) {
			// On memorise que ce User est dans ce salon
			newUser.setIdSalon(numeroSalon);
		}
		avertirMembresDuSalonArriveeOuDepartDe(newUser, typeMouvement);

		

		if (typeMouvement.equals(IfClientServerProtocol.REJOINT_SAL)) {
			retournerListeMembresDuSalonVers(newUser, newServerToClientThread);
			retournerArchiveDuSalonVers(newServerToClientThread, numeroSalon);
			// Et lui retourner les archives du salon dans lequel il entre
			
		}
		return res;
	}

	private static void retournerListeMembresDuSalonVers(User newUser, ServerToClientThread newServerToClientThread) {
		User unUser;
		// On demande au serveur de poster au nouvel user (thread recu en
		// paramétre)
		// la liste de tous les login presents dans le salon (y compris lui
		// meme)

		

		for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			// si entry.getvalue().user est sur le salon alors renvoyer a celui
			// qui entre.
			unUser = entry.getKey();
			if (unUser.getIdSalon() == newUser.getIdSalon()) {
				// si entry.getvalue().user est sur le salon alors renvoyer a
				// celui qui entre.
				System.out.println("BroadCast: retournerListeMembresDuSalon" + unUser.getLogin());
				String nomSalon = listeDesSalons.get(newUser.getIdSalon()).getNomSalon();
				newServerToClientThread.post(
						unMessageIRC.encode(unUser.getLogin(), IfClientServerProtocol.REJOINT_SAL, "", nomSalon, ""));
			}
		}
	}

	private static void retournerArchiveDuSalonVers(ServerToClientThread newServerToClientThread, int numeroSalon) {
		User unUser;
		// On demande au serveur de poster au nouvel user (thread recu en
		// paramétre)
		// la liste de tous les messages archivés pour le salon courant

		
		ArrayList<String> unHisto = listeDesSalons.get(numeroSalon).historique;
		for (int i = 0; i < unHisto.size(); i++) {
			newServerToClientThread.post(unHisto.get(i));
		}

	}

	private static void avertirMembresDuSalonArriveeOuDepartDe(User newUser, String typeMouvement) {
		User unUser;
		ServerToClientThread threadClient;
		// on demande au serveur de poster a tous les user (excepté le user
		// emetteur)
		// un message precisant si on rejoint ou quitte un salon
		for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			// si le client courant est sur le salon en paramètre alors
			// l'avertir que quelqu'un arrive
			
			threadClient = entry.getValue();
			unUser = entry.getKey();

			 if (unUser == newUser) continue;
			
			if (unUser.getIdSalon()== newUser.getIdSalon()) {

				String nomSalon = listeDesSalons.get(newUser.getIdSalon()).getNomSalon();
				threadClient.post(unMessageIRC.encode(newUser.getLogin(), typeMouvement, "", nomSalon, ""));
				System.out.println("Broadcast, arriveeOuDepartDansSalon " + typeMouvement + " "+ unUser.getLogin() + ": salon = " + unUser.getIdSalon() + "/" + newUser.getIdSalon() );
				
			}

		}
	}

	private static void emettreListeSalonsAuClient(ServerToClientThread newServerToClientThread) {
		for (int i = 0; i < listeDesSalons.taille(); i++) {
			newServerToClientThread.post(unMessageIRC.encode("Admin", IfClientServerProtocol.AJ_SAL, "",
					listeDesSalons.get(i).getNomSalon(), ""));
		}
	}

	public static void sendMessage(User sender, String msg) {
		String unMessage;
		Collection<ServerToClientThread> clientThreads = new ArrayList<ServerToClientThread>();// =clientTreadsMap.values();
																								// //
																								// tous
																								// les
																								// Threads
		ServerToClientThread unThread;
		for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			if (entry.getKey().getIdSalon() == sender.getIdSalon()) {
				clientThreads.add(entry.getValue());
			}
		}
		unMessage = msg; // unMessageIRC.encode(sender.getLogin(),"#DISCUTE#",msg,"","");
		if (clientTreadsMap.size() == 0) { // en mode test, on ne diffuse pas
											// les messages
			dernierMessageEnvoye = unMessage;
		} else if (clientThreads.size() > 0) {
			// Il y a au moins un thread a avertir donc on poste le message a
			// l'ensemble
			sendMessage(unMessage, clientThreads);
		}

		System.out.println("Broadcast sendMessage : " + unMessage);

		// Et on archive ce message
		Salon salonCourant = listeDesSalons.get(sender.getIdSalon());
		salonCourant.archive(msg);
	}

	
	public static void sendInstruction(User sender, String inst){
		Collection<ServerToClientThread> clientThreads=null;  
		
		
		ProtocoleIRC unMessageIRCRecu = new ProtocoleIRC();
		unMessageIRCRecu.decode(inst);
		if (unMessageIRCRecu.verbe.equals(IfClientServerProtocol.AJ_SAL) &&
				(unMessageIRCRecu.userPrivate.equals(".") == false) )  {
			// Cas particulier du salon privé, il ne faut diffuser la creation du salon que 
			//pour l'emetteur et le destinataire du salon privé
			
			// !!!  LIRE le HMAP et alimenter les 2 thread de l'emetteur et du private
			clientThreads = new ArrayList<ServerToClientThread>();
			for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
				System.out.println("BroadCast:aj_sal privé" + entry.getKey().getLogin());
				if (entry.getKey().getLogin().equals(unMessageIRCRecu.userEmetteur) ||
						entry.getKey().getLogin().equals(unMessageIRCRecu.userPrivate)	)
				{
					clientThreads.add(entry.getValue());
				}
			}
		} else
		{ 
			clientThreads = clientTreadsMap.values();// tous les Threads		
		}
		sendMessage(inst,clientThreads);

		System.out.println("Broadcast sendInstruction : " + inst);
	}

	// envoyer à chaque thread recus en parametre, le message en paramètre en précisant
	// l'expéditeur
	private static void sendMessage(String msg, Collection<ServerToClientThread> aEnvoyer) {
		dernierMessageEnvoye = msg;
			
		Iterator<ServerToClientThread> receiverClientThreadIterator = aEnvoyer.iterator();
		while (receiverClientThreadIterator.hasNext()) {
			ServerToClientThread clientThread = (ServerToClientThread) receiverClientThreadIterator.next();

	
			clientThread.post(msg);	
	

		}
	}

	public static boolean rmClient(User oldUser, ServerToClientThread newServerToClientThread) {
		boolean res = true;
		System.out.println("BroadCast : rmClient");
		if (!clientTreadsMap.containsKey(oldUser)) {
			res = false;
		} else {
			// a) On supprime reellement le user du hashmap
			clientTreadsMap.remove(oldUser);

			// b) On précise qu'il quitte le salon où il était présent
			mouvementDansSalon(oldUser, newServerToClientThread, oldUser.getIdSalon(),
					IfClientServerProtocol.QUITTE_SAL);

			// c) Et on précise a tous les users restant que ce user a quitté le
			// chat
			avertirUserMouvement(oldUser, IfClientServerProtocol.DEL);

			// Rien a retourner au client qui se deconnecte

		}
		return res;
	}

	
	
	
	// le user existe t'il déja ? (utilisé par le serveur pour accepter un client)
		public static boolean accept(User user){
		boolean res=true;
		if(clientTreadsMap.containsKey(user)){
			res= false;

		}
		return res;
	}
}
