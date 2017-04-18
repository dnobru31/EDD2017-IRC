package com.cfranc.irc.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class SalonTest {

	@Test
	public void archiveTest() { // la taille max du tableau (tailleMaxHisto) est � 3 en dur (classe Salon)
		Salon actuel = new Salon("actuel", false);
		assertEquals (0, actuel.historique.size()); // on teste la taille du tableau a chaque ajout de message
		actuel.archive("1er message");
		assertEquals (1, actuel.historique.size());
		actuel.archive("2�me message");
		assertEquals (2, actuel.historique.size());
		actuel.archive("3�me message");
		assertEquals (3, actuel.historique.size());
		actuel.archive("4�me message");
		assertEquals (3, actuel.historique.size());
		
		// on teste qu'� l'ajout du 4�me message le 2�me message soit pass� de la position 1 � la position 0
		assertEquals("[2�me message, 3�me message, 4�me message]", actuel.historique.toString());

		
		
		//System.out.println(actuel.histoSalon);
	}

}
