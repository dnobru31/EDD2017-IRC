package com.cfranc.irc.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.ProtocoleIRC;


// Envoi de message d'UN client vers le serveur
public class ServerToClientThread extends Thread{
	public User userCourant;
	private Socket socket = null;
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;
	
	private ProtocoleIRC dernierMessageIRC = new ProtocoleIRC();
	
	DefaultListModel<String> clientListModel;
	
	protected ServerToClientThread() {
		
	}
	
	// Constructor en recevant en paramètre la socket gérant cette connection Serveur/client
	// et le user qui est géré par cette connection
	public ServerToClientThread(User user, Socket socket, DefaultListModel<String> _clientListModel) {
		super();
		this.userCourant=user;
		this.socket = socket;
		this.clientListModel = _clientListModel;
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

	protected boolean traiteDonneesEnEntree(String line) {
		// Si le message est un message de commande (ajout salon par exemple)
		// On traite ce message specifique en retournant faux si non accepté
		boolean done = false;
		System.out.println("ServerToClient traite donnes par le serveur" + line);
		dernierMessageIRC.decode(line);
		System.out.println("Verbe recu" + dernierMessageIRC.verbe);
		if (dernierMessageIRC.verbe.startsWith("_")) {
			traiteMsgSpecifique(userCourant,line);
		} else
		{
			if (dernierMessageIRC.verbe.equals(IfClientServerProtocol.DEL)) {
				// On recoit d'un client le fait qu'il se deconnecte
				// Le user quitte le chat, il faut avertir tous les clients.
				System.out.println("le user " + userCourant.getLogin() + " quitte le chat");
				BroadcastThread.rmClient(userCourant, this);  // US9: avertir les autres clients


				if(clientListModel.contains(userCourant.getLogin())){
					//On enleve le user de la liste des users sur le serveur
					clientListModel.removeElement(userCourant.getLogin());
					System.out.println("apres remove" + clientListModel.size());
				}  
			} else {
				done = traiteMessageClassique(line);}
		}
		return done;
	}
	protected  boolean traiteMessageClassique(String line) {
		
		boolean done;

		String loginSender = dernierMessageIRC.userEmetteur;
		String commentaire = dernierMessageIRC.commentaire;
		System.out.println("ServerToClient traite donnees en entree" + line);

		done = commentaire.equals(".bye");  // Condition d'arret du client
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

				BroadcastThread.sendMessage(userCourant,line);
	
	
		} 
		
		
		return done;
	}
	
	protected void traiteMsgSpecifique(User userAppelant, String msg) {
		boolean messageRetenu = true;
		String msgQuit;
		int ancienNoSalon;
		
		System.out.println("ServerToClient traite msg specifique" + msg);
		
		
		if(dernierMessageIRC.verbe.equals(IfClientServerProtocol.AJ_SAL)) {messageRetenu = traiteAJ_SAL(userAppelant,msg);}
		if(dernierMessageIRC.verbe.equals(IfClientServerProtocol.REJOINT_SAL))
		{	
			ancienNoSalon = userAppelant.getIdSalon();
			if (ancienNoSalon == -1) 
			{
				// premiere entree dans un salon général a la création d'un user
			}
			else
			{
				// On quitte le salon existe, et on precise qu'on va dans un nouveau salon
				String nomSalon = BroadcastThread.listeDesSalons.get(ancienNoSalon).getNomSalon();
				String userPourSalonPrive = "";
				msgQuit = dernierMessageIRC.encode(userAppelant.getLogin(), IfClientServerProtocol.QUITTE_SAL, "", nomSalon, userPourSalonPrive);
				traiteMouvementDansSalon(userAppelant, msgQuit,IfClientServerProtocol.QUITTE_SAL);
				System.out.println("on envoie quitte en boucle");
			}
			messageRetenu = traiteMouvementDansSalon(userAppelant, msg,IfClientServerProtocol.REJOINT_SAL);
		}


		if (messageRetenu) {
			// on le broadcast tel quel a tous les user
			BroadcastThread.sendInstruction(userCourant,msg);
		}
		
	}

	private boolean traiteMouvementDansSalon(User userAppelant, String msg, String typeMessage ) {
		boolean messageRetenu=true;		
		Salon salonAJoindre;
		int numeroSalonAJoindre;		
		ServerToClientThread unThread;
			// Un user rejoint le salon
			dernierMessageIRC.decode(msg);
			String nomUser=dernierMessageIRC.userEmetteur;
			String nomSalon = dernierMessageIRC.salonCree;
			System.out.println("ServerToClient traite mouvement dans salon" + msg + "::" + typeMessage);
	
			// On regarde dans le salon, si le user n'y est pas déja
			numeroSalonAJoindre = BroadcastThread.listeDesSalons.getNumero(nomSalon);
			salonAJoindre = BroadcastThread.listeDesSalons.get(numeroSalonAJoindre);
	
			// A verifier que si REJOINT_SAL
//			if (salonAJoindre.addUser(userAppelant)==false) {
//				post("KO"); // le user est déja dans le salon
//				messageRetenu = false;
//			} else
//			{
				
				System.out.println("broadcaster " + typeMessage + " dans salon");

				// Il faut avertir tous les users de monSalon  que <user> a rejoint monSalon
				unThread = BroadcastThread.clientTreadsMap.get(userAppelant);
				BroadcastThread.mouvementDansSalon(userAppelant, unThread, numeroSalonAJoindre,typeMessage);
				
//			};	
		
		return messageRetenu;
	}



	private boolean traiteAJ_SAL(User userAppelant,String msg) {
		
		 boolean messageRetenu=true;

			// Ajout de salon
		 	System.out.println("ServerToClient traite AJ_SAL");

			String nomUser=dernierMessageIRC.userEmetteur;
			String nomSalon = dernierMessageIRC.salonCree;
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
