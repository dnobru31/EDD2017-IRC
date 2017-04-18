package com.cfranc.irc;

public class ProtocoleIRC implements IfClientServerProtocol {
	public String userEmetteur;
	public String verbe;
	public String commentaire;
	public String salonCree;
	public String userPrivate;
	
	public String encode(String _emetteur, String _verbe, String _commentaire, String _salon,String _userPrivate) {
		String ret="";
		String separ = IfClientServerProtocol.SEPARATOR;
		if (_commentaire.equals("")) {_commentaire=".";}
		if (_salon.equals("")) {_salon=".";}
		if (_userPrivate.equals("")) {_userPrivate=".";}
		ret = _emetteur +  separ + _verbe +  separ + _commentaire + separ + _salon + separ  + _userPrivate;
		System.out.println("encode:" + ret);
		return ret;
	}
	
	public void decode(String _mess)  {
		System.out.println("decode:" + _mess);
		String messageSplit[] = _mess.split(this.SEPARATOR);
		this.userEmetteur = messageSplit[0];
		this.verbe = messageSplit[1];
		this.commentaire=messageSplit[2];
		this.salonCree = messageSplit[3];
		this.userPrivate = messageSplit[4];
		
	}

}
