package com.cfranc.irc.server;

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
	

	public int getIdSalon() {


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
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (this.getLogin() == null) {
			if (other.getLogin() != null)
				return false;
		} else if (!this.getLogin().equals(other.getLogin()))
			return false;
		return true;
	
	}
	
}
