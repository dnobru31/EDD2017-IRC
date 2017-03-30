package com.cfranc.irc.server;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cfranc.irc.IfClientServerProtocol;

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

}
