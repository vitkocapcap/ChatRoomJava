package message;


import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable{
    private static final long serialVersionUID = 1L;
    private String sender   ="";
    private String type     ="";
    private String content  ="";
    private String time     ="";
    private int size = 0;
    private ArrayList<String> userList; 
    
    public Message(){}

    public Message(String type, ArrayList<String> userList){
        //this.userList.add("Empty");
        this.type = type;
        this.userList = new ArrayList<String>(userList);
    }
    

    public Message(String type, String sender, String content, String time){
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.time = time;
    }

    public Message(String type, String sender, String content){
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    public Message(String type, String content){
        this.type = type;
        this.content = content;
    }
    public Message(String type, String content, int size){
        this.type = type;
        this.content = content;
        this.size = size;
    }

    public Message(String type){
        this.type = type;
    }




    public void setSender(String sender){
        this.sender = sender;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setContent(String content){
        this.content = content;
    }


    public String getSender(){
        return this.sender;
    }

    public int getFileSize(){
        return this.size;
    }

    public String getTime(){
        return this.time;
    }


    public String getType(){
        return this.type;
    }

    public String messageType(){
        return this.type;
    }

    public ArrayList<String> getList(){
        return this.userList;
    }

    public String getContent(){
        return this.content;
    }






}