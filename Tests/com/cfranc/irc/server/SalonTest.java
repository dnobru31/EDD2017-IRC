package com.cfranc.irc.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class SalonTest {
	
	int tailleMaxHisto = Salon.getTailleMaxHisto(); // on récupère la taille max de l'historique

	@Test
	public void archiveTest() { 
		Salon actuel = new Salon("actuel", false);
				
		
		// on rempli le tableau a hauteur de la tailleMaxHisto ATTENTION!! le 1er message est le "message n° 0"
		for (int i=0; i<tailleMaxHisto; i++) { 
			actuel.archive("message n° " + i); 
		}
		
		// on ajoute un message supplémentaire par rapport à la taille max
		actuel.archive("message n° " + (tailleMaxHisto)); 
		
		// on vérifie que la liste soit bien à la taille max
		assertEquals (tailleMaxHisto, actuel.historique.size()); 
		
		// on teste que le "message n°0 ait bien été remplacé par le n°1
		assertEquals ("message n° 1", actuel.historique.get(0).toString()); 
		
		// on teste que le dernier message ajouté soit bien en fin de liste
		assertEquals ("message n° " + (tailleMaxHisto), actuel.historique.get(tailleMaxHisto -1).toString());
		

		
		System.out.println(actuel.historique);
	}

}
