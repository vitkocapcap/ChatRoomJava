package server;

import message.*;

import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class WorkerThread extends Thread{
    public Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private String accName;
    private Message check = new Message();
    private FileOutputStream fos = null;
    private BufferedOutputStream bos = null;
    private FileInputStream fis = null;
    private BufferedInputStream bis = null;	

    public WorkerThread(Socket socket){
        this.socket = socket;
    }   

    public void run(){
        System.out.println("Processing: " + socket);
        try{
            //Initialize the Input and Output stream
			os = new ObjectOutputStream(socket.getOutputStream());
			is = new ObjectInputStream(socket.getInputStream());
            //Get nickname and add to the list
            accName = getNickName();
            Server.userList.put(accName, this);
            //Send announcement to all the list
            Server.SetAnnouncement("1", accName, "none");
            sendList("/updateFileList", Server.fileList);

            while(true){
                this.receiveCommand();
            }
        }
        catch (IOException | ClassNotFoundException e){
            System.err.println("Request processing Error: " + e);
        }
        finally{
            System.out.println("Complete processing: " + socket);
            Server.userList.remove(accName);
            try{
                //Leave group
                Server.SetAnnouncement("2", accName, "none"); 
            }
            catch (IOException | ClassNotFoundException e){
                System.err.println("Request processing Error: " + e);
            } 
        }
    }

    //Send message to client
    public void sendMsg(String type, String content) throws IOException, ClassNotFoundException{
        os.writeObject(new Message(type, content));
    }

    //Send list to client
    public void sendList(String type, ArrayList<String> list) throws IOException, ClassNotFoundException{
        os.writeObject(new Message(type, list));
    }

    //Send message to client (overloading)
    public void sendMsg(String type, String sender, String content, String time) throws IOException, ClassNotFoundException{
        os.writeObject(new Message(type, sender, content, time));
    }


    //Return the nick name from user, if the nickname is already taken, send false message to client
    public String getNickName() throws IOException , ClassNotFoundException{
    String ch = "";
    while(true){
        this.check = (Message) this.is.readObject();
        //Double check
        if(this.check.getType().equals("/setName") || this.check.getType().equals("/changeName")){
            ch = this.check.getContent();
            if (Server.userList.containsKey(ch)){
                System.err.println("Already taken");
                this.sendMsg("/successFlag","false");
            }
            else {
                System.err.println("succesful!");
                this.sendMsg("/successFlag","true");
                return ch;
            }
        }else{
            System.out.println("error, not a set name command");
        }
        }
    }

    //Receive commands from user like message, file request
    public void receiveCommand() throws IOException, ClassNotFoundException{
        //Receive messages from client
        Message msg =  new Message();
        msg = (Message) this.is.readObject();
        System.out.println("Message Type: " + msg.getType());
        //If the client want to upload a file
        if(msg.getType().equals("/sendFile")){
            this.receiveFile(msg);
        //If the client want to get a file
        }else if(msg.getType().equals("/getFile")){
            this.sendFile(msg);
        }
        //The client want to send messages
        else Server.SendMsgToAll(msg.getType(), msg.getSender(), msg.getContent());
    } 

    //Receive file from client
    public void receiveFile(Message msg) throws IOException, ClassNotFoundException{
        //Declare Variable
        int currentFileSize = 0;
        int bytesRead = 0;
        int fileSize = msg.getFileSize();
        byte [] mybytearray  = new byte [fileSize];
        //Prepare file Stream and buffer stream
        fos = new FileOutputStream(Server.folderPath.toString() + "/" + msg.getContent());
        bos = new BufferedOutputStream(fos);
        //Read the file coming from client
        while((bytesRead != -1) && (fileSize>0)){
            //Read the file
            bytesRead = is.read(mybytearray, currentFileSize, (mybytearray.length-currentFileSize));
            System.out.println("Byte: " + bytesRead);
            //Calculate the size received
            if(bytesRead >= 0) currentFileSize += bytesRead;
            fileSize -= bytesRead;
        } //Break the loop if reach EOF

        //If the server does not receive the correct file size
        if(fileSize != 0){
            System.out.println("File is not sent sucessfully!");
            sendMsg("/announcement", "File is not sent sucessfully!");
        }
        //Write file into buffer
        bos.write(mybytearray, 0 , currentFileSize);
        bos.flush();        
        System.out.println("File downloaded (" + currentFileSize + " bytes read)");
        //Send announcement when some one uploads a file
        Server.SetAnnouncement("3", accName, msg.getContent());
    }

    //Send file when user request
    public void sendFile(Message msg)throws IOException{
        try{
            //Find the requested file
            File myFile = new File (Server.folderPath.toString() + "/" + msg.getContent());
            byte [] mybytearray  = new byte [(int)myFile.length()];
            //If the file exists
            if (myFile.exists()){
                //Send the file info
                os.writeObject(new Message("/sendFile", myFile.getName(), (int)myFile.length()));
                //Create file stream and buffer for the file
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                //read from server local resource
                bis.read(mybytearray,0,mybytearray.length);;
                System.out.println("Sending " + msg.getContent() + "(" + mybytearray.length + " bytes)");
                //Send the file
                os.write(mybytearray,0,mybytearray.length);
                //Flush the stream
                os.flush();
            }else{
                System.out.println("File not found");
                //Return file size -1
                os.writeObject(new Message("/sendFile", msg.getContent(), -1));
            }
        }
        //If the file does not exist
        catch(FileNotFoundException e){}
		
    }
}
