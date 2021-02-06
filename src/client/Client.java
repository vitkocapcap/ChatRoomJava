package client;


import message.*;
import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.InputEvent;


import java.awt.Dimension;

import javax.swing.*;
import javax.swing.text.Keymap;


import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Client extends JFrame implements ActionListener{
	
	//GUI Variables
	private static final long serialVersionUID = 1L;
	private JButton send,clear,logout,login,quit,sendFile,setkey,setStyle;
	private JPanel p_login,p_chat;
	private JPanel p1,p2,p2a,p2b,pf,pfa,p3;
	private JLabel p_bgr,pFile1,onlList,nameLabel,mess;
	private Font font;
    private JTextField nick,currentnick, inputkey, keyindicator;
	public static JTextArea msg, online, message, files;
	public static int keyShift=0;
	
	public DefaultComboBoxModel<String> colorStyle;
	public JComboBox<String> colorCombo;
	
	private JFileChooser fileDialog;
	public static Path folderPath;
	public static File folder;

	//Input stream to store files
	FileInputStream fis = null;
    BufferedInputStream bis = null;
	
	//Declare output and input Stream to send Data
	public static ObjectOutputStream os;
	public static ObjectInputStream is;

	//Declare the number of additional thread
	public static final int NUM_OF_THREAD = 1;

	//Declare server IP and server port
	public final static String SERVER_IP = "127.0.0.1";
	//public final static String SERVER_IP = "192.168.0.100";
	public final static int SERVER_PORT = 7;
	Socket socket = null;

	
	public static ArrayList<String> userList;
	public static ArrayList<String> fileList;

	//Client's nick name
	public static String name = "";

	//Adding source of images//
	ImageIcon imgclient = new ImageIcon("resources/client.png");
	ImageIcon imgsend = new ImageIcon("resources/send.png");
	ImageIcon imgexit = new ImageIcon("resources/exit.png");
	ImageIcon imglogin = new ImageIcon("resources/login.png");
	ImageIcon imgquit = new ImageIcon("resources/quit.png");
	ImageIcon imgattach = new ImageIcon("resources/attach.png");
	ImageIcon imgdelete = new ImageIcon("resources/delete.png");
	ImageIcon imgbackground = new ImageIcon("resources/background.gif");
    

	public Client(){
		super("Client");
		this.setIconImage(imgclient.getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Stop the program when exiting
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
                if(socket != null){
                    try {
                        socket.close();
                        System.exit(0);
                    } catch (IOException e1) {
                        msg.append("Can not stop server.\n");
                    }
                }
			}	
		});
		//Start UI
		GUI(); 
	}

	/*
	 * This function is responsible for all the GUI settings
	 */
	private void GUI() {
		//Set the size of the main frame of UI
		setSize(800, 580); 
		setResizable(false);
		setLocation(250,80);
		setLayout(new BorderLayout());

		//-------Login background gif--------/
		p_bgr = new JLabel(imgbackground);
		p_bgr.setBackground(Color.BLACK);
		add(p_bgr,BorderLayout.CENTER);
		//----------------------------------------/

		//-------- Setting up buttons ------------/

		//Allow users to choose files from computer
		fileDialog = new JFileChooser();

		//Create exit button and listener
		logout = new JButton(imgexit);
		logout.setBackground(Color.WHITE);
		//addActionListener(this) will call actionPerformed(event)
		logout.addActionListener(this); 

		//Create send button and listener
		send = new JButton(imgsend);
		send.setBackground(Color.WHITE);
		send.addActionListener(this);
		
		//Create send button and listener
		setkey = new JButton("SET");
		setkey.setBackground(Color.WHITE);
		setkey.addActionListener(this); 

		//Create clear button and listener
		clear = new JButton(imgdelete);
		clear.setBackground(Color.WHITE);
		clear.addActionListener(this); 
		
		//Create change button and listener
		setStyle = new JButton("Change");
		setStyle.setBackground(Color.WHITE);
		setStyle.addActionListener(this); 

		//Create send file button and listener
		sendFile = new JButton(imgattach);
		sendFile.setBackground(Color.WHITE);
		sendFile.addActionListener(this); 
		
		//Create Log in button and listener
		login= new JButton(imglogin);
		login.setBackground(Color.BLACK);
		login.addActionListener(this); 

		//Create Log out button and listener
		quit= new JButton(imgquit);
		quit.setBackground(Color.BLACK);
		quit.addActionListener(this); 

		//-----------------------------------------//
		

		//----------- Setting login UI-------------//
		p_login = new JPanel();
		//Create login panel with the layout in center
		p_login.setLayout(new FlowLayout(FlowLayout.CENTER)); 
		
		JLabel inputLabel = new JLabel("Enter your Nickname: ");
		inputLabel.setForeground(Color.WHITE);
		//Text field for typing name
		nick=new JTextField(20); 
		nick.setForeground(Color.WHITE);
		nick.setBackground(Color.BLACK);
		//add(show command) the 4 components of login start
		p_login.add(inputLabel); //Indicate text
		p_login.add(nick); 
		p_login.add(login);
		p_login.add(quit);
		
		p_login.setBackground(Color.BLACK);
		/* initially create a layout with border for the whole UI
		 * and set the position of the login panel at the
		 * highest position of the layout.
		 */
		add(p_login,BorderLayout.SOUTH);

		//---------- End of login UI -------------//

		//------------------------------------------------------------------------------------------//
		
		//--------- Chat UI after login ----------//

		//-------- NORTH layout settings --------//
		
		//North layout is responsible for displaying username, theme options, log out

		//Initialize the panel 1 (North)
		p1 = new JPanel();  
		p1.setLayout(new FlowLayout(FlowLayout.LEFT)); 
		currentnick = new JTextField(20);
		currentnick.setBackground(Color.WHITE);

		//Display user name
		nameLabel = new JLabel("Current Nickname: ");
		nameLabel.setForeground(Color.RED);	
		
		//Change Theme Setting
		colorStyle = new DefaultComboBoxModel<String>();
		colorStyle.addElement("Light Mode");
		colorStyle.addElement("Dark Mode");
		colorStyle.addElement("Pinky");
		colorStyle.addElement("Cream");
		colorCombo = new JComboBox<String>(colorStyle);
		colorCombo.setSelectedIndex(0);
		JScrollPane colorListScrollPane = new JScrollPane(colorCombo); 
		
		colorCombo.setBackground(Color.WHITE);
		colorCombo.setForeground(Color.BLACK);
		
		//Add components to panel 1
		p1.add(nameLabel); 
		p1.add(currentnick);
		p1.add(logout);
		p1.add(setStyle);
		p1.add(colorListScrollPane);
		
		//-------- End of NORTH Layout ----------//
		
		//-------- WEST layout settings --------//
		//West layout is responsible for displaying online user list, key shift option

		//Initialize the panel 2 (West)
		p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		
		//Layout for online user list
		p2a = new JPanel();							
		p2a.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		onlList = new JLabel("ONLINE USERS");
		onlList.setForeground(Color.GREEN);
		
		//Indicate text
		p2a.add(onlList); 

		//Put the label "Online users" to the north of layout p2
		p2.add(p2a,BorderLayout.NORTH);	
		
		//Create the user list of length 10, width 10
		online = new JTextArea(10,10); 
		online.setForeground(Color.GREEN);
		//The text of the list can not be modified by the user, automatically changed by login or quit
		online.setEditable(false);   

		//Change the Caeser key
		inputkey = new JTextField(2);
		inputkey.setBackground(Color.WHITE);
		inputkey.setForeground(Color.BLACK);
		
		//Indicate the current key
		keyindicator = new JTextField("0",2);
		keyindicator.setBackground(Color.BLACK);
		keyindicator.setForeground(Color.YELLOW);
		keyindicator.setEditable(false);
		
		//Layout for key shifting
		p2b = new JPanel();							
		p2b.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		p2b.add(setkey);
		p2b.add(inputkey);
		p2b.add(keyindicator);
		
		//Put the key shift setting to the bottom of p2
		p2.add(p2b,BorderLayout.SOUTH);	
		
		//Put the list at the center of the layout p2
		p2.add(new JScrollPane(online),BorderLayout.CENTER); 

		p2.add(new JLabel("     "),BorderLayout.EAST); //Left Border layout 
		p2.add(new JLabel("     "),BorderLayout.WEST); //Right Border layout

		//-------- End of WEST Layout ----------//

		//-------- EAST layout settings --------//
		//East layout is responsible for displaying file list


		//Initialize the panel f (East)
		pf = new JPanel();							
		pf.setLayout(new BorderLayout());
		
		//Layout for list of available files 		
		pfa = new JPanel();
		pfa.setLayout(new FlowLayout(FlowLayout.CENTER));	

		pFile1 = new JLabel("FILES");
		pFile1.setForeground(Color.GREEN);
		
		pfa.add(pFile1); //Indicate text

		//Put the label "FILES" to the north of layout p2
		pf.add(pfa,BorderLayout.NORTH);	
		
		//Create the file list of length 10, width 10
		files = new JTextArea(10,15); 
		files.setForeground(Color.GREEN);
		//The text of the list can not be modified by the user, automatically changed by login or quit
		files.setEditable(false);   

		//Put the list at the center of the layout pf
		pf.add(new JScrollPane(files),BorderLayout.CENTER); 
		
		pf.add(new JLabel("     "),BorderLayout.EAST); //Left Border layout 
		pf.add(new JLabel("     "),BorderLayout.WEST); //Right Border layout

		
		//-------- End of EAST Layout ----------//
		

		//-------- SOUTH layout settings --------//		
		//South layout is responsible for displaying message input, send and clear option

		//Layout p3 (south) is the message being type by user 
		p3 = new JPanel();
		//Set the panel to the center of the p3 layout
		p3.setLayout(new FlowLayout(FlowLayout.CENTER)); 
		//Create label "Message"
		mess=new JLabel("Message");
		p3.add(mess); 
		
		//Create message area indicating all messages and announcement
		message = new JTextArea(3,30);
		message.setMaximumSize(new Dimension(50,50));
		font = new Font(message.getText(), Font.ITALIC,12);
		message.setFont(font);
		message.setText("Your message!");
		//When the charaters are out of the specified area, it will be wrapped
		message.setLineWrap(true);
		message.setWrapStyleWord(true);		

		//add 3 components
		p3.add(new JScrollPane(message)); 
		//p3.add(new JScrollPane(message));
		p3.add(send);
		p3.add(sendFile);
		p3.add(clear);
		
		//-------- End of SOUTH Layout ----------//
		
		//--------  CENTER Layout setting ----------//

		//Create a text area to displat messages and announcements
		msg = new JTextArea(); 
		//Chat content can not be modified
		msg.setEditable(false); 
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);

		//-----  End of CENTER layout setting ------//
		

		//Light mode Color as default
		p1.setBackground(new Color(255,255,255));
		p2.setBackground(new Color(255,255,255));
		p2a.setBackground(new Color(255,255,255));
		p2b.setBackground(new Color(255,255,255));
		pf.setBackground(new Color(255,255,255));
		pfa.setBackground(new Color(255,255,255));
		p3.setBackground(new Color(255,255,255));
		

		/* Combine all the layouts: North(Nickname), South(Message), 
		 * East(File list), West(Online user) and Center(Chat content)
		 */

		//Create a general panel 
		p_chat = new JPanel();
		//Create layout with border
		p_chat.setLayout(new BorderLayout()); 
		//The chat content as a Scroll pane is set in the center layout
		p_chat.add(new JScrollPane(msg),BorderLayout.CENTER); 
		//Nick name and other p1 components are set in the North layout
		p_chat.add(p1,BorderLayout.NORTH);
		//Online user list and other p2 components are set at the West layout
		p_chat.add(p2,BorderLayout.WEST); 
		//File list is set at the East layout
		p_chat.add(pf,BorderLayout.EAST); 
		//Message input and other p3 components are set at the South layout
		p_chat.add(p3,BorderLayout.SOUTH); 
		
		
		p_chat.setVisible(false); //Only turn on p_login, p_bgr and the main frame

		//-----------------End of chat UI---------------/
		
		//--------------Set Keyboard Control------------/

		//Keyboard Action for logging in
		InputMap im = login.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = login.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		am.put("enter", new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				LogIn();
			}
		});

		//Keyboard Action for sending message
		Keymap messageMap = message.getKeymap();
		KeyStroke mEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
		//Press enter to send message
		messageMap.addActionForKeyStroke(mEnter, new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMSG();
			}
		});

		//Press shift-enter to create a new line in message text area
		KeyStroke mShiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.SHIFT_DOWN_MASK);
		messageMap.addActionForKeyStroke(mShiftEnter, new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				message.append("\n");
			}
		});

		//-------------- End of Keyboard Control ------------------/

		setVisible(true); //Everything is visible
	}

	//Create socket and connect to server	
	private void go() {
		try{
			//Connect to the server
			socket = new Socket(SERVER_IP, SERVER_PORT); 
			System.out.println("Connected: " + socket);
			
			os = new ObjectOutputStream(socket.getOutputStream());
			is = new ObjectInputStream(socket.getInputStream());
			userList = new ArrayList<String>();
			fileList = new ArrayList<String>();
		}
		catch (IOException ie){
			System.out.println("can't connect to server");
			System.out.println("Exiting the Chat Room");
			JOptionPane.showMessageDialog(this,"Failed to connect to Server. Check your connection again","Error",JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
	}


	
	public static void main(String[] args) {
		Client client =new Client();
		client.go();
	}	
	

	//Encrypt the message using Caesar cipher 
	public static String Encrypt(String encmess,int keyshift) {
		//Shift keys
		if(keyshift > 26) {
			keyshift=keyshift%26;
		}
		else if (keyshift < 0){
			keyshift=(keyshift%26)+26;
		}
		String cipherText="";
		int length = encmess.length();
		for(int i=0;i<length;i++) {
			char ch=encmess.charAt(i);
			if(Character.isLetter(ch)) {
				if(Character.isLowerCase(ch)) {
					char c= (char)(ch+keyshift);
					if (c>'z') {
						cipherText+= (char)(ch-(26-keyshift));
					}
					else {
						cipherText+=c;
				}
			}
			else if(Character.isUpperCase(ch)) {
				char c= (char)(ch+keyshift);
				if (c>'Z') {
					cipherText+= (char)(ch-(26-keyshift));
				}
				else {
					cipherText+=c;
				}
			}
		}
		else {
			cipherText+=ch;
		}
	}
	//return the encrypted message 	
	return cipherText;
	}

	/*
	 * Perform the intended action when clicking buttons 
	 */
	public void actionPerformed(ActionEvent e) {
	try{
		//If the user wants to log out of the chat box
		if(e.getSource()==logout){
			if(socket != null){
				int output = JOptionPane.showConfirmDialog(this,"Do you want to log out?"," Dialog",JOptionPane.YES_NO_OPTION);
				if(output == JOptionPane.YES_OPTION){
					try {
						socket.close();			
						remove(p_chat);
						msg.setText("");
						keyShift=0;
						inputkey.setText("0");
						keyindicator.setText("0");
						
						p_login.setVisible(true);
						p_bgr.setVisible(true);	
						this.setTitle("Client"); //Change the title of frame from client to user name
						go();				
					} catch (IOException e1) {
						msg.append("Can not stop server.\n");
					}
		            }
				
			}
		}
		//If the user wants to set the key shift in Caesar cipher 
		else if(e.getSource()==setkey){
			keyShift= Integer.parseInt(inputkey.getText());
			keyindicator.setText(inputkey.getText());
			msg.append("You have set the key shift to "+ "[" +inputkey.getText()+ "]" + "\n");
			System.out.println(keyShift);
		}
		//If the user wants to clear the message
		else if(e.getSource()==clear){
			message.setText(""); //Delete all of typed message
		}
		//If the user wants to customize theme
		else if(e.getSource()==setStyle){
			SetStyle();
		}
		//If the user wants to send a file
		else if(e.getSource()==sendFile){
			//message.setText("");
			int returnVal = fileDialog.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileDialog.getSelectedFile();
				this.SendFile(file.getAbsolutePath());
			 } else {
				msg.append("Cannot open file\n"); //print successfully
			 }
		}
		//if the user wants to send the message
		else if(e.getSource()==send){
			sendMSG();
		}
		//if the user wants to log in the chat box
		else if(e.getSource()==login){
			LogIn();
		}
		//if the user wants to exit the application
		else if(e.getSource()==quit){
			if(socket != null){
				int output = JOptionPane.showConfirmDialog(this,"Do you want to exit? :((","Dialog",JOptionPane.YES_NO_OPTION);
				if(output == JOptionPane.YES_OPTION){
					try {
						socket.close();
						System.exit(0);
					} catch (IOException e1) {
						msg.append("Can not stop server.\n");
					}
		        }
					
			}
		}
	}catch(IOException e2){}
	}


	//Send the message to server, /msg and /getFile 
	private void sendMSG(){
		try{
			//If the user wants to get a file. Syntax: /cmd filename
			if(message.getText().trim().isEmpty()) {
				System.out.println("No message");
			}
			else if (message.getText().startsWith("/get")){
				System.out.println("file:"+message.getText().replace("/get ", "")+" end");
				Client.os.writeObject(new Message("/getFile", Client.name, message.getText().replace("/get ", "")));
			}
			//If it is a normal message
			else {
				String encrypted = Encrypt(message.getText().trim(), keyShift);
					//Encrypt the message
					if(keyShift%26==0){
						Client.os.writeObject(new Message("/msg0", Client.name, encrypted+"\n"));
					}
					else {
						Client.os.writeObject(new Message("/msg", Client.name, encrypted+"\n"));
					}
				
			}
			message.setText("");
		}catch(IOException e2){}
	}
	
	//This function allows user to customize the theme
	private void SetStyle() {
		String Style="";
		if (colorCombo.getSelectedIndex() != -1) {  
             int a = colorCombo.getSelectedIndex();
             Style= "" + colorCombo.getItemAt(a);  
        }
		switch(Style) {
			case "Light Mode":{
			//Light mode theme
				p1.setBackground(new Color(255,255,255));
				p2.setBackground(new Color(255,255,255));
				p2a.setBackground(new Color(255,255,255));
				p2b.setBackground(new Color(255,255,255));
				pf.setBackground(new Color(255,255,255));
				pfa.setBackground(new Color(255,255,255));
				p3.setBackground(new Color(255,255,255));
				
				send.setBackground(new Color(255,255,255));
				
				setkey.setBackground(new Color(255,255,255));
				setkey.setForeground(new Color(0,0,0));
				
				setStyle.setBackground(new Color(255,255,255));
				setStyle.setForeground(new Color(0,0,0));
				
				logout.setBackground(new Color(255,255,255));
				
				sendFile.setBackground(new Color(255,255,255));
				
				clear.setBackground(new Color(255,255,255));
				
				mess.setForeground(new Color(0,0,0));
				
				nameLabel.setForeground(new Color(255,0,0));
				
				currentnick.setBackground(new Color(255,255,255));
				currentnick.setForeground(new Color(0,0,0));
				
				colorCombo.setBackground(new Color(255,255,255));
				colorCombo.setForeground(new Color(0,0,0));
				
				inputkey.setBackground(new Color(255,255,255));
				inputkey.setForeground(new Color(0,0,0));
				
				msg.setBackground(new Color(255,255,255));
				msg.setForeground(new Color(0,0,0));
				
				message.setBackground(new Color(255,255,255));
				message.setForeground(new Color(0,0,0));
				
				onlList.setBackground(new Color(255,255,255));
				onlList.setForeground(new Color(0,160,0));
				
				online.setBackground(new Color(255,255,255));
				online.setForeground(new Color(0,160,0));
				
				pFile1.setBackground(new Color(255,255,255));
				pFile1.setForeground(new Color(0,160,0));
				
				files.setBackground(new Color(255,255,255));
				files.setForeground(new Color(0,160,0));
				
				break;
		}
			case "Dark Mode":{
				//Dark mode theme
				p1.setBackground(new Color(50,50,50));
				p2.setBackground(new Color(50,50,50));
				p2a.setBackground(new Color(50,50,50));
				p2b.setBackground(new Color(50,50,50));
				pf.setBackground(new Color(50,50,50));
				pfa.setBackground(new Color(50,50,50));
				p3.setBackground(new Color(50,50,50));

				send.setBackground(new Color(0,0,0));
				
				setkey.setBackground(new Color(0,0,0));
				setkey.setForeground(new Color(255,255,255));
				
				setStyle.setBackground(new Color(0,0,0));
				setStyle.setForeground(new Color(255,255,255));
				
				logout.setBackground(new Color(0,0,0));
				
				sendFile.setBackground(new Color(0,0,0));
				
				clear.setBackground(new Color(0,0,0));
				
				mess.setForeground(new Color(0,255,0));
				
				nameLabel.setForeground(new Color(255,0,0).brighter());
				
				currentnick.setBackground(new Color(0,0,0));
				currentnick.setForeground(new Color(0,255,0));
				
				colorCombo.setBackground(new Color(0,0,0));
				colorCombo.setForeground(new Color(255,255,255));
				
				inputkey.setBackground(new Color(0,0,0));
				inputkey.setForeground(new Color(255,255,255));
				
				msg.setBackground(new Color(0,0,0));
				msg.setForeground(new Color(0,255,0));
				
				message.setBackground(new Color(0,0,0));
				message.setForeground(new Color(0,255,0));
				
				onlList.setBackground(new Color(0,0,0));
				onlList.setForeground(new Color(0,255,0));
				
				online.setBackground(new Color(0,0,0));
				online.setForeground(new Color(0,255,0));
				
				pFile1.setBackground(new Color(0,0,0));
				pFile1.setForeground(new Color(0,255,0));
				
				files.setBackground(new Color(0,0,0));
				files.setForeground(new Color(0,255,0));
				
				break;
			}
			case "Pinky":{
				//Pinky theme
				p1.setBackground(new Color(255,120,120));
				p2.setBackground(new Color(255,120,120));
				p2a.setBackground(new Color(255,120,120));
				p2b.setBackground(new Color(255,120,120));
				pf.setBackground(new Color(255,120,120));
				pfa.setBackground(new Color(255,120,120));
				p3.setBackground(new Color(255,120,120));
				
				send.setBackground(new Color(255,120,120));
				
				setkey.setBackground(new Color(255,120,120));
				setkey.setForeground(new Color(0,0,0));
				
				setStyle.setBackground(new Color(255,120,120));
				setStyle.setForeground(new Color(0,0,0));
				
				logout.setBackground(new Color(255,120,120));
				
				sendFile.setBackground(new Color(255,120,120));
				
				clear.setBackground(new Color(255,120,120));
				
				mess.setForeground(new Color(0,0,0));
				
				nameLabel.setForeground(new Color(170,0,0));
				
				currentnick.setBackground(new Color(255,255,255));
				currentnick.setForeground(new Color(0,0,0));
				
				colorCombo.setBackground(new Color(255,255,255));
				colorCombo.setForeground(new Color(0,0,0));
				
				inputkey.setBackground(new Color(255,255,255));
				inputkey.setForeground(new Color(0,0,0));
				
				msg.setBackground(new Color(255,255,255));
				msg.setForeground(new Color(255,40,255));
				
				message.setBackground(new Color(255,255,255));
				message.setForeground(new Color(0,0,0));
				
				onlList.setBackground(new Color(255,255,255));
				onlList.setForeground(new Color(0,255,0));
				
				online.setBackground(new Color(255,255,255));
				online.setForeground(new Color(0,160,0));
				
				pFile1.setBackground(new Color(255,255,255));
				pFile1.setForeground(new Color(0,255,0));
				
				files.setBackground(new Color(255,255,255));
				files.setForeground(new Color(0,160,0));
				
				break;
			}
			case "Cream":{
				//Cream theme
				p1.setBackground(new Color(255,255,153));
				p2.setBackground(new Color(255,255,153));
				p2a.setBackground(new Color(255,255,153));
				p2b.setBackground(new Color(255,255,153));
				pf.setBackground(new Color(255,255,153));
				pfa.setBackground(new Color(255,255,153));
				p3.setBackground(new Color(255,255,153));
				
				send.setBackground(new Color(255,255,153));
				
				setkey.setBackground(new Color(255,255,153));
				setkey.setForeground(new Color(0,0,0));
				
				setStyle.setBackground(new Color(255,255,153));
				setStyle.setForeground(new Color(0,0,0));
				
				logout.setBackground(new Color(255,255,153));
				
				sendFile.setBackground(new Color(255,255,153));
				
				clear.setBackground(new Color(255,255,153));
				
				mess.setForeground(new Color(0,0,0));
				
				nameLabel.setForeground(new Color(200,0,0));
				 
				currentnick.setBackground(new Color(255,255,255));
				currentnick.setForeground(new Color(0,0,0));
				
				colorCombo.setBackground(new Color(255,255,255));
				colorCombo.setForeground(new Color(0,0,0));
				
				inputkey.setBackground(new Color(255,255,255));
				inputkey.setForeground(new Color(0,0,0));
				
				msg.setBackground(new Color(255,255,255));
				msg.setForeground(new Color(0,0,0));
				
				message.setBackground(new Color(255,255,255));
				message.setForeground(new Color(0,0,0));
				
				onlList.setBackground(new Color(255,255,255));
				onlList.setForeground(new Color(0,255,0));
				
				online.setBackground(new Color(255,255,255));
				online.setForeground(new Color(0,160,0));
				
				pFile1.setBackground(new Color(255,255,255));
				pFile1.setForeground(new Color(0,255,0));
				
				files.setBackground(new Color(255,255,255));
				files.setForeground(new Color(0,160,0));
				
				break;
			}
		}
	}
	
	private void LogIn(){
		try{
			//Get the user name
			if(SetNickName(nick.getText())){
				//Create a hearing thread
				ExecutorService  executorClient = Executors.newFixedThreadPool(NUM_OF_THREAD);
				add(p_chat,BorderLayout.CENTER);
				//Turn to chat UI
				p_login.setVisible(false);
				p_bgr.setVisible(false);
				//successfully login will indicate the chat panel and set the login panel invisibly
				p_chat.setVisible(true);  
				//Set the current name of user
				currentnick.setText(nick.getText()); 
				//Current name can not be modified
				currentnick.setEditable(false); 
				
				//Change the title of frame from client to user name
				this.setTitle(nick.getText()); 
				msg.append("Logged in successfully\n"); 
				//Set and create a folder path to store files
				folderPath = FileSystems.getDefault().getPath("Received files/"+name);
				folder = new File(folderPath.toString());
				if(!folder.exists()) folder.mkdirs();
				msg.append("Folder created: " + folderPath.toString() + "\n");
				//Start the hearing thread
				WorkerThreadClient handlerClient = new WorkerThreadClient(socket, is);
				executorClient.execute(handlerClient);
			}
			else{
				JOptionPane.showMessageDialog(this,"Existing user. Please use another name.","Message Dialog",JOptionPane.WARNING_MESSAGE);
				//Wrong login will indicate this
			}
		}catch (IOException | ClassNotFoundException e) {}
	}

	private boolean SetNickName(String nickName) throws IOException, ClassNotFoundException{
		Message temp_check = new Message();
		
		//Trim all the unnecessary parts
		Client.name = nickName.trim();
		if (Client.name.isEmpty()) return false;
		//Request the server to set name
		Client.os.writeObject(new Message("/setName", Client.name));
		//Read the response from the Server
		temp_check = (Message) Client.is.readObject();
		//Check if the name is already taken or not
		if (temp_check.getContent().equals("false")){
			return false;
		}
		else if (temp_check.getContent().equals("true"))
		{
			return true;
		}
		return false;
	}	

	private void SendFile(String path) throws IOException{
		//Create a file
		File myFile = new File (path);
		byte [] mybytearray  = new byte [(int)myFile.length()];
		//Send the message about the file info
		Client.os.writeObject(new Message("/sendFile", myFile.getName(), (int)myFile.length()));
		//Open file stream
		fis = new FileInputStream(myFile);
		bis = new BufferedInputStream(fis);
		//Read the file from client's computer
		bis.read(mybytearray,0,mybytearray.length);;
		System.out.println("Sending " + myFile.getName() + "(" + mybytearray.length + " bytes)");
		//Send the file to server
		os.write(mybytearray,0,mybytearray.length);
		//Flush the stream
		os.flush();
	}
}
