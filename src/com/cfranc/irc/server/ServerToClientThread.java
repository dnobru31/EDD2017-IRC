package com.cfranc.irc.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.cfranc.irc.IfClientServerProtocol;


// Envoi de message d'UN client vers le serveur
public class ServerToClientThread extends Thread{
	private User userCourant;
	private Socket socket = null;
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;
	
	public ServerToClientThread() {
		
	}
	
	// Constructor en recevant en paramètre la socket gérant cette connection Serveur/client
	// et le user qui est géré par cette connection
	public ServerToClientThread(User user, Socket socket) {
		super();
		this.userCourant=user;
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
						
						traiteDonneesEnEntree(line);
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

	public boolean traiteDonneesEnEntree(String line) {
		// Si le message est un message de commande (ajout salon par exemple)
		// On traite ce message specifique en retournant faux si non accepté
		boolean done = false;
		System.out.println("ServerToClient traite donnes par le serveur");
		if (line.startsWith("##")) {
			traiteMsgSpecifique(userCourant,line);
		} else
		{
			done = traiteMessageClassique(line);
		}
		return done;
	}
	public  boolean traiteMessageClassique(String line) {
		
		boolean done;
		// On tronconne le message en X élements (séparateur '#')
		// Si on a recu '#toto#bonjour'
		//  userMsg[1]='toto',  userMsg[2] = 'bonjour'
		
		// Si message commence par ## on a des lignes de commande du serveur vers le client
		// (genre ajoute salon), on fait alors un traitement spécifique
		String reste = line.substring(IfClientServerProtocol.ADD.length());
		String[] userMsg=line.split(IfClientServerProtocol.SEPARATOR);
		String loginSender=userMsg[1];
		String msg=userMsg[2];
		System.out.println("ServerToClient traite donnees en entree" + line);
		System.out.println(loginSender + "/" + msg);
		done = msg.equals(".bye");  // Condition d'arret du client
		if(!done){
			if(loginSender.equals(userCourant)){
				// le login contenu dans le message est celui du user courant
				// On s'envoie un message !!!!
				// (user doit etre convertit implicitement en user.tostring() )
				
				
				System.err.println("ServerToClientThread::run(), login!=user"+loginSender);
			}
			
	
				// on publie a tous les user le message <msg> venant de <user>
				// qu'il ait ou non un traitement spécifique
				// On appele les methodes sur la classe, car c'est en statique
				BroadcastThread.sendMessage(userCourant,msg);
	
		}
		return done;
	}
	
	public void traiteMsgSpecifique(User userAppelant, String msg) {
		boolean messageRetenu = true;
		System.out.println("ServerToClient traite msg specifique" + msg);
		if(msg.startsWith(IfClientServerProtocol.AJ_SAL)) {messageRetenu = traiteAJ_SAL(userAppelant,msg);}
		if(msg.startsWith(IfClientServerProtocol.REJOINT_SAL)){	messageRetenu = traite_REJOINT_SAL(userAppelant, msg);}

		if (messageRetenu) {
			// on le broadcast tel quel a tous les user
			BroadcastThread.sendMessage(userCourant,msg);
		}
		
	}

	private boolean traite_REJOINT_SAL(User userAppelant, String msg ) {
		boolean messageRetenu=true;
		String nomSalonRejoint;
		Salon salonAJoindre;
		int numeroSalonAJoindre;
		String msgQuit;
			// Un user rejoint le salon
			String reste =msg.substring(IfClientServerProtocol.REJOINT_SAL.length());
			String[] rejointMsg=reste.split(IfClientServerProtocol.SEPARATOR);
			String nomUser=rejointMsg[0];
			String nomSalon = rejointMsg[1];
			// On regarde dans le salon, si le user n'y est pas déja
			numeroSalonAJoindre = BroadcastThread.listeDesSalons.getNumero(nomSalon);
			salonAJoindre = BroadcastThread.listeDesSalons.get(numeroSalonAJoindre);
			if (salonAJoindre.addUser(userAppelant)==false) {
				post("KO"); // le user est déja dans le salon
				messageRetenu = false;
			} else
			{
				userQuitteSalon(userAppelant);				
				// Puis mémoriser au niveau du User sur quel salon il est
				userAppelant.setIdSalon(numeroSalonAJoindre);
			};	
		
		return messageRetenu;
	}

	private void userQuitteSalon(User unUser) {
		String msgQuit;
		// Avertir tout le monde que User quitte son ancien salon
		Salon quitteLeSalon = BroadcastThread.listeDesSalons.get(unUser.getIdSalon());
		msgQuit = IfClientServerProtocol.QUITTE_SAL + unUser.getLogin() + IfClientServerProtocol.SEPARATOR + quitteLeSalon.getNomSalon();
		BroadcastThread.sendMessage(userCourant,msgQuit);
	}

	private boolean traiteAJ_SAL(User userAppelant,String msg) {
		String newSalon;
		 boolean messageRetenu=true;

			// Ajout de salon
		 	System.out.println("ServerToClient traite AJ_SAL");
			String reste =msg.substring(IfClientServerProtocol.AJ_SAL.length());
			String[] quitteMsg=reste.split(IfClientServerProtocol.SEPARATOR);
			String nomUser=quitteMsg[0];
			String nomSalon = quitteMsg[1];
			System.out.println("ServerToClient " + nomUser + " ajoute " + nomSalon);
			if (!BroadcastThread.listeDesSalons.add(nomSalon)) {
				//  Si impossible retourner KO au client
				post("KO");
				messageRetenu = false; // on ne le diffusera pas a tout le monde
			} else
			{
				// Si ok on retournera la creation de salon a tout le monde;
			};
		
		return messageRetenu;
	}
	
}
