package com.cfranc.irc.server;

import java.util.ArrayList;

public class SalonLst  {

	private ArrayList <Salon> lstSalons; // list des salons
	
	public SalonLst(){
		this.lstSalons = new ArrayList<Salon>();

		// Création du salon "Général"
		this.lstSalons.add(new Salon("Général",false));

	}

	public Salon get(int i) {
		
		return (Salon) lstSalons.get(i);
		
		
	}


}
