package com.cfranc.irc.server;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SalonLstTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// test des m�thodes add(Salon) et add(String) sur le nombre d'entr�es de SalonLst
	@Test
	public void testAddNewSalonQuantite() {
		SalonLst actual = new SalonLst();
		actual.add(new Salon("salonA", false)); // ajout d'un salon � partir de la methode add(Salon)
		actual.add("salonB"); // ajout d'un salon � partir de la methode add(String)
		assertEquals(3, actual.lstSalons.size()); // test du nombre d'entr�es de la SalonLst 
												  // (3 salons pour prendre en compte le salon par d�faut cr�� par le new Salon)
		
		String premierSalon = actual.get(1).getNomSalon();
		String secondSalon = actual.get(2).getNomSalon();
		assertEquals(("salonA" + "salonB"), (premierSalon + secondSalon)); // test sur le nom des salons 1 et 2 (salon 0 par d�faut pas pris en compte)
		assertEquals(("salonA" + "salonB"), premierSalon + secondSalon);
		
		assertFalse(actual.add("salonB"));
		
	}



}
