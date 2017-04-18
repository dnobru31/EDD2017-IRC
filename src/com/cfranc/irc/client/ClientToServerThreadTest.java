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
		String nomUser = "toto";
		ClientToServerThread unThread= new ClientToServerThread(nomUser);
		unThread.traiterAjoutSalon(IfClientServerProtocol.AJ_SAL + nomUser + IfClientServerProtocol.SEPARATOR + nomSalon);
		
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
		ClientToServerThread unThread= new ClientToServerThread(nomUser);
		unThread.login = nomUser;
		unThread.setMsgToSend(IfClientServerProtocol.AJ_SAL + nomSalon);
		actual = unThread.msgToSend;
		System.out.println(actual);
		assertEquals(expected, actual);	
		
	}
	
	
}
