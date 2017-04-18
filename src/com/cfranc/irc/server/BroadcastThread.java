package com.cfranc.irc.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


import com.cfranc.irc.IfClientServerProtocol;

// Publication des messages a tous les clients
public class BroadcastThread extends Thread {
	

	
	// On conserve en static la liste des users avec le thread qui leur est associé	
	public static HashMap<User, ServerToClientThread> clientTreadsMap=new HashMap<User, ServerToClientThread>();
	static{
		// Instructions a faire au moment de l'init des variables static
		// On synchronise les différents thread !! Cyril
		Collections.synchronizedMap(clientTreadsMap);
	}
	
	public static SalonLst listeDesSalons=new SalonLst();
	
	public static String dernierMessageEnvoye=""; // pour Test
	//public static User dernierUserEnvoye = null;
	// Ajout d'un client si existe pas déja
	// On recoit un user et le thread qui lui a été attribué par le serveur
	
	//TODO  => addclientTosalon( en ajoutant un 3eme arguement salon a 0 par défaut)
	public static boolean addClient(User newUser, ServerToClientThread newServerToClientThread) {
		boolean res = true;
		if(clientTreadsMap.containsKey(newUser)  ){
			res=false;
		}
		else{
			newUser.setIdSalon(0);
			avertirMembreDuSalon(newUser);
			// b: On mémorise reellement le nouveau user et son thread
			clientTreadsMap.put(newUser, newServerToClientThread);
			//  on envoie aussi au nouveau client la liste des salons déja crées
			for (int i=0; i < listeDesSalons.taille();i++) {
				newServerToClientThread.post(IfClientServerProtocol.AJ_SAL + "Admin" + IfClientServerProtocol.SEPARATOR + listeDesSalons.get(i).getNomSalon());
			}			
			retournerListeMembresDuSalon(newUser, newServerToClientThread); 
			
		}
		return res ;
		
	}
	
	
	public static boolean clientEntreDansSalon(User newUser, ServerToClientThread newServerToClientThread,int numeroSalon){
		boolean res=true;
		
		newUser.setIdSalon(numeroSalon);
		avertirMembreDuSalon(newUser);
			
			// On mémorise reellement le nouveau user et son thread
						
		retournerListeMembresDuSalon(newUser, newServerToClientThread); 
		return res;
	}	



	private static void retournerListeMembresDuSalon(User newUser, ServerToClientThread newServerToClientThread) {
		User unUser;
		// c: On demande au serveur de poster au nouvel user (thread recu en paramétre)
		//   la liste de tous les login existants (y compris lui meme)
		for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			// si entry.getvalue().user est sur le salon alors renvoyer a celui qui entre.
			unUser= entry.getKey();
			if (unUser.getIdSalon()== newUser.getIdSalon()) {
				// si entry.getvalue().user est sur le salon alors renvoyer a celui qui entre.
				newServerToClientThread.post(IfClientServerProtocol.ADD+unUser.getLogin());  
			}
		}
	}


	private static void avertirMembreDuSalon(User newUser) {
		User unUser;
		ServerToClientThread threadClient;
		// a: on demande au serveur de poster a tous les user préexistants 
		//  un message ADD<login> du nouvel user
		for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			// si le client courant est sur le salon en paramètre alors
			// l'avertir que quelqu'un arrive
			// si le client courant est sur le salon en paramètre alors
			// l'avertir que quelqu'un arrive
			threadClient= entry.getValue();
			unUser = entry.getKey();
			if (unUser.getIdSalon()== newUser.getIdSalon()) {
				threadClient.post(IfClientServerProtocol.ADD+newUser.getLogin());
			}
			
		}
	}
	
//	// Un client entre dans le salon, il faut preciser que ce user est sur ce salon
//	// il faut avertir ceux qui sont déja dans le salon
//	// Et retourner a ce nouvel user la liste des clients qui sont sur ce salon
//	public static boolean addClientToSalon(User newUser, ServerToClientThread newServerToClientThread, int numeroSalon){
//		boolean res=true;
//		User unUser;
//		
//		ServerToClientThread threadClient;
////		if(clientTreadsMap.containsKey(newUser)){
////			res=false;
////		}
////		else{
//			//clientTreadsMap.put(user, serverToClientThread);	
//			// modifs du 03/11 : ajout de tous les users à la liste des users des clients
//			
//			// a: on demande au serveur de poster a tous les user connecté au salon
//			//  un message ADD<login> du nouvel user
//			for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
//				// si le client courant est sur le salon en paramètre alors
//				// l'avertir que quelqu'un arrive
//				threadClient= entry.getValue();
//				unUser = entry.getKey();
//				if (unUser.getIdSalon()== newUser.getIdSalon()) {
//					threadClient.post(IfClientServerProtocol.ADD+newUser.getLogin());
//				}
//			}
//			
//			// b: On mémorise reellement le nouveau user et son thread
//			// Todo ajouter a clientTreadsMap seulement si on est sur le salon 0.
//			newUser.setIdSalon(numeroSalon);
//			
//			
//			// c: On demande au serveur de poster au nouvel user (thread recu en paramétre)
//			//   la liste de tous les login existants (y compris lui meme)
//			for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
//				unUser= entry.getKey();
//				if (unUser.getIdSalon()== newUser.getIdSalon()) {
//					// si entry.getvalue().user est sur le salon alors renvoyer a celui qui entre.
//					newServerToClientThread.post(IfClientServerProtocol.ADD+unUser.getLogin());  
//				}
//			} 
//			
//			// 2eme partie, on envoie aussi au nouveau client la liste des salons déja crées
//			for (int i=0; i < listeDesSalons.taille();i++) {
//				newServerToClientThread.post(IfClientServerProtocol.AJ_SAL + "Admin" + IfClientServerProtocol.SEPARATOR + listeDesSalons.get(i).getNomSalon());
//				
//			}
//		//}
//		return res;
//	}
//	// Ajout de salon a batir sur le meme principe
//	public static boolean addSalon(Salon newSalon) {
//		for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
//			entry.getValue().post(IfClientServerProtocol.AJ_SAL+newSalon.getNomSalon());
//		}
//		return true;
//	}

	public static void sendMessage(User sender, String msg){
		
		Collection<ServerToClientThread> clientThreads=new ArrayList<ServerToClientThread>();//=clientTreadsMap.values();  // tous les Threads
		ServerToClientThread unThread;
		for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			if (entry.getKey().getIdSalon() == sender.getIdSalon())  {
				clientThreads.add(entry.getValue());
			}
		}
		
		sendMessage("#"+sender.getLogin()+"#"+msg, clientThreads);
		System.out.println("Broadcast sendMessage : "+"#"+sender.getLogin()+"#"+msg);
		
		
		
		// Et on archive ce message
		//Salon salonCourant;
		//salonCourant = listeDesSalons.get(sender.getIdSalon());
		//salonCourant.archive(msg);
	}
	
	public static void sendInstruction(User sender, String inst){
		Collection<ServerToClientThread> clientThreads=clientTreadsMap.values();  // tous les Threads
		sendMessage(inst,clientThreads);
		System.out.println("Broadcast sendInstruction : " + inst);
	}
	
	// envoyer à chaque thread existant, le message en paramètre en précisant l'expéditeur
	private static void sendMessage(String msg, Collection<ServerToClientThread> aEnvoyer){
		 dernierMessageEnvoye = msg;
		 //dernierUserEnvoye = sender;
		// clientTreads, est le tableau des thread (on a recupére que les values dans hashMap)
		//Collection<ServerToClientThread> clientTreads=clientTreadsMap.values();
		
		Iterator<ServerToClientThread> receiverClientThreadIterator=aEnvoyer.iterator();
		while (receiverClientThreadIterator.hasNext()) {
			ServerToClientThread clientThread = (ServerToClientThread) receiverClientThreadIterator.next();
	
			clientThread.post(msg);	
			
			// LstSalon.getId(sender.idSalon).archive( <meme message>)   
			// Attention a ne pas archiver 2 fois quand on rejoue un message
			// pour l'arrivée d'un client dans le salon
	
		}
	}
	
	public static void removeClient(User user){
		clientTreadsMap.remove(user);
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
