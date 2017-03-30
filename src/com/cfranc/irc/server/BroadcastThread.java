package com.cfranc.irc.server;

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
	public static boolean addClient(User newUser, ServerToClientThread newServerToClientThread){
		boolean res=true;
		if(clientTreadsMap.containsKey(newUser)){
			res=false;
		}
		else{
			//clientTreadsMap.put(user, serverToClientThread);	
			// modifs du 03/11 : ajout de tous les users à la liste des users des clients
			
			// a: on demande au serveur de poster a tous les user préexistants 
			//  un message ADD<login> du nouvel user
			for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
				// si le client courant est sur le salon en paramètre alors
				// l'avertir que quelqu'un arrive
				entry.getValue().post(IfClientServerProtocol.ADD+newUser.getLogin());
			}
			
			// b: On mémorise reellement le nouveau user et son thread
			// Todo ajouter a clientTreadsMap seulement si on est sur le salon 0.
			clientTreadsMap.put(newUser, newServerToClientThread);
			
			// c: On demande au serveur de poster au nouvel user (thread recu en paramétre)
			//   la liste de tous les login existants (y compris lui meme)
			for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
				// si entry.getvalue().user est sur le salon alors renvoyer a celui qui entre.
				newServerToClientThread.post(IfClientServerProtocol.ADD+entry.getKey().getLogin());   
			} 
		}
		return res;
	}
	
	// Ajout de salon a batir sur le meme principe
	public static boolean addSalon(Salon newSalon) {
		for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			entry.getValue().post(IfClientServerProtocol.AJ_SAL+newSalon.getNomSalon());
		}
		return true;
	}

	// envoyer à chaque thread existant, le message en paramètre en précisant l'expéditeur
	public static void sendMessage(User sender, String msg){
		 dernierMessageEnvoye = msg;
		 //dernierUserEnvoye = sender;
		// clientTreads, est le tableau des thread (on a recupére que les values dans hashMap)
		Collection<ServerToClientThread> clientTreads=clientTreadsMap.values();
		
		Iterator<ServerToClientThread> receiverClientThreadIterator=clientTreads.iterator();
		while (receiverClientThreadIterator.hasNext()) {
			ServerToClientThread clientThread = (ServerToClientThread) receiverClientThreadIterator.next();
			
			
			//   IMPLEMENTATION SALON
			
			// Si sender.idSalon <> clientThread.user.idSalon alors
			//     ne pas envoyer.
			
			//Dans tous les cas garder en buffer le message envoyé avec un ID (propre au salon)
			// Pour pouvoir rejouer les messages quand le client se (re)connectera au salon
			
			// Finallement chaque connection ou reconnection sur un salon, on rejoue tous
			// les messages
			
			clientThread.post("#"+sender.getLogin()+"#"+msg);	
			
			// LstSalon.getId(sender.idSalon).archive( <meme message>)   
			// Attention a ne pas archiver 2 fois quand on rejoue un message
			// pour l'arrivée d'un client dans le salon
			System.out.println("sendMessage : "+"#"+sender.getLogin()+"#"+msg);
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
