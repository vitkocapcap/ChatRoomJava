package server;

import java.io.IOException;
import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Enumeration;
import java.util.Hashtable;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

//File library
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Server  extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	//GUI
    private JButton close,setThread;
    public static JTextArea user;
    public static JTextField threadIndicator, currentThread;
    public static JPanel PState;
    private static boolean threadFlag=false;

    //Variable Declaration 
    public static int NUM_OF_THREAD = 0;
    public static final int SERVER_PORT = 7;
    
    //Path to resource folder
    public static Path folderPath;
    public static File folder;
    public static File[] listOfFiles;
    public static FileInputStream fIn = null;
    public static FileOutputStream fOut = null;

    private static Server server;

    //Create a list of account
    public static Hashtable<String, WorkerThread> userList;
    public static ArrayList<String> nameList;
    public static ArrayList<String> fileList;

    //Get time with suitable format
    public static LocalTime time = LocalTime.now();
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static boolean announcementFlag = true;
    public static ServerSocket serverSocket; 
    public static int i=0;

    //Image
    ImageIcon imgserver = new ImageIcon("resources/server.png");


    
    //Server constructor
    public Server()  {
        super("Server");	
		this.setIconImage(imgserver.getImage());
        System.out.println("Creating Server...");
        this.prepareGUI();
    }

    //GUI setting
    //This function is responsible for all the GUI settings of Server
    private void prepareGUI(){
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
                if(serverSocket != null){
                    try {
                        serverSocket.close();
                        System.exit(0);
                    } catch (IOException e1) {
                        user.append("Can not stop server.\n");
                    }
                }
			}	
		});
		setSize(400, 400);
		setLocation(200,200);
        setLayout(new BorderLayout());
        JLabel state = new JLabel("Server state:      ");
        //Create PState general layout
        PState = new JPanel();
        PState.setLayout(new FlowLayout(FlowLayout.CENTER));
        //Set number of client panel
        setThread = new JButton("SET NUMBER OF CLIENT");
        setThread.addActionListener(this);
        
        //Area to input the client number
        currentThread = new JTextField(2);
        currentThread.setBackground(Color.WHITE);
        currentThread.setForeground(Color.BLACK);
        
        //Area to display the client number
        threadIndicator = new JTextField("",2);
        threadIndicator.setBackground(Color.BLACK);
        threadIndicator.setForeground(Color.YELLOW);
        threadIndicator.setEditable(false);
            
        //Add components to PState layout
        PState.add(state);
        PState.add(setThread);
        PState.add(currentThread);
        PState.add(threadIndicator);
        
        //Add PState to the North layout
		add(PState,BorderLayout.NORTH);
		add(new JPanel(),BorderLayout.EAST);
		add(new JPanel(),BorderLayout.WEST);
        
        //Set up the scrollbar 
		user = new JTextArea(10,20);
		DefaultCaret caret = (DefaultCaret)user.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		user.setEditable(false);
		user.append("Server has opened.\n");
		
		//Close button
		close = new JButton("Close Server");
		close.addActionListener(this);
		
		
		add(new JScrollPane(user),BorderLayout.CENTER);	
		add(close,BorderLayout.SOUTH);
		setVisible(true);
    }
    

    private void serverStart(){
        //Initialize the thread pool
    	ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
        //Create a server socket
        try{
            //Change to running mode
        	remove(setThread);
            remove(currentThread);
            //Connect to server port
            System.out.println("Binding to port " + SERVER_PORT);
            serverSocket = new ServerSocket(SERVER_PORT);
            //Wait for clients
            System.out.println("Server starts at " + serverSocket);
            user.append("Waiting for a client...\n");
            System.out.println("Waiting for a client");
            //Create a hash table of users
            userList = new Hashtable<String, WorkerThread>();
            //Create a array of files and name
            fileList = new ArrayList<String>();
            nameList = new ArrayList<String>();
            //Create a resource folder with path
            folderPath = FileSystems.getDefault().getPath("Server files");
            folder = new File(folderPath.toString());
            if(!folder.exists()) folder.mkdirs();
            //Update File list in resource folder
            Server.updateFileList();           

            while(true){
                    //Accept new client
                    Socket socket = serverSocket.accept();
                    System.out.println("Client accepted " + socket);
                    //Open and execute a new thread for client
                    WorkerThread handler = new WorkerThread(socket);
                    executor.execute(handler);
           
            }   
            
             
             
        }
        catch(IOException | ClassNotFoundException e1){
            e1.printStackTrace();
            user.append("Server can not start properly.\n");
            JOptionPane.showMessageDialog(this,"Server has been started already.","Error",JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }
    
    //Actions performed when button is clicked
    public void actionPerformed(ActionEvent e) {
        //If the user wants to close the server
        if(e.getSource()==close){
            try {
            	if(serverSocket!=null) {
            		serverSocket.close();
            		System.exit(0);}
            	else {
            		System.exit(0);
            	}
            } catch (IOException e1) {
                user.append("Can not stop server.\n");
            }    
        }
        //If the user wants to set the number of clients
        else if(e.getSource()==setThread){
        	NUM_OF_THREAD= Integer.parseInt(currentThread.getText());
			threadIndicator.setText(currentThread.getText());
			setThread.setVisible(false);
			currentThread.setVisible(false);
        	
			user.append("\nYou have set the number of clients to "+ "[" +currentThread.getText()+ "]" + "\n");
			System.out.println(NUM_OF_THREAD);
			threadFlag=true;
		}
    }

    public static void main(String []args) throws IOException, InterruptedException{
       server = new Server();
       user.append("Setting number of clients");
       
       while(!threadFlag) {
    	   TimeUnit.MILLISECONDS.sleep(100);
    	   waiting();
    	   
       }
       user.append("\nServer created!!!\n");
       server.serverStart();
   }    

    
    //Send msg to all clients including normal message and announcement from Server
    public static void SendMsgToAll(String type, String sender, String msg) throws IOException, ClassNotFoundException{  
        Enumeration<String> e = userList.keys();
        String a;
        while(e.hasMoreElements()){
            a = (String) e.nextElement();
            userList.get(a).sendMsg(type, sender, msg, LocalTime.now().format(formatter));
        }
    }

    //Update user list when there is new user join in the group chat
    public static void updateList() throws IOException, ClassNotFoundException{  
        Enumeration<String> e = userList.keys();
        String a;
        nameList.clear();
        while(e.hasMoreElements()){
            a = (String) e.nextElement();
            nameList.add(a);
        }
    }

    //Update file list when user upload new file to Server
    public static void updateFileList() throws IOException, ClassNotFoundException{  
        fileList.clear();
        listOfFiles = folder.listFiles();
        for (File tempFile : listOfFiles) {
              fileList.add(tempFile.getName());
        }
    }


    //Send the list (User list or file list) to all clients
    public static void sendListToAll(String cmd, ArrayList<String> tempList) throws IOException, ClassNotFoundException{  
        Enumeration<String> e = userList.keys();
        String a;
        while(e.hasMoreElements()){
            a = (String) e.nextElement();
            userList.get(a).sendList(cmd, tempList);
        }
    }
    
    //Send announcement from server to all clients
    public static void SetAnnouncement(String cmd, String nickName, String msg) throws IOException, ClassNotFoundException{  
        //Invoking announcement for client
        switch(cmd){
            //Announcement when someone joins in the group chat
            case "1":{
                System.out.println(nickName + " has entered the group chat");
                SendMsgToAll("/announcement", nickName, nickName +" has entered the group chat at ");
                Server.user.append(nickName +" has entered the group chat\n");
                //Update user list first
                Server.updateList();
                Server.sendListToAll("/updateNameList", nameList);
                break;
            }
            //Announcement when someone leaves in the group chat
            case "2":{
                System.out.println(nickName + " has left the group chat");
                SendMsgToAll("/announcement", nickName, nickName +" has left the group chat at ");
                Server.user.append(nickName +" has left the group chat\n");
                //Update user list first
                Server.updateList();
                Server.sendListToAll("/updateNameList", nameList);
                break;
            }
            //Announcement when someone upload a file
            case "3":{
                SendMsgToAll("/announcement", nickName, nickName + " has uploaded a file: " + msg + " at ");
                //Update file list first
                Server.updateFileList();
                //Update the list for all clients
                Server.sendListToAll("/updateFileList",fileList);
                break;
            }
        }
    }

    //Waiting function
    public static void waiting() throws InterruptedException {
    	if(i==0) {
 		   user.append("\n      *  *  *  *       *  *  *  *");
 		   i++;
 	   }
 	   else if(i==1) {
 		   user.append("\n   *              *   *              *");
 		   i++;
 	   }
 	   else if(i==2) {
 		   user.append("\n    *               *                *");
 		   i++;
 	   }
 	   else if(i==3) {
 		   user.append("\n       *                           *");
 		   i++;
 	   }
 	   else if(i==4) {
 		   user.append("\n          *                     *");
 		   i++;
 	   }
 	   else if(i==5) {
 		   user.append("\n              *             *");
 		   i++;
 	   }
 	   else if(i==6) {
 		   user.append("\n                  *     *");
 		   i++;
 	   }
 	   else if(i==7){
 		   user.append("\n                     *");
 		   i++;
 	   }
 	   else if(i==8){
 		   user.append("\n\tLOADING");
 		   i++;
 	   }
 	   else if(i==9){
 		   user.append(".");
 		   TimeUnit.MILLISECONDS.sleep(200);
 		   user.append(".");
		   TimeUnit.MILLISECONDS.sleep(200);
		   user.append(".\n\n\n");
 		   TimeUnit.MILLISECONDS.sleep(200);
 		   i++;
 	   }

 	   else if(i==10){
 		   i=0;
 		  TimeUnit.MILLISECONDS.sleep(1000);
 	   }
    }

    
}
