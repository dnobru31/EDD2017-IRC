package com.cfranc.irc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import com.cfranc.irc.server.SalonLst;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class SimpleChatFrameServer extends JFrame{

	public StyledDocument model=null;
	public DefaultListModel<String> clientListModel=null;
	public SalonLst serverSalon=null;
			
	public SimpleChatFrameServer(int port, StyledDocument model,  DefaultListModel<String> clientListModel) {
		super("ISM - IRC Server Manager");
		this.model=model;
		this.clientListModel=clientListModel;
		serverSalon = new SalonLst();
		initialize();
		
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setBounds(100, 100, 702, 339);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JTextPane textPane = new JTextPane(model);
		scrollPane.setViewportView(textPane);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
				
			}
		});
		
		JScrollPane scrollPaneList = new JScrollPane();
		getContentPane().add(scrollPaneList, BorderLayout.WEST);

		final JLabel statusBar=new JLabel("");
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		final JList<String> list = new JList<String>(clientListModel);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String clientSelected=list.getSelectedValue().toString();
				statusBar.setText(clientSelected);
			}
		});
		list.setMinimumSize(new Dimension(200,0));
		scrollPaneList.setViewportView(list);
						
		
	}	
}