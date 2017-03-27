package com.cfranc.irc.server;

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
	
	
	
}
