package com.cfranc.irc.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.cfranc.irc.IfClientServerProtocol;


// Envoi de message du serveur vers le client
public class ServerToClientThread extends Thread{
	private User user;
	private Socket socket = null;
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;
	
	// Constructor en recevant en paramètre la socket gérant cette connection Serveur/client
	// et le user qui est géré par cette connection
	public ServerToClientThread(User user, Socket socket) {
		super();
		this.user=user;
		this.socket = socket;
	}
	
	// Buffer des messages a poster
	List<String> msgToPost=new ArrayList<String>();
	// Ajout d'un message dans le buffer
	public synchronized void post(String msg){
		msgToPost.add(msg);
	}
	
	// Envoi réel des messages du buffer et raz du buffer
	private synchronized void doPost(){
		try {
			for (String msg : msgToPost) {
					streamOut.writeUTF(msg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			msgToPost.clear();
		}
	}
	
	// On recupere le flux d'entree et de sortie liée a la socket de connection
	public void open() throws IOException {
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	// Fermeture du serveur, on ferme toutes les ressources ouvertes pour le serveur
	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
		if (streamOut != null)
			streamOut.close();
	}

	// Execution du thread
	@Override
	public void run() {
		try {
			open();  // ouvre flux d'entree sortie sur socket
			boolean done = false;
			while (!done) {    // Le serveur ne s'arrete pas depuis la console ??? CYRIL
				try {
					// .available permet de ne pas bloquer, on regarde juste s'il y a qqchose
					// Ca permet alors de faire le post s'il n'y a rien
					// (en faisant directement readUTF, on reste bloqué en attente de données)
					if(streamIn.available()>0){
						// Des données sont disponibles en entrée
						String line = streamIn.readUTF(); // on lit ces données
						
						// On tronconne le message en X élements (séparateur '#')
						// Si on a recu '#toto#bonjour'
						//  userMsg[1]='toto',  userMsg[2] = 'bonjour'
						String[] userMsg=line.split(IfClientServerProtocol.SEPARATOR);
						String login=userMsg[1];
						String msg=userMsg[2];
						done = msg.equals(".bye");  // Condition d'arret du client
						if(!done){
							if(login.equals(user)){
								// le login contenu dans le message est celui du user courant
								// On s'envoie un message !!!!
								// (user doit etre convertit implicitement en user.tostring() )
								// refact login  ==> loginSender
								//   et   user   ==> userCourant
								
								System.err.println("ServerToClientThread::run(), login!=user"+login);
							}
							// on publie a tous les user le message <msg> venant de <user>
							BroadcastThread.sendMessage(user,msg);
						}
					}
					else{
						// Rien n'est en entrée, on post tous les messages qui peuvent etre en attente
						doPost();
					}
				} 
				catch (IOException ioe) {
					done = true;
				}
			}
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
