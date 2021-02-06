package client;



import java.io.IOException;
import java.io.*;
import java.net.Socket;


import message.Message;


public class WorkerThreadClient extends Thread{
    public Socket socket;
    public static String check = "";
    public static String flag = "";
    private FileOutputStream fos = null;
    private BufferedOutputStream bos = null;

    private ObjectInputStream is;
    //public static String announcementFlag = "";

    public WorkerThreadClient(Socket socket, ObjectInputStream is){
        this.socket = socket;
        this.is = is;
    }  

    
    public void run(){
        //System.out.println("Client Thread is running: " + socket);
        try{
            System.out.println(Client.name);
            while(true){
                Message message = (Message) is.readObject();
                //nameCheck ="";
                //Check if the data is a message or an announcement
                if(message.getType().equals("/announcement")){
                    System.out.println("Announcement from Server: " + message.getContent() + message.getTime());
                    Client.msg.append("Announcement from Server: " + message.getContent() + message.getTime() + "\n");
                }
                //If the server wants to update account list
                else if(message.getType().equals("/updateNameList")){
                    //Reset the text
                    Client.online.setText(null);
                    //Reset the list
                    Client.userList.clear();
                    //Get new list
                    Client.userList = message.getList();
                    //Append all the user on window
                    for(String a : Client.userList){
                        Client.online.append(a + "\n");
                     }
                }
                //If the server wants to update the file list
                else if(message.getType().equals("/updateFileList")){
                    //Reset the text
                    Client.files.setText(null);
                    //Reset the list
                    Client.fileList = message.getList();
                    //Update the file list on window
                    for(String a : Client.fileList){
                        //ClickableText clickabletext = new ClickableText(a);
                        //Client.jfiles.add(clickabletext);
                        Client.files.append(a + "\n");
                     }
                }
                //If the 
                else if(message.getType().equals("/sendFile")){
                    if(message.getFileSize() == -1){
                        Client.msg.append("File not found\n");
                    }
                    else if(message.getFileSize() >= 0) {
                        Client.msg.append("Receiving file\n");
                        if(receiveFile(message)) Client.msg.append("Received sucessfully\n");
                        else Client.msg.append("Received file failed!\n");
                    }
                }
                //read the message from other clients
                else if(message.getType().equals("/msg0")) {
                    String nickname,time,msg;
                    //Read the messages coming from the server including nickname, time and message correspondingly
                    nickname = message.getSender();
                    time = message.getTime();
                    msg = message.getContent();
                    if (nickname.equals(Client.name)){
                    	Client.msg.append("(" + time + ") Me: " + msg);
                        System.out.println("(" + time + ") Me: " + msg);
                    }
                    else {
                    	Client.msg.append("(" + time + ") " + nickname+": " + msg);
                        System.out.println("(" + time + ") "+ nickname+": " + msg);
                    }
                }
                else if(message.getType().equals("/msg")) {
                    String nickname,time,msg;
                    //Read the messages coming from the server including nickname, time and message correspondingly
                    nickname = message.getSender();
                    time = message.getTime();
                    msg = message.getContent();
                    
                    if (nickname.equals(Client.name)){
                        String decrypted = decrypt(msg,Client.keyShift);
                        Client.msg.append("(" + time + ") Me: " + decrypted);
                        System.out.println("(" + time + ") Me: " + msg);
                    }
                    else {
                    	if(Client.keyShift!=0) {
                    		String decrypted = decrypt(msg,Client.keyShift);
                    		Client.msg.append("(" + time +") " + nickname + ": " + decrypted);
                    	}
                    	else {
                    		Client.msg.append("(" + time +") " + nickname + ": " + msg);
                        	System.out.println("(" + time +") " + nickname + ": " + msg);
                    	}
                    }
                }
                //If the server sends some commands
            }
        }
        catch (IOException | ClassNotFoundException e){
            System.err.println("Request processing Error: " + e);
        }
        System.out.println("Complete processing: " + socket);
    }

    /*
     * Receive the file from server
     * @param String 
     * @return void
     */
    public boolean receiveFile(Message msg) throws IOException{
        //Declare Variable
        int currentFileSize = 0;
        int bytesRead = 0;
        int fileSize = msg.getFileSize();
        byte [] mybytearray  = new byte [fileSize];
        //Prepare file Stream and buffer stream
        fos = new FileOutputStream(Client.folderPath.toString() + "/" + msg.getContent());
        bos = new BufferedOutputStream(fos);
        //Read the file coming from client
        while((bytesRead != -1) && (fileSize>0)){
            //Read the file
            bytesRead = is.read(mybytearray, currentFileSize, (mybytearray.length-currentFileSize));
            //System.out.println("Byte: " + bytesRead);
            //Calculate the size received
            if(bytesRead >= 0) currentFileSize += bytesRead;
            fileSize -= bytesRead;
            //System.out.println("Byte left: " + fileSize);
        } //Break the loop if reach EOF

        if(fileSize != 0){
            Client.msg.append("File is not sent sucessfully!");
            return false;
        }
        //Write file into buffer
        bos.write(mybytearray, 0 , currentFileSize);
        bos.flush();
        Client.msg.append("File downloaded (" + currentFileSize + " bytes read)\n");
        return true;
    }

    public static String decrypt(String encmess,int keyshift) {
    	
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
					char c= (char)(ch-keyshift);
					if (c<'a') {
						cipherText+= (char)(ch+(26-keyshift));
					}
					else {
						cipherText+=c;
				}
			}
			else if(Character.isUpperCase(ch)) {
				char c= (char)(ch-keyshift);
				if (c<'A') {
					cipherText+= (char)(ch+(26-keyshift));
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
		return cipherText;
	}
}