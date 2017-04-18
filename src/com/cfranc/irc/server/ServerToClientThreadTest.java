package com.cfranc.irc.server;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.client.ClientToServerThread;

public class ServerToClientThreadTest {

	@Test
	public void traiteMsgSpecifique() {
		String nomSalon="SalonA";
		ServerToClientThread unThread = new ServerToClientThread();
		User userAppelant = new User("toto","1234",0);
		String msg = IfClientServerProtocol.AJ_SAL + userAppelant.getLogin() + IfClientServerProtocol.SEPARATOR + nomSalon;
		unThread.traiteDonneesEnEntree(msg);
		
		assertEquals(nomSalon,BroadcastThread.listeDesSalons.get(1).getNomSalon());
		
	
	}
	
	@Test
	public void integrationClientServeurAjoutSalon() {
		// un message envoyé au serveur par un client 1
		// et retourne au serveur par un autre client
		// est correctement traité par le client 2
		String nomUser = "toto";
		String nomSalon = "SalonA";
		// a) On crée un thread client
		User unUser = new User(nomUser,"1234",0);
		ClientToServerThread Thread1= new ClientToServerThread(nomUser);
		
		// b: On recoit un message de l'ihm que clientToServer transforme
		String messageFromIHM = IfClientServerProtocol.AJ_SAL + nomSalon;		
		Thread1.setMsgToSend(messageFromIHM);
		String msgAuServeur = Thread1.msgToSend;
		System.out.println("msgAuServeur = " + msgAuServeur);
		// c: On envoie le message transformé au client qui va faire
		// les traitement spécifiques	
		// Le serveur demande a BroadCast de renvoyer a tous les clients
		// on verifie donc que broadcast ne deforme pas le message
		ServerToClientThread threadReception = new ServerToClientThread();
		// cyril, !!! oblige de le passer en public car autre package (idem pour fction ci dessous)
		//threadReception.traiteMsgSpecifique(unUser, msgAuServeur);
		threadReception.traiteDonneesEnEntree(msgAuServeur);
		assertEquals(nomSalon,BroadcastThread.listeDesSalons.get(1).getNomSalon());  // le salon est crée
		assertEquals(msgAuServeur,BroadcastThread.dernierMessageEnvoye);  // On a demandé a broadcasté le message a tous les user
		
	}
	
	@Test
	public void messageClassique() {
		// on envoie un message classique de l'interface au client
		// et elle est retourné au User
		
		
		
		// a) On crée un thread client
		String nomUser = "user1";
		User unUser = new User(nomUser,"1234",0);
		ClientToServerThread Thread1= new ClientToServerThread(nomUser);

		// b: On recoit un message de l'ihm que clientToServer transforme
		String messageFromIHM = "Bonjour";
		Thread1.setMsgToSend(messageFromIHM);
		String msgAuServeur = Thread1.msgToSend;
		assertEquals("#" + nomUser + "#" + messageFromIHM, msgAuServeur );
		
		// c: On envoie le message transformé au client qui va faire
		// les traitement spécifiques		
		ServerToClientThread threadReception = new ServerToClientThread();
		threadReception.userCourant = unUser;
		// cyril, !!! oblige de le passer en public car autre package (idem pour fction ci dessous)
		//threadReception.traiteMsgSpecifique(unUser, msgAuServeur);
		threadReception.traiteDonneesEnEntree(msgAuServeur);
		System.out.println("broadcast" + BroadcastThread.dernierMessageEnvoye);
		
		assertEquals("#" + nomUser + "#" + messageFromIHM, BroadcastThread.dernierMessageEnvoye);
		//assertEquals(nomUser, BroadcastThread.dernierUserEnvoye.getLogin());
	}


}
