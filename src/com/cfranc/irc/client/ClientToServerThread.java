package com.cfranc.irc.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.ProtocoleIRC;
import com.cfranc.irc.server.BroadcastThread;
import com.cfranc.irc.server.Salon;
import com.cfranc.irc.server.User;
import com.cfranc.irc.ui.SimpleChatClientApp;

// Envoi des messages du client vers le serveur
// A partir des données contenue dans le modèle

// Remplace SimpleChatClient a priori
public class ClientToServerThread extends Thread implements IfSenderModel {
	private Socket socket = null;
	private DataOutputStream streamOut = null;
	private DataInputStream streamIn = null;
	private BufferedReader console = null;
	String login, pwd;
	DefaultListModel<String> clientListModel;
	DefaultListModel<String> salonListModel;
	StyledDocument documentModel;

	public String msgToSend = null;
	private ProtocoleIRC unMessageIRC = new ProtocoleIRC();

	User userLieAuThread;

	public ClientToServerThread(String _login) {
		// constructor simple pour les test
		salonListModel = new DefaultListModel<String>();
		clientListModel = new DefaultListModel<String>();
		documentModel = new DefaultStyledDocument();
		this.login = _login;
	}

	public ClientToServerThread(StyledDocument documentModel, DefaultListModel<String> clientListModel,
			DefaultListModel<String> salonListModel, Socket socket, String login, String pwd) {
		super();
		this.documentModel = documentModel;
		this.clientListModel = clientListModel;
		this.salonListModel = salonListModel;
		this.socket = socket;
		this.login = login;
		this.pwd = pwd;
	}

	// Entrée clavier , et flux d'entree /sortie pour la socket recue au
	// constructor
	public void open() throws IOException {
		console = new BufferedReader(new InputStreamReader(System.in));
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(socket.getOutputStream());
	}

	// Libération de toutes les ressources du client
	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
		if (streamOut != null)
			streamOut.close();
	}

	// On recoit un message, quand pas précisé, ce sera par defaut gris pour le
	// text et italique pour le user
	// REFACTOR => MiseEnPageMessage
	public void receiveMessage(String user, String line) {
		if (user.equals("Admin")) return;
		Style styleBI = ((StyledDocument) documentModel).getStyle(SimpleChatClientApp.BOLD_ITALIC);
		Style styleGP = ((StyledDocument) documentModel).getStyle(SimpleChatClientApp.GRAY_PLAIN);

		receiveMessage(user, line, styleBI, styleGP);
	}

	// On a recu un message comment l'afficher dans le modele (effet italic et
	// gris)
	// REFACTOR => MiseEnPageMessage
	public void receiveMessage(String user, String line, Style styleBI, Style styleGP) {

		// TODO
		// si senderUser.idSalon != UserLiéAuThread.idSalon alors
		// garder un buffer des données, pour les ajouter au modèle uniquement
		// lorsqu'on reviendra sur le salon
		// A priori c'est plutot coté serveur qu'il faut garder tout le dialogue
		// d'un salon
		// Et pouvoir renvoyer chaque message quand le user vient ou revient sur
		// un salon
		// Coté client, il faudrait juste garder un ID, dernier message recu
		// pour ce salon.
		// Serveur.ConnecteSalon(dernierID) rejouera alors tous les messages en
		// attente depuis dernier ID

		// C'est donc le serveur qui n'envoie pas les messages au client s'il
		// n'est pas
		// sur le salon concerné

		try {
			// On ajoute en fin de documentModel, le SenderUser et son message
			documentModel.insertString(documentModel.getLength(), user + " : ", styleBI);
			documentModel.insertString(documentModel.getLength(), line + "\n", styleGP);

			// LPAL ICON
			try {

				int index = line.indexOf(":)");
				int start = 0;
				while (index > -1) {
					documentModel.remove( documentModel.getLength() - line.length()-1 + index, 2);
					SimpleAttributeSet attrs = new SimpleAttributeSet();
					StyleConstants.setIcon(attrs, getImageHappy());
					documentModel.insertString(1 + documentModel.getLength() - line.length() + index, ":)", attrs);
					start = index + 2;
					index = line.indexOf(":)", start);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {

				int index = line.indexOf(":(");
				int start = 0;
				while (index > -1) {
					documentModel.remove( documentModel.getLength() - line.length()-1 + index, 2);
					SimpleAttributeSet attrs = new SimpleAttributeSet();
					StyleConstants.setIcon(attrs, getImageSad());
					documentModel.insertString(1 + documentModel.getLength() - line.length() + index, ":(", attrs);
					start = index + 2;
					index = line.indexOf(":(", start);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// Protocole traitant l'ensemble des messages recus du serveur
	void readMsg() throws IOException {
		String line = streamIn.readUTF();
		System.out.println("ClientToserver recoit " + line);

		unMessageIRC.decode(line);
		System.out.println("Verbe:" + unMessageIRC.verbe);

		if (unMessageIRC.verbe.equals(IfClientServerProtocol.ADD)) {
			// Message recu commence par <ADD>,
			String newUser = unMessageIRC.userEmetteur;
			System.out.println("newUser=" + newUser);
			if (!clientListModel.contains(newUser)) {
				// le user entre dans le salon s'il n'y était pas déja
				clientListModel.addElement(newUser);
				receiveMessage(newUser, " entre dans le Chat...");
			}
		} else if (unMessageIRC.verbe.equals(IfClientServerProtocol.DEL)) {
			// Message recu commence par <DEL>,
			String delUser = unMessageIRC.userEmetteur;
			if (clientListModel.contains(delUser)) {
				// le user quitte le salon s'il y était
				clientListModel.removeElement(delUser);
				receiveMessage(delUser, " quitte le salon !");
			}
		} else if (unMessageIRC.verbe.equals(IfClientServerProtocol.AJ_SAL)) {
			traiterAjoutSalon(line);
		} else if (unMessageIRC.verbe.equals(IfClientServerProtocol.REJOINT_SAL)) {
			traiterRejointSalon(line);
		} else if (unMessageIRC.verbe.equals(IfClientServerProtocol.QUITTE_SAL)) {
			traiterQuitterSalon(line);
		} else {
			
			receiveMessage(unMessageIRC.userEmetteur, unMessageIRC.commentaire);
		}
	}

	private void traiterQuitterSalon(String line) {
		
		receiveMessage(unMessageIRC.userEmetteur,
				unMessageIRC.userEmetteur + " quitte le salon  " + unMessageIRC.salonCree);

		// Enlever l'étoiles du unMessageIRC.userEmetteur si elles est là .
		for (int vli = 0; vli < clientListModel.size(); vli++) {

			String unUser = clientListModel.getElementAt(vli);
			String laFinDuUser = unUser.substring(unUser.length() - 1, unUser.length());
			String unUserSansEtoile = unUser.substring(0, unUser.length() - 1);

			if (unUserSansEtoile.equals(unMessageIRC.userEmetteur)) {
				if (laFinDuUser.equals("=")) {
					clientListModel.setElementAt(unUserSansEtoile, vli);
				}
			}
		}

		/*
		 * //Enlever une etoile. for (int vli = 0; vli < clientListModel.size();
		 * vli++) { if
		 * (clientListModel.getElementAt(vli).equals(unMessageIRC.userEmetteur))
		 * { clientListModel.setElementAt(clientListModel.getElementAt(vli).
		 * substring(0, clientListModel.getElementAt(vli).length() - 1), vli); }
		 * }
		 */

		/*
		 * // Enlever les étoiles si elles sont là . for (int vli = 0; vli <
		 * clientListModel.size(); vli++) {
		 * System.out.println(clientListModel.getElementAt(vli).substring(
		 * clientListModel.getElementAt(vli).length() - 1,
		 * clientListModel.getElementAt(vli).length())); if
		 * (clientListModel.getElementAt(vli).substring(clientListModel.
		 * getElementAt(vli).length() - 1,
		 * clientListModel.getElementAt(vli).length()) == "*" ){
		 * clientListModel.setElementAt(clientListModel.getElementAt(vli).
		 * substring(0, clientListModel.getElementAt(vli).length() - 1), vli); }
		 * }
		 */

		// parcourir la liste et grasser qui est unMessageIRC.userEmetteur
		// for (int vli = 0; vli < clientListModel.size(); vli++) {
		// System.out.println("traiterQuitterSalon test :"
		// +clientListModel.toString());
		// }

		// TODO, mettre en gras les users du salon courant
		// donc ici remettre en normal le user recu
		// Voir si necessaire d'avertir qu'un client rejoint le salon courant

		// !!! user LieAuThread n'est pas alimenté
		// Salon salonCourant =
		// BroadcastThread.listeDesSalons.get(userLieAuThread.getIdSalon());
		// if (salonCourant.getNomSalon() == nomSalon) {
		// if(clientListModel.contains(nomUser)){
		// // ajout dans liste user si pas déja présent
		// clientListModel.removeElement(nomUser);
		// }
		// }
	}

	private void traiterRejointSalon(String line) {
		// Message recu commence par <REJOINT_SAL>, un user rejoint un salon
		// Si c'est le salon de l'utilisateur courant alors on peut l'ajouter a
		// la liste des users
		// String reste =
		// line.substring(IfClientServerProtocol.REJOINT_SAL.length());
		// String[] rejointMsg=reste.split(IfClientServerProtocol.SEPARATOR);
		// String nomUser=rejointMsg[0];
		// String nomSalon = rejointMsg[1];

		receiveMessage(unMessageIRC.userEmetteur,
				unMessageIRC.userEmetteur + " rejoint le salon " + unMessageIRC.salonCree);

		// parcourir la liste et mettre une croix à celui qui est
		// unMessageIRC.userEmetteur

		for (int vli = 0; vli < clientListModel.size(); vli++) {
			if (clientListModel.getElementAt(vli).equals(unMessageIRC.userEmetteur)) {
				System.out.println("traiterRejointSalon  test:" + clientListModel.getElementAt(vli).toString());
				// grisser l'indice vli
				clientListModel.setElementAt(clientListModel.getElementAt(vli) + "=", vli);
			}
		}

		// Salon salonCourant =
		// BroadcastThread.listeDesSalons.get(userLieAuThread.getIdSalon());

		// if (salonCourant.getNomSalon() == nomSalon) {
		// if(!clientListModel.contains(nomUser)){
		// // ajout dans liste user si pas déja présent
		// clientListModel.addElement(nomUser);
		// receiveMessage(nomUser, nomUser + " rejoint le salon " + nomSalon );
		// }
		// }
	}

	protected void traiterAjoutSalon(String line) {
		// Message recu commence par <AJSAL>, ajout de salon
		String nomSalon = "";
		nomSalon = unMessageIRC.salonCree;
		receiveMessage(unMessageIRC.userEmetteur, "Le salon " + nomSalon + " a été crée!");
		// salonListModel est NULL
		if (!salonListModel.contains(nomSalon)) {
			// le user cree un salon si existe pas déja
			System.out.println("ClientToserver " + "ajout");
			salonListModel.addElement(nomSalon);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cfranc.irc.client.IfSenderModel#setMsgToSend(java.lang.String)
	 */
	@Override
	public void setMsgToSend(String _msgToSend) {
		// On recoit un message on le met dans le buffer a poster
		// (c'est une simple chaine, on n'aura pas plusieurs messages posté
		// en attente de l'envoi)

		// si msgtosend commence par un verbe alors
		// envoyer verbe + login + message
		// sinon
		// envoyer login suivi de message
		System.out.println("setMsgtoSend de clienttoserver" + _msgToSend);

		_msgToSend = _msgToSend.replace("<User Courant>", this.login);

		// L'interface ne connaissant pas le login courant
		// "<User courant>" est remplacé par le login
		this.msgToSend = _msgToSend;

		System.out.println("client to server" + this.msgToSend);

	}

	public void setMsgToSend(String _verbe, String _commentaire, String _salon, String _userPrivate) {
		this.msgToSend = unMessageIRC.encode(this.login, _verbe, _commentaire, _salon, _userPrivate);

	}

	// Pousser le message en attente sur le flux de sortie
	private boolean sendMsg() throws IOException {
		boolean res = false;
		if (msgToSend != null) {
			streamOut.writeUTF(msgToSend);
			System.out.println("ClientToserver " + "on envoie au serveur le message" + msgToSend);
			msgToSend = null;
			streamOut.flush();
			res = true;

		}
		return res;
	}

	public void quitServer() throws IOException {
		// Pousser un message DEL sur le flux de sortie vers le serveur
		String messageEncode = unMessageIRC.encode(login, IfClientServerProtocol.DEL, "", "", "");
		streamOut.writeUTF(messageEncode);
		streamOut.flush();
		done = true; // Pour quitter la boucle et libérer les ressources
	}

	boolean done;

	@Override
	public void run() {
		try {
			open();
			done = !authentification(); // On commence si authentification
										// correcte
			while (!done) {
				try {
					// Si message en entrée, alors les lire et les traiter
					if (streamIn.available() > 0) {
						readMsg();
					}

					// Puis on envoie le message s'il y en a un
					if (!sendMsg()) {
						Thread.sleep(100);
						// Si pas de message on patiente un peu avant de boucler
						// pour pas tout saturer
					}
				} catch (IOException | InterruptedException ioe) {
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
		boolean res = false;
		String loginPwdQ;
		try {
			// On attend les données en entrée venant de l'authentification
			while (streamIn.available() <= 0) {
				Thread.sleep(100);
			}
			loginPwdQ = streamIn.readUTF(); // sous forme #login?#passwd
			if (loginPwdQ.equals(IfClientServerProtocol.LOGIN_PWD)) {
				// On envoie au serveur ce login mot de passe (pour qu'il
				// accepte ou non sa connection)
				streamOut.writeUTF(unMessageIRC.encode(this.login, "#Connection#", this.pwd, ".", "."));
				// #login#pwd
				// IfClientServerProtocol.SEPARATOR+this.login+IfClientServerProtocol.SEPARATOR+this.pwd);
			}

			// On attend maintenant l'accusé reception du serveur
			while (streamIn.available() <= 0) {
				Thread.sleep(100);
			}
			String acq = streamIn.readUTF();
			if (acq.equals(IfClientServerProtocol.OK)) {
				// Le serveur a accepté l'acquittement sera positif (return
				// true)
				res = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}


	protected ImageIcon getImageSad() {
		BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(Color.red);
		g.drawOval(0, 0, 14, 14);
		g.drawLine(4, 9, 9, 9);
		g.drawOval(4, 4, 1, 1);
		g.drawOval(10, 4, 1, 1);
		return new ImageIcon(bi);
	}

	protected ImageIcon getImageHappy() {
		BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(Color.green);
		g.drawOval(0, 0, 14, 14);
		g.drawLine(4, 9, 9, 9);
		g.drawOval(4, 4, 1, 1);
		g.drawOval(10, 4, 1, 1);
		return new ImageIcon(bi);
	}

}
