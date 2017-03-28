package com.cfranc.irc.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import com.cfranc.irc.IfClientServerProtocol;


// Cote serveur, comment gérer la connection d'un nouveau client, et lui demander login mot de  passe
public class ClientConnectThread extends Thread implements IfClientServerProtocol {
	StyledDocument model=null;
	DefaultListModel<String> clientListModel;		
	
	private boolean canStop=false;
	private ServerSocket server = null;
	
	private void printMsg(String msg){
		try {
			if(model!=null){
				model.insertString(model.getLength(), msg+"\n", null);
			}
			System.out.println(msg);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// Le serveur demarre sur le port recu (correspondant a handshake)
	// On connait alors la socket qui permettra de dialoguer avec le serveur
	public ClientConnectThread(int port, StyledDocument model, DefaultListModel<String> clientListModel) {
		try {
			this.model=model;
			this.clientListModel=clientListModel;
			printMsg("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);
			printMsg("Server started: " + server);
		} 
		catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	@Override
	public void run() {
		while(!canStop){
			printMsg("Waiting for a client ...");
			Socket socket;
			try {
				socket = server.accept();  // On bloque en attendant un nouveau client
				printMsg("Client accepted: " + socket);
				
				// Accept new client or close the socket
				acceptClient(socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// Un client demande a utiliser le serveur
	// On a donc donné une socket de connection

	private void acceptClient(Socket socket) throws IOException, InterruptedException {
		// Read user login and pwd
		DataInputStream dis=new DataInputStream(socket.getInputStream());
		DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
		dos.writeUTF(LOGIN_PWD);
		
		// On boucle en attendant qu'il y ait qqchose en entrée (en patientant 100 unites entre chaque tour)
		while(dis.available()<=0){
			Thread.sleep(100);
		}
		String reponse=dis.readUTF();
		String[] userPwd=reponse.split(SEPARATOR);
		String login=userPwd[1];
		String pwd=userPwd[2];
		// On analyse le message recu et on connait le login et password en entrée
		
		int salonUser=0;  // pour l'instant figé sur le salon général
		
		
		User newUser=new User(login, pwd, salonUser);
		boolean isUserOK=authentication(newUser);
		if(isUserOK){
			// On peut donc créer un thread pour ce client			
			ServerToClientThread client=new ServerToClientThread(newUser, socket);
			dos.writeUTF(OK);  // Acquittement pour le client

			// Add user
			if(BroadcastThread.addClient(newUser, client)){
				client.start();	 // On lance le thread du client crée		
				clientListModel.addElement(newUser.getLogin());  // On l'ajoute dans la liste des user connectés
				// 2eme acquittement au client pour lui dire que tt les users connaissent sa présence
				dos.writeUTF(ADD+login); 
			}
		}
		else{
			// newUser n'est pas authentifié, on ferme proprement
			System.out.println("socket.close()");
			dos.writeUTF(KO);
			dos.close();
			socket.close();
		}
	}
	
	private boolean authentication(User newUser){
		// On accepte tous les user mais on pourra plus tard verifier mot de passe ou autre
		return BroadcastThread.accept(newUser);
	}

	
	public void open() throws IOException {
	}
	
	public void close() throws IOException {
		System.err.println("server:close()");
		if (server != null)
			server.close();
	}
}
