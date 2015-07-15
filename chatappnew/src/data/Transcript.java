
package data;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Transcript {
   private List<String> messages = new ArrayList<>();
    private List<String> usernames = new ArrayList<>();
   private List<String> recipient = new ArrayList<>();
    private int maxLines;

    Transcript(int maxLines) {
        this.maxLines = maxLines;
    }
    
    public String getLastUsername() {
        return usernames.get(usernames.size() -1);
    }
    
    public String getLastMessage() {
        return messages.get(messages.size() -1);
    }     public String getLastRecipient() {
    	return recipient.get(recipient.size() -1);
    }
    

    public void addEntry(String username, String message, String recipie) {
        if (usernames.size() > maxLines) {
            usernames.remove(0);
            messages.remove(0);
            recipient.remove(0);
        }
      
        usernames.add(username);
        messages.add(message);
        recipient.add(recipie);
    
        
    }
    
   
    
   
   
    
}
