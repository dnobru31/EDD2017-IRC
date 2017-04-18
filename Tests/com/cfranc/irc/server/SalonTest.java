package com.cfranc.irc.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class SalonTest {

	@Test
	public void archiveTest() { // la taille max du tableau (tailleMaxHisto) est à 3 en dur (classe Salon)
		Salon actuel = new Salon("actuel", false);
		assertEquals (0, actuel.historique.size()); // on teste la taille du tableau a chaque ajout de message
		actuel.archive("1er message");
		assertEquals (1, actuel.historique.size());
		actuel.archive("2ème message");
		assertEquals (2, actuel.historique.size());
		actuel.archive("3ème message");
		assertEquals (3, actuel.historique.size());
		actuel.archive("4ème message");
		assertEquals (3, actuel.historique.size());
		
		// on teste qu'à l'ajout du 4ème message le 2ème message soit passé de la position 1 à la position 0
		assertEquals("[2ème message, 3ème message, 4ème message]", actuel.historique.toString());

		
		
		//System.out.println(actuel.histoSalon);
	}

}
