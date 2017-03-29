package com.cfranc.irc.server;


// Pour gérer les salons, a priori, le mode salon privé permettrait
// de gérer une communication privée entre deux utilisateur

// TODO: Garder l'ensemble des messages du salon pour pouvoir le rejouer a un client entrant dans le salon
public class Salon {
	private String nomSalon=null;
	private boolean bPrivate=false;
	
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
	}
	
	//User userCreator = null; need it ?
	
	public void archive (String mess)  {
		
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
	
	// Rejoue les message a partir du <depuis> eme.
//	public StringArray rejoue( int depuis)  {
//		
//	}
	
	
	
}
