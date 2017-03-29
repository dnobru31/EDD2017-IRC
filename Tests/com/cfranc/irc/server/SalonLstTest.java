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



	@Test
	public void testAddNewSalon() {
		SalonLst actual = new SalonLst(); 
	    actual.add(new Salon("salonA",false));
		actual.add("salonB");
		assertEquals(3, actual.lstSalons.size());
		
		
	}
	
	//@Test
	public void testAddNomSalon() {
		
		fail("Not yet implemented");
	}

}
