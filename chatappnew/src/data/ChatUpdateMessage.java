package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.Statement;


public class ChatUpdateMessage extends StructuredMessage {
    
    public ChatUpdateMessage(String username, String message, String recipient) {
        super(ChatMessage.CHAT_DATA_MESSAGE);
        super.dataList.add(username);
        super.dataList.add(message);
        super.dataList.add(recipient);
    }
     public ChatUpdateMessage(String username, String message) {
    	 super(ChatMessage.CHAT_DATA_MESSAGE);
         super.dataList.add(username);
         super.dataList.add(message);
         
        
         
         
         
        
    	 
     }
     
    
    public String getUsername() {
        return (String) super.getList().get(0);
    }

    public String getMessage() {
        return (String) super.getList().get(1);
    }
    public String getRecipient() {
    	return (String) super.getList().get(2);
     }

}
