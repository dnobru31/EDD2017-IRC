package com.cfranc.irc.ui;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import com.cfranc.irc.server.ClientConnectThread;
import com.cfranc.irc.server.SalonLst;

public class SimpleChatServerApp {

	private SimpleChatFrameServer frame;
	public StyledDocument model=new DefaultStyledDocument();
	DefaultListModel<String> clientListModel=new DefaultListModel<String>();
	private ClientConnectThread clientConnectThread;
	public SalonLst mySalons=null;
			
	public SimpleChatServerApp(int port) {
		
		// Init GUI
		this.frame=new SimpleChatFrameServer(port, this.model, clientListModel);
		this.mySalons= frame.serverSalon;
		try {
			this.model.insertString(this.model.getLength(), "Wellcome into IRC Server Manager\n", null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		((JFrame)this.frame).setVisible(true);
		
		// Start connection services
		this.clientConnectThread=new ClientConnectThread(port, this.model, clientListModel);
		this.clientConnectThread.start();
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimpleChatServerApp app = new SimpleChatServerApp(4567);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}