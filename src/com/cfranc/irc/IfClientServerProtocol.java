package com.cfranc.irc;

public interface IfClientServerProtocol {
	public static final String LOGIN_PWD = "#Login?#Pwd?";
	public static final String SEPARATOR="#";
	public static final String KO = "#KO";
	public static final String OK = "#OK";
	public static final String ADD = "ADD"; //"#+#";
	public static final String DEL = "DEL"; //"#-#";
	
	// A voir pour gérer les salons, # suivi d'un 2eme # signifierait salon
	public static final String AJ_SAL = "_AJ_SAL"; //##+#";
	public static final String REJOINT_SAL = "_REJOINT_SAL";//##>#";
	public static final String QUITTE_SAL = "QUITTE_SAL"; //"##<#";
}