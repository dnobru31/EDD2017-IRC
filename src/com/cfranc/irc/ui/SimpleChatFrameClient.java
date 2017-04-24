package com.cfranc.irc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import com.cfranc.irc.IfClientServerProtocol;
import com.cfranc.irc.ProtocoleIRC;
import com.cfranc.irc.client.IfSenderModel;

import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JEditorPane;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.border.BevelBorder;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class SimpleChatFrameClient extends JFrame {

	private static Document documentModel;
	private static ListModel<String> listModel;
	private static ListModel<String> listSalonModel;

	IfSenderModel sender;
	private String senderName;
	private String senderSalonName;

	private JPanel contentPane;
	private JTextField textField;
	private JLabel lblSender;

	private final ResourceAction sendAction = new SendAction();
	private final ResourceAction sendActionNewSalon = new sendActionNewSalon();
	private final ResourceAction lockAction = new LockAction();

	private boolean isScrollLocked = true;

	private ProtocoleIRC unMessageIRC = new ProtocoleIRC();

	/**
	 * Launch the application.
	 * 
	 * @throws BadLocationException
	 */
	public static void main(String[] args) throws BadLocationException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimpleChatFrameClient frame = new SimpleChatFrameClient();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Scanner sc = new Scanner(System.in);
		String line = ""; //$NON-NLS-1$
		while (!line.equals(".bye")) { //$NON-NLS-1$
			line = sc.nextLine();
		}
	}

	public static void sendMessage(String user, String line, Style styleBI, Style styleGP) {
		try {
			documentModel.insertString(documentModel.getLength(), user + " : ", styleBI); //$NON-NLS-1$
			documentModel.insertString(documentModel.getLength(), line + "\n", styleGP); //$NON-NLS-1$
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void sendMessage() throws BadLocationException {
		sender.setMsgToSend(unMessageIRC.encode("<User courant>", "DISCUTE", textField.getText(), "", ""));

	}

	public void sendMsgCSreationSalonToSend(String salonACreer) {
		sender.setMsgToSend(unMessageIRC.encode("<User Courant>", IfClientServerProtocol.AJ_SAL, "", salonACreer, ""));
		// IfClientServerProtocol.AJ_SAL + salonACreer);
	}

	public void sendMsgRejoindreUnSalonToSend(String nomDuSalon) {
		sender.setMsgToSend(
				unMessageIRC.encode("<User Courant>", IfClientServerProtocol.REJOINT_SAL, "", nomDuSalon, ""));

		// LPAL
		for (int vli = 0; vli < listModel.getSize(); vli++) {
			String unUser = listModel.getElementAt(vli);
			String laFinDuUser = unUser.substring(unUser.length() - 1, unUser.length());

			if (laFinDuUser.equals("-")) {
				listModel.getElementAt(vli).replaceAll("-", "");
				((DefaultListModel<String>) listModel).setElementAt(listModel.getElementAt(vli).replaceAll("-", " "),
						vli);
			}

		}
	}

	public SimpleChatFrameClient() {
		this(null, new DefaultListModel<String>(), new DefaultListModel<String>(),
				SimpleChatClientApp.defaultDocumentModel());
	}

	/**
	 * Create the frame.
	 */
	public SimpleChatFrameClient(IfSenderModel sender, ListModel<String> clientListModel,
			ListModel<String> salonListModel, Document documentModel) {
		this.sender = sender;
		this.documentModel = documentModel;
		this.listModel = clientListModel;
		this.listSalonModel = salonListModel;
		setTitle(Messages.getString("SimpleChatFrameClient.4")); //$NON-NLS-1$
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 740, 600);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu(Messages.getString("SimpleChatFrameClient.5")); //$NON-NLS-1$
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);

		JMenuItem mntmEnregistrerSous = new JMenuItem(Messages.getString("SimpleChatFrameClient.6")); //$NON-NLS-1$
		mnFile.add(mntmEnregistrerSous);

		JMenu mnOutils = new JMenu(Messages.getString("SimpleChatFrameClient.7")); //$NON-NLS-1$
		mnOutils.setMnemonic('O');
		menuBar.add(mnOutils);

		JMenuItem mntmEnvoyer = new JMenuItem(Messages.getString("SimpleChatFrameClient.8")); //$NON-NLS-1$
		mntmEnvoyer.setAction(sendAction);
		mnOutils.add(mntmEnvoyer);

		JSeparator separator = new JSeparator();
		mnOutils.add(separator);
		JCheckBoxMenuItem chckbxmntmNewCheckItem = new JCheckBoxMenuItem(lockAction);
		mnOutils.add(chckbxmntmNewCheckItem);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);

		JButton button = toolBar.add(sendAction);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.add(panel_2, BorderLayout.WEST);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_2.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel panel = new JPanel();
		panel_1.add(panel);

		lblSender = new JLabel(Messages.getString("SimpleChatFrameClient.lblSender.text")); //$NON-NLS-1$
		lblSender.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSender.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSender.setPreferredSize(new Dimension(100, 14));
		lblSender.setMinimumSize(new Dimension(100, 14));

		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				Messages.getString("SimpleChatFrameClient.12")); //$NON-NLS-1$
		textField.getActionMap().put(Messages.getString("SimpleChatFrameClient.13"), sendAction); //$NON-NLS-1$

		JButton btnSend = new JButton(sendAction);
		btnSend.setMnemonic(KeyEvent.VK_ENTER);

		JButton btnNewSalon = new JButton(sendActionNewSalon);
		btnNewSalon.setText(Messages.getString("SimpleChatFrameClient.14"));
		btnNewSalon.setMnemonic(KeyEvent.VK_ENTER);

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup().addGap(7).addComponent(btnNewSalon)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblSender, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(gl_panel
				.createSequentialGroup().addGap(10)
				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblSender, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
						.addComponent(btnNewSalon))));
		panel.setLayout(gl_panel);

		JSplitPane splitPane = new JSplitPane();
		panel_2.add(splitPane, BorderLayout.CENTER);

		JTextPane textArea = new JTextPane((StyledDocument) documentModel);

		// LPAL ICON
		// textArea.setEditorKit(new StyledEditorKit());
		// getContentPane().add(textArea, BorderLayout.CENTER);
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setIcon(attrs, getImageHappy());
		textArea.addCaretListener(new CaretListener() {
		public void caretUpdate(CaretEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							StyledDocument doc = (StyledDocument) textArea.getDocument();
							String text = doc.getText(0, textArea.getDocument().getLength());
							int index = text.indexOf(":)");
							int start = 0;
							while (index > -1) {
								Element el = doc.getCharacterElement(index);
								if (StyleConstants.getIcon(el.getAttributes()) == null) {
									doc.remove(index, 2);
									SimpleAttributeSet attrs = new SimpleAttributeSet();
									StyleConstants.setIcon(attrs, getImageHappy());
									doc.insertString(index, ":)", attrs);
								}
								start = index + 2;
								index = text.indexOf(":)", start);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
						try {
							StyledDocument doc = (StyledDocument) textArea.getDocument();
							String text = doc.getText(0, textArea.getDocument().getLength());
							int index = text.indexOf(":(");
							int start = 0;
							while (index > -1) {
								Element el = doc.getCharacterElement(index);
								if (StyleConstants.getIcon(el.getAttributes()) == null) {
									doc.remove(index, 2);
									SimpleAttributeSet attrs = new SimpleAttributeSet();
									StyleConstants.setIcon(attrs, getImageSad());
									doc.insertString(index, ":(", attrs);
								}
								start = index + 2;
								index = text.indexOf(":(", start);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
					}
				});

			}

		});
			
		documentModel.addDocumentListener(new DocumentListener(){

			@Override
			public void insertUpdate(DocumentEvent e) {
				System.out.println("insertUpdate");
				
				try {
					StyledDocument doc = (StyledDocument) textArea.getDocument();
					String text = doc.getText(0, textArea.getDocument().getLength());
					int index = text.indexOf(":)");
					int start = 0;
					while (index > -1) {
						Element el = doc.getCharacterElement(index);
						if (StyleConstants.getIcon(el.getAttributes()) == null) {
							doc.remove(index, 2);
							SimpleAttributeSet attrs = new SimpleAttributeSet();
							StyleConstants.setIcon(attrs, getImageHappy());
							doc.insertString(index, ":)", attrs);
						}
						start = index + 2;
						index = text.indexOf(":)", start);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				try {
					StyledDocument doc = (StyledDocument) textArea.getDocument();
					String text = doc.getText(0, textArea.getDocument().getLength());
					int index = text.indexOf(":(");
					int start = 0;
					while (index > -1) {
						Element el = doc.getCharacterElement(index);
						if (StyleConstants.getIcon(el.getAttributes()) == null) {
							doc.remove(index, 2);
							SimpleAttributeSet attrs = new SimpleAttributeSet();
							StyleConstants.setIcon(attrs, getImageSad());
							doc.insertString(index, ":(", attrs);
						}
						start = index + 2;
						index = text.indexOf(":(", start);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				System.out.println("removeUpdate");
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				System.out.println("changedUpdate");
				
			}
			
		});
		
		textArea.setEnabled(true);

		
		//LPAL
		// gerer les click sur ce composant
		// textArea.addMouseListener(new MouseAdapter()
		textArea.addFocusListener(new FocusListener() {
			
			/*public void mouseClicked(MouseEvent me) {
				javax.swing.JOptionPane.showMessageDialog(null,
						"les modification dans cette zones ne seront pas sauvegardées.", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}*/

			@Override
			public void focusGained(FocusEvent e) {
				/*javax.swing.JOptionPane.showMessageDialog(null,
						"les modification dans cette zones ne sont pas autorisées.", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				textField.requestFocus();*/

			}

			@Override
			public void focusLost(FocusEvent e) {
				
			}

		});

		
		
		JScrollPane scrollPaneText = new JScrollPane(textArea);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(textArea, popupMenu);

		JCheckBoxMenuItem chckbxmntmLock = new JCheckBoxMenuItem(Messages.getString("SimpleChatFrameClient.10")); //$NON-NLS-1$
		chckbxmntmLock.setEnabled(isScrollLocked);
		popupMenu.add(chckbxmntmLock);
		chckbxmntmLock.addActionListener(lockAction);

		scrollPaneText.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (isScrollLocked) {
					e.getAdjustable().setValue(e.getAdjustable().getMaximum());
				}
			}
		});

		splitPane.setRightComponent(scrollPaneText);

		JPanel panel_3 = new JPanel();
		splitPane.setLeftComponent(panel_3);
		panel_3.setLayout(new GridLayout(0, 1, 0, 0));

		JList<String> list = new JList<String>(listModel);
		list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_3.add(list);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int iFirstSelectedElement = ((JList) e.getSource()).getSelectedIndex();
				if (iFirstSelectedElement >= 0 && iFirstSelectedElement < listModel.getSize()) {
					senderName = listModel.getElementAt(iFirstSelectedElement);
					getLblSender().setText(senderName);
				} else {
					getLblSender().setText("?"); //$NON-NLS-1$
				}
			}
		});
		list.setMinimumSize(new Dimension(150, 0));

		JList<String> listSalon = new JList<String>(this.listSalonModel);
		listSalon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				int rep = JOptionPane.showConfirmDialog(null, "Voulez vous rejoindre ce salon?");
				if (rep == 0) {
					sendMsgRejoindreUnSalonToSend(listSalon.getSelectedValue());
					textArea.setText("");
				}

			}
		});
		listSalon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSalon.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_3.add(listSalon);
		listSalon.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int iFirstSelectedElement = ((JList) e.getSource()).getSelectedIndex();
				if (iFirstSelectedElement >= 0 && iFirstSelectedElement < listSalonModel.getSize()) {
					senderSalonName = listSalonModel.getElementAt(iFirstSelectedElement);
					getLblSender().setText(senderSalonName);
				} else {
					getLblSender().setText("?"); //$NON-NLS-1$
				}
			}
		});
		listSalon.setMinimumSize(new Dimension(150, 0));

	}

	public JLabel getLblSender() {
		return lblSender;
	}

	private abstract class ResourceAction extends AbstractAction {
		public ResourceAction() {
		}
	}

	private class SendAction extends ResourceAction {
		private Icon getIcon() {
			return new ImageIcon(SimpleChatFrameClient.class.getResource("send_16_16.jpg")); //$NON-NLS-1$
		}

		public SendAction() {
			putValue(NAME, Messages.getString("SimpleChatFrameClient.3")); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, Messages.getString("SimpleChatFrameClient.2")); //$NON-NLS-1$
			putValue(SMALL_ICON, getIcon());
		}

		public void actionPerformed(ActionEvent e) {
			try {
				sendMessage();
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private class sendActionNewSalon extends ResourceAction {
		private Icon getIcon() {
			return new ImageIcon(SimpleChatFrameClient.class.getResource("send_16_16.jpg")); //$NON-NLS-1$
		}

		public sendActionNewSalon() {
			putValue(NAME, Messages.getString("SimpleChatFrameClient.14")); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, Messages.getString("SimpleChatFrameClient.14")); //$NON-NLS-1$
			putValue(SMALL_ICON, getIcon());
		}

		public void actionPerformed(ActionEvent e) {

			String res = JOptionPane.showInputDialog(null, "Veuillez saisir le nom de votre salon.",
					"Création d'un nouveau salon", JOptionPane.QUESTION_MESSAGE);
			if (res != null) {
				System.out.println(res);
				sendMsgCSreationSalonToSend(res);
			}
			// sendMessage();
		}
	}

	private class LockAction extends ResourceAction {
		public LockAction() {
			putValue(NAME, Messages.getString("SimpleChatFrameClient.1")); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, Messages.getString("SimpleChatFrameClient.0")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			isScrollLocked = (!isScrollLocked);
		}
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	// LPAL ICON
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
