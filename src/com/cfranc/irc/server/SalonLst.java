package com.cfranc.irc.server;

import java.util.ArrayList;


// Pour connaitre la liste des salons existants, il y en a toujours au moins un (le g�n�ral)
public class SalonLst  {

	private ArrayList <Salon> lstSalons; // list des salons
	
	public SalonLst(){
		this.lstSalons = new ArrayList<Salon>();

		// Cr�ation du salon "G�n�ral"
		this.lstSalons.add(new Salon("G�n�ral",false));

	}

	public Salon get(int i) {
		
		return (Salon) lstSalons.get(i);		
		
	}

	public boolean add(Salon unSalon)  {
		// Verifier le salon n'est pas dans lstSalons et l'ajouter alors
		// TODO
		return true;
		
	}
	public void remove() {
		// TODO
	}

	// Faire une fonction ajoutant le salon ou retournant faux si existe d�ja
	
	
}
