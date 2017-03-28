package com.cfranc.irc.server;


// Utilisateur, on gère déja sur quel salon il est, au depart il y a un seul salon
// le salon général

public class User {

	private String login;
	private String pwd;
	private int idSalon;
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	protected int getIdSalon() {
		return idSalon;
	}

	protected void setIdSalon(int idSalon) {
		this.idSalon = idSalon;
	}

	public User(String login, String pwd, int userSalon) {
		super();
		this.login = login;
		this.pwd = pwd;
		this.idSalon=userSalon;
	}	
	
}
