package com.cfranc.irc.client;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.server.BroadcastThread;
import com.cfranc.irc.server.ServerToClientThread;
import com.cfranc.irc.server.User;

public class ClientToServerThreadTest {

	@Test
	public void traiterAjoutSalon() {
		String nomSalon = "SalonA";
		ClientToServerThread unThread= new ClientToServerThread();
		unThread.traiterAjoutSalon(IfClientServerProtocol.AJ_SAL + "toto" + IfClientServerProtocol.SEPARATOR + nomSalon);
		
		System.out.println(unThread.salonListModel.size());
		assertEquals(1, unThread.salonListModel.size());
		//System.out.println("retour " + unThread.salonListModel.get(0));
		assertEquals(unThread.salonListModel.get(0),nomSalon);
		
	}
	
	@Test
	public void setMsgToSend() {
		String nomUser = "toto";
		String nomSalon = "SalonA";
		String expected = IfClientServerProtocol.AJ_SAL + nomUser + IfClientServerProtocol.SEPARATOR + nomSalon;
		String actual ;
		ClientToServerThread unThread= new ClientToServerThread();
		unThread.login = nomUser;
		unThread.setMsgToSend(IfClientServerProtocol.AJ_SAL + nomSalon);
		actual = unThread.msgToSend;
		System.out.println(actual);
		assertEquals(expected, actual);	
		
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
		ClientToServerThread Thread1= new ClientToServerThread();
		Thread1.login = "user1";
		// b: On recoit un message de l'ihm que clientToServer transforme
		String messageFromIHM = IfClientServerProtocol.AJ_SAL + nomSalon;		
		Thread1.setMsgToSend(messageFromIHM);
		String msgAuServeur = Thread1.msgToSend;
		System.out.println("msgAuServeur = " + msgAuServeur);
		// c: On envoie le message transformé au client qui va faire
		// les traitement spécifiques		
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
		ClientToServerThread Thread1= new ClientToServerThread();
		Thread1.login = nomUser;
		// b: On recoit un message de l'ihm que clientToServer transforme
		String messageFromIHM = "Bonjour";
		Thread1.setMsgToSend(messageFromIHM);
		String msgAuServeur = Thread1.msgToSend;
		assertEquals("#" + nomUser + "#" + messageFromIHM, msgAuServeur );
		
		// c: On envoie le message transformé au client qui va faire
		// les traitement spécifiques		
		ServerToClientThread threadReception = new ServerToClientThread();
		// cyril, !!! oblige de le passer en public car autre package (idem pour fction ci dessous)
		//threadReception.traiteMsgSpecifique(unUser, msgAuServeur);
		threadReception.traiteDonneesEnEntree(msgAuServeur);
		System.out.println("broadcast" + BroadcastThread.dernierMessageEnvoye);
		assertEquals(messageFromIHM, BroadcastThread.dernierMessageEnvoye);
		//assertEquals(nomUser, BroadcastThread.dernierUserEnvoye.getLogin());
	}

}
