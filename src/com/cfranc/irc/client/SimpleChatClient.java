package com.cfranc.irc.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimpleChatClient {
	private Socket socket = null;
	private BufferedReader console = null;
	private DataOutputStream streamOut = null;
	
	// Creation du chat client

	public SimpleChatClient(String serverName, int serverPort) {
		// a) On se connecte au serveur
		System.out.println("Establishing connection. Please wait ...");
		try {
			socket = new Socket(serverName, serverPort);			
			System.out.println("Connected: " + socket);
			start();
		} catch (UnknownHostException uhe) {
			System.out.println("Host unknown: " + uhe.getMessage());
		} catch (IOException ioe) {
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
		//b) On attend les commandes au clavier, et on les envoie au serveur
		String line = "";
		while (!line.equals(".bye")) {
			try {
				line = console.readLine();
				streamOut.writeUTF(line);
				streamOut.flush();
			} catch (IOException ioe) {
				System.out.println("Sending error: " + ioe.getMessage());
			}
		}
	}
	
	// Demarrage du thread client,   lecture des entrées clavier et creation du flux du sortie
	public void start() throws IOException {
		console = new BufferedReader(new InputStreamReader(System.in));
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	// liberation des ressources ouvertes pour ce client
	// A APPRONFONDIR, libère t'on les ressources utilisées coté serveur par un client quand il se deconnecte
	public void stop() {
		try {
			if (console != null){console.close();}
			if (streamOut != null){streamOut.close();}
			if (socket != null){socket.close();}
		} catch (IOException ioe) {
			System.out.println("Error closing ...");
		}
	}
	
	// A lancer avec 2 arguments : nom du serveur et port pour 'handshake'
	public static void main(String args[]) {
		SimpleChatClient client = null;
		if (args.length != 2){
			System.out.println("Usage: java ChatClient host port");
		}
		else{
			client = new SimpleChatClient(args[0], Integer.parseInt(args[1]));
		}
	}
}
