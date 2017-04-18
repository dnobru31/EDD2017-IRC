package com.cfranc.irc.server;

import java.util.ArrayList;
import java.util.Scanner;


// Pour connaitre la liste des salons existants, il y en a toujours au moins un (le g�n�ral)
public class SalonLst  {

	protected ArrayList <Salon> lstSalons; // list des salons
	
	public SalonLst(){
		this.lstSalons = new ArrayList<Salon>();

		// Cr�ation du salon "G�n�ral"
		this.lstSalons.add(new Salon("G�n�ral",false));

	}

	public Salon get(int i) {
		
		return (Salon) lstSalons.get(i);		
		
	}
	
	public int taille() { return lstSalons.size();}
	public int getNumero(String unNomSalon) {
		int ret = 0;
		// retrouve le numero de salon a partir de son nom en faisant un parcours
		for (int i=0; i < lstSalons.size(); i++)  {
			if (lstSalons.get(i).getNomSalon().equals(unNomSalon) ) {
				ret = i;
				break;
			}
		}
		return ret;
	}



	// cr�ation d'un salon apr�s reception de l'objet (Salon)
	public boolean add(Salon newSalon)  {
		boolean ajoutOK = true;
		
		if(!lstSalons.contains(newSalon)){
			lstSalons.add(newSalon);
			System.out.println("Le salon " + newSalon.getNomSalon() + " a bien �t� cr��...");
			
		}else {
			System.out.println("Le salon " + newSalon.getNomSalon() + " existe d�j�, deux salons ne peuvent porter le m�me nom");
			ajoutOK= false;
			
		}
		return ajoutOK;
		
	}

	
	
	// cr�ation d'un salon apr�s r�ception du nom (String)
	public boolean add(String nomSalon)  {
						
		Salon newSalon = new Salon(nomSalon, false); 
				
		return add(newSalon);
		
		
	}
	
	
	public void remove() {
		// TODO
	}

	// Faire une fonction ajoutant le salon ou retournant faux si existe d�ja
	
	
}
