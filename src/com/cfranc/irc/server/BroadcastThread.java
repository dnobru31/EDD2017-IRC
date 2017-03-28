package com.cfranc.irc.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.cfranc.irc.IfClientServerProtocol;

// Publication des messages a tous les clients
public class BroadcastThread extends Thread {
	

	
	// On conserve en static la liste des users avec le thread qui leur est associ�	
	public static HashMap<User, ServerToClientThread> clientTreadsMap=new HashMap<User, ServerToClientThread>();
	static{
		// Instructions a faire au moment de l'init des variables static
		// On synchronise les diff�rents thread !! Cyril
		Collections.synchronizedMap(clientTreadsMap);
	}
	
	// Ajout d'un client si existe pas d�ja
	// On recoit un user et le thread qui lui a �t� attribu� par le serveur
	public static boolean addClient(User newUser, ServerToClientThread newServerToClientThread){
		boolean res=true;
		if(clientTreadsMap.containsKey(newUser)){
			res=false;
		}
		else{
			//clientTreadsMap.put(user, serverToClientThread);	
			// modifs du 03/11 : ajout de tous les users � la liste des users des clients
			
			// a: on demande au serveur de poster a tous les user pr�existants 
			//  un message ADD<login> du nouvel user
			for(Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
				entry.getValue().post(IfClientServerProtocol.ADD+newUser.getLogin());
			}
			
			// b: On m�morise reellement le nouveau user et son thread
			clientTreadsMap.put(newUser, newServerToClientThread);
			
			// c: On demande au serveur de poster au nouvel user (thread recu en param�tre)
			//   la liste de tous les login existants (y compris lui meme)
			for (Entry<User, ServerToClientThread> entry : clientTreadsMap.entrySet()) {
			newServerToClientThread.post(IfClientServerProtocol.ADD+entry.getKey().getLogin());   
			} 
		}
		return res;
	}
	
	// Ajout de salon a batir sur le meme principe
	// En ayant enrichi IfClientServerProtocol  pour avoir le mot 'SAL' par exemple
	public static boolean addSalon() {
		return true;
	}

	// envoyer � chaque thread existant, le message en param�tre en pr�cisant l'exp�diteur
	public static void sendMessage(User sender, String msg){
		
		// clientTreads, est le tableau des thread (on a recup�re que les values dans hashMap)
		Collection<ServerToClientThread> clientTreads=clientTreadsMap.values();
		
		Iterator<ServerToClientThread> receiverClientThreadIterator=clientTreads.iterator();
		while (receiverClientThreadIterator.hasNext()) {
			ServerToClientThread clientThread = (ServerToClientThread) receiverClientThreadIterator.next();
			
			
			//   IMPLEMENTATION SALON
			
			// Si sender.idSalon <> clientThread.user.idSalon alors
			//     ne pas envoyer.
			
			//Dans tous les cas garder en buffer le message envoy� avec un ID (propre au salon)
			// Pour pouvoir rejouer les messages quand le client se (re)connectera au salon
			
			// Finallement chaque connection ou reconnection sur un salon, on rejoue tous
			// les messages
			
			clientThread.post("#"+sender.getLogin()+"#"+msg);	
			
			// LstSalon.getId(sender.idSalon).archive( <meme message>)   
			// Attention a ne pas archiver 2 fois quand on rejoue un message
			// pour l'arriv�e d'un client dans le salon
			System.out.println("sendMessage : "+"#"+sender.getLogin()+"#"+msg);
		}
	}
	
	public static void removeClient(User user){
		clientTreadsMap.remove(user);
	}
	
	
	// le user existe t'il d�ja ? (utilis� par le serveur pour accepter un client)
	// Enrichir ici l'authentification si necessaire
	public static boolean accept(User user){
		boolean res=true;
		if(clientTreadsMap.containsKey(user)){
			res= false;
		}
		return res;
	}
}
