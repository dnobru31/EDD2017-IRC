package com.cfranc.irc.server;

import java.util.ArrayList;
import java.util.List;


// Pour gérer les salons, a priori, le mode salon privé permettrait
// de gérer une communication privée entre deux utilisateur

// TODO: Garder l'ensemble des messages du salon pour pouvoir le rejouer a un client entrant dans le salon
public class Salon {
	private String nomSalon=null;
	private boolean bPrivate=false;
	private static int tailleMaxHisto = 20;
	
	private List<User> listeDesUsers;
	
	public ArrayList<String> historique = new ArrayList<String>();
	public String getNomSalon() {
		return nomSalon;
	}
	public void setNomSalon(String nomSalon) {
		this.nomSalon = nomSalon;
	}
	public boolean isbPrivate() {
		return bPrivate;
	}
	public void setbPrivate(boolean bPrivate) {
		this.bPrivate = bPrivate;
	}
	public Salon(String nomSalon, boolean bPrivate) {
		super();
		this.nomSalon = nomSalon;
		this.bPrivate = bPrivate;
		this.historique = new ArrayList<String>();
	}
	
	
	
	
	
	//User userCreator = null; need it ?
	

	// getter pour le tailleMaxHisto pour le rendre accessible au test
	public static int getTailleMaxHisto() {
		return tailleMaxHisto;
	}

	// si la liste est < à la taille max, rajouter newMess à la fin; sinon supprimer le 1er message de la liste et rajouter newMessage à la fin
	// la liste (ArrayList<String>) est créée par le constructeur de Salon
	public void archive (String mess)  {
		
		
		if (historique.size()< tailleMaxHisto) {
			historique.add(mess);
		}else{
			historique.remove(0);
			historique.add(mess);
		}
	}


	// redefinition de la methode equals pour que content() compare les valeurs et nom les références
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Salon other = (Salon) obj;
		if (nomSalon == null) {
			if (other.nomSalon != null)
				return false;
		} else if (!nomSalon.equals(other.nomSalon))
			return false;
		return true;
	}
	
	public boolean addUser(User userAAjouter) {
		boolean ajoutPossible = false;
		if (listeDesUsers.contains(userAAjouter)==false) {
			listeDesUsers.add(userAAjouter);
			ajoutPossible=true;
		} 
		
		return ajoutPossible;
	}
	
	// Rejoue les message a partir du <depuis> eme.
//	public StringArray rejoue( int depuis)  {
//		
//	}
	
	
	
}
