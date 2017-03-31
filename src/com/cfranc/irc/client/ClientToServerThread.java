package com.cfranc.irc.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.server.BroadcastThread;
import com.cfranc.irc.server.Salon;
import com.cfranc.irc.server.User;
import com.cfranc.irc.ui.SimpleChatClientApp;


// Envoi des messages du client vers le serveur
// A partir des donn�es contenue dans le mod�le

// Remplace SimpleChatClient a priori
public class ClientToServerThread extends Thread implements IfSenderModel{
	private Socket socket = null;
	private DataOutputStream streamOut = null;
	private DataInputStream streamIn = null;
	private BufferedReader console = null;
	String login,pwd;
	DefaultListModel<String> clientListModel;
	DefaultListModel<String> salonListModel;
	StyledDocument documentModel;
	
	User userLieAuThread;
	
	protected ClientToServerThread() {
		// constructor simple pour les test
		salonListModel = new DefaultListModel<String>();
		clientListModel = new DefaultListModel<String> ();
		documentModel = new DefaultStyledDocument();
	}
	
	public ClientToServerThread(StyledDocument documentModel, DefaultListModel<String> clientListModel, DefaultListModel<String> salonListModel,Socket socket, String login, String pwd) {
		super();
		this.documentModel=documentModel;
		this.clientListModel=clientListModel;
		this.salonListModel=salonListModel;
		this.socket = socket;
		this.login=login;
		this.pwd=pwd;
	}
	
	// Entr�e clavier , et flux d'entree /sortie pour la socket recue au constructor
	public void open() throws IOException {
		console = new BufferedReader(new InputStreamReader(System.in));
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	
	// Lib�ration de toutes les ressources du client
	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
		if (streamOut != null)
			streamOut.close();
	}
	
	// On recoit un message, quand pas pr�cis�, ce sera par defaut gris pour le text et italique pour le user
	// REFACTOR => MiseEnPageMessage
	public void receiveMessage(String user, String line){
		Style styleBI = ((StyledDocument)documentModel).getStyle(SimpleChatClientApp.BOLD_ITALIC);
        Style styleGP = ((StyledDocument)documentModel).getStyle(SimpleChatClientApp.GRAY_PLAIN);
        receiveMessage(user, line, styleBI, styleGP);
	}
	
	// On a recu un message comment l'afficher dans le modele (effet italic et gris)
	// REFACTOR => MiseEnPageMessage
	public void receiveMessage(String user, String line, Style styleBI,
			Style styleGP) {
		
		// TODO
		// si senderUser.idSalon != UserLi�AuThread.idSalon alors
		//   garder un buffer des donn�es, pour les ajouter au mod�le uniquement
		//   lorsqu'on reviendra sur le salon
		// A priori c'est plutot cot� serveur qu'il faut garder tout le dialogue d'un salon
		// Et pouvoir renvoyer chaque message quand le user vient ou revient sur un salon
		// Cot� client, il faudrait juste garder un ID, dernier message recu pour ce salon.
		// Serveur.ConnecteSalon(dernierID) rejouera alors tous les messages en attente depuis dernier ID
		
		//C'est donc le serveur qui n'envoie pas les messages au client s'il n'est pas 
		// sur le salon concern�
		
        try {      
        	// On ajoute en fin de documentModel, le SenderUser et son message
			documentModel.insertString(documentModel.getLength(), user+" : ", styleBI);
			documentModel.insertString(documentModel.getLength(), line+"\n", styleGP);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}				        	
	}
	
    // Protocole traitant l'ensemble des messages recus du serveur
	void readMsg() throws IOException{
		String line = streamIn.readUTF();
		System.out.println("ClientToserver " + line);
		
		if(line.startsWith(IfClientServerProtocol.ADD)){
			// Message recu commence par <ADD>, 
			String newUser=line.substring(IfClientServerProtocol.ADD.length());
			if(!clientListModel.contains(newUser)){
				//le user entre dans le salon s'il n'y �tait pas d�ja
				clientListModel.addElement(newUser);
				receiveMessage(newUser, " entre dans le salon...");
			}
		}
		else if(line.startsWith(IfClientServerProtocol.DEL)){
			// Message recu commence par <DEL>, 
			String delUser=line.substring(IfClientServerProtocol.DEL.length());
			if(clientListModel.contains(delUser)){
				// le user quitte le salon s'il y �tait
				clientListModel.removeElement(delUser);
				receiveMessage(delUser, " quite le salon !");
			}
		}
		else if(line.startsWith(IfClientServerProtocol.AJ_SAL)){
			traiterAjoutSalon(line);
		}
		else if(line.startsWith(IfClientServerProtocol.REJOINT_SAL)){
			traiterRejointSalon(line);
		}
		else if(line.startsWith(IfClientServerProtocol.QUITTE_SAL)){
			traiterQuitterSalon(line);
		}
		else{
			// A defaut, c'est un message 'chat' du type '#user#message'
			String[] userMsg=line.split(IfClientServerProtocol.SEPARATOR);
			String user=userMsg[1];
			receiveMessage(user, userMsg[2]);
		}
	}

	private void traiterQuitterSalon(String line) {
		// Message recu commence par <REJOINT_SAL>, un user rejoint un salon
		// Si c'est le salon de l'utilisateur courant alors on peut l'ajouter a la liste des users
		String reste = line.substring(IfClientServerProtocol.QUITTE_SAL.length());
		String[] quitteMsg=reste.split(IfClientServerProtocol.SEPARATOR);
		String nomUser=quitteMsg[0];
		String nomSalon = quitteMsg[1];	
		Salon salonCourant = BroadcastThread.listeDesSalons.get(userLieAuThread.getIdSalon());

		if (salonCourant.getNomSalon() == nomSalon) {
			if(clientListModel.contains(nomUser)){
				// ajout dans liste user si pas d�ja pr�sent
				clientListModel.removeElement(nomUser);
				receiveMessage(nomUser, nomUser + " quitte le salon " + nomSalon );
			}
		}
	}
	
	
	private void traiterRejointSalon(String line) {
		// Message recu commence par <REJOINT_SAL>, un user rejoint un salon
		// Si c'est le salon de l'utilisateur courant alors on peut l'ajouter a la liste des users
		String reste = line.substring(IfClientServerProtocol.REJOINT_SAL.length());
		String[] rejointMsg=reste.split(IfClientServerProtocol.SEPARATOR);
		String nomUser=rejointMsg[0];
		String nomSalon = rejointMsg[1];	
		Salon salonCourant = BroadcastThread.listeDesSalons.get(userLieAuThread.getIdSalon());

		if (salonCourant.getNomSalon() == nomSalon) {
			if(!clientListModel.contains(nomUser)){
				// ajout dans liste user si pas d�ja pr�sent
				clientListModel.addElement(nomUser);
				receiveMessage(nomUser, nomUser + " rejoint le salon " + nomSalon );
			}
		}
	}

	protected void traiterAjoutSalon(String line) {
		// Message recu commence par <AJSAL>, ajout de salon 
		String reste = line.substring(IfClientServerProtocol.AJ_SAL.length());
		String[] rejointMsg=reste.split(IfClientServerProtocol.SEPARATOR);
		String nomUser=rejointMsg[0];
		String nomSalon = rejointMsg[1];	
		
		receiveMessage(nomUser, "Le salon " + nomSalon + " a �t� cr�e!");
			// salonListModel  est NULL
		if(!salonListModel.contains(nomSalon)){
			// le user cree un salon si existe pas d�ja
			System.out.println("ClientToserver " +  "ajout");
			salonListModel.addElement(nomSalon);

		}
	}
	
	String msgToSend=null;
	
	/* (non-Javadoc)
	 * @see com.cfranc.irc.client.IfSenderModel#setMsgToSend(java.lang.String)
	 */
	@Override
	public void setMsgToSend(String _msgToSend) {
		// On recoit un message on le met dans le buffer a poster
		// (c'est une simple chaine, on n'aura pas plusieurs messages post�
		//  en attente de l'envoi)
		
		//si msgtosend commence par un verbe alors
		// envoyer verbe + login + message
		//sinon
		// envoyer login suivi de message
		System.out.println("setMsgtoSend de clienttoserver");
		if(_msgToSend.startsWith(IfClientServerProtocol.AJ_SAL)) {
			String resteMsg = _msgToSend.substring(IfClientServerProtocol.AJ_SAL.length());
			_msgToSend = IfClientServerProtocol.AJ_SAL + login + IfClientServerProtocol.SEPARATOR + resteMsg;
			System.out.println("ClientToserver " + "AJ__SAL recu" );
		}else if (_msgToSend.startsWith(IfClientServerProtocol.REJOINT_SAL)) {
			String resteMsg = _msgToSend.substring(IfClientServerProtocol.REJOINT_SAL.length());
			_msgToSend = IfClientServerProtocol.REJOINT_SAL + IfClientServerProtocol.SEPARATOR + resteMsg;
			System.out.println("ClientToserver " + "REJOINT_SAL recu" );
		}else
		{
		_msgToSend = "#"+login+"#"+_msgToSend;
		
		}
		this.msgToSend = _msgToSend;
		System.out.println("client to server" + this.msgToSend);
	}

	// Pousser le message en attente sur le flux de sortie
	private boolean sendMsg() throws IOException{
		boolean res=false;
		if(msgToSend!=null){
			streamOut.writeUTF(msgToSend) ; 
		    System.out.println("ClientToserver " + "on envoie au serveur le message" + msgToSend);
			msgToSend=null;
		    streamOut.flush();		    
		    res=true;

		}
		return res;
	}
	
	public void quitServer() throws IOException{
		// Pousser un message DEL sur le flux de sortie vers le serveur
		streamOut.writeUTF(IfClientServerProtocol.DEL+login);
		streamOut.flush();
		done=true;   // Pour quitter la boucle et lib�rer les ressources
	}
	
	boolean done;
	@Override
	public void run() {
		try {
			open();
			done = !authentification(); // On commence si authentification correcte
			while (!done) {
				try {
					// Si message en entr�e, alors les lire et les traiter
					if(streamIn.available()>0){
						readMsg();
					}
					
					// Puis on envoie le message s'il y en a un
					if(!sendMsg()){
						Thread.sleep(100); 
						// Si pas de message on patiente un peu avant de boucler pour pas tout saturer
					}
				} 
				catch (IOException | InterruptedException ioe) {
					ioe.printStackTrace();
					done = true;
				}
			}
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean authentification() {
		boolean res=false;
		String loginPwdQ;
		try {
			// On attend les donn�es en entr�e venant de l'authentification
			while(streamIn.available()<=0){
				Thread.sleep(100);
			}
			loginPwdQ = streamIn.readUTF(); // sous forme #login?#passwd
			if(loginPwdQ.equals(IfClientServerProtocol.LOGIN_PWD)){
				// On envoie au serveur ce login mot de passe (pour qu'il accepte ou non sa connection)
				streamOut.writeUTF(IfClientServerProtocol.SEPARATOR+this.login+IfClientServerProtocol.SEPARATOR+this.pwd);
			}
			
			// On attend maintenant l'accus� reception du serveur
			while(streamIn.available()<=0){
				Thread.sleep(100);
			}
			String acq=streamIn.readUTF();
			if(acq.equals(IfClientServerProtocol.OK)){
				// Le serveur a accept� l'acquittement sera positif (return true)
				res=true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;		
	}
	
}

