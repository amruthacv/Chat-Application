package data;


import static org.junit.Assert.assertNotNull;


import java.io.IOException;
import java.util.*;
import java.sql.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import org.junit.Test;





import javax.websocket.*;
import javax.websocket.server.*;




@ServerEndpoint(value = "/chat-server",
        subprotocols={"chat"},
        decoders = {ChatDecoder.class},
        encoders = {ChatEncoder.class},
        configurator=ChatServerConfigurator.class)
public class ChatServer {
    private static String USERNAME_KEY = "username";
    private static String USERNAMES_KEY = "usernames";
    private Session session;
    private ServerEndpointConfig endpointConfig;
    private Transcript transcript;
    static Logger log = Logger.getLogger(ChatServer.class.getName());
    
    @OnOpen
    public void startChatChannel(EndpointConfig config, Session session) {
        this.endpointConfig = (ServerEndpointConfig) config;
        ChatServerConfigurator csc = (ChatServerConfigurator) endpointConfig.getConfigurator();
        this.transcript = csc.getTranscript();
        this.session = session;
    }

    @OnMessage
    public void handleChatMessage(ChatMessage message) {
        switch (message.getType()){
            case NewUserMessage.USERNAME_MESSAGE:
               this.processNewUser((NewUserMessage) message);
               break;
            case ChatMessage.CHAT_DATA_MESSAGE:
                this.processChatUpdate((ChatUpdateMessage) message);
                break;
            case ChatMessage.SIGNOFF_REQUEST:
                this.processSignoffRequest((UserSignoffMessage) message);
        }
    }
    
    @OnError
    public void myError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }
    
    @OnClose
    public void endChatChannel() {
        if (this.getCurrentUsername() != null) {
         this.addMessage(" just left...without even signing out !","gc");
            this.removeUser();
        }
    }

    void processNewUser(NewUserMessage message) {
        String newUsername = this.validateUsername(message.getUsername());
        
        log.info("Hello this is an info message");
        
        NewUserMessage uMessage = new NewUserMessage(newUsername);
        try {
            session.getBasicRemote().sendObject(uMessage);
        } catch (IOException | EncodeException ioe) {
            System.out.println("Error signing " + message.getUsername() + " into chat : " + ioe.getMessage());
        } 
        this.registerUser(newUsername);
        this.broadcastUserListUpdate();
      this.addMessage(" just joined.","gc");
    }
    
    @Test
    public void test() {
 		try{
 		// TODO Auto-generated method stub
     ChatUpdateMessage m = null;
 		assertNotNull(m.getUsername());
 		 log.info("username not null");
 		
 		}
 		 catch (NullPointerException t)
         {}
 		

          // assertEquals("username",un);
       
 	}

    void processChatUpdate(ChatUpdateMessage message) {
    	String user;
    	String chat1;
    	String reci;
    	user=message.getUsername();
    	chat1=message.getMessage();
    	reci=message.getRecipient();
 
    	
    	 try{
             Class.forName("com.mysql.jdbc.Driver");}
            catch (ClassNotFoundException t)
            {}
         
         try {
         	
             String url = "jdbc:mysql://localhost:3306/test"; 
             Connection conn = DriverManager.getConnection(url,"root","root"); 
             Statement st = conn.createStatement(); 
             st.executeUpdate("INSERT INTO chatlog " +  "VALUES ( NULL,'"+user+"','"+chat1+"','"+reci+"')"); 
             //st.executeUpdate("INSERT INTO username " + 
               //  "VALUES ( 'McBeal','Boston')"); 
            
       
             conn.close(); 
         }catch (Exception e) { 
        	 log.info("Got an exception");
             System.err.println("Got an exception! "); 
             System.err.println(e.getMessage()); 
         } 
    	
    	
        this.addMessage(message.getMessage(),message.getRecipient());
    }

    void processSignoffRequest(UserSignoffMessage drm) {
       this.addMessage(" just left.","gc");
        this.removeUser();   
    }
    
    private String getCurrentUsername() {
        return (String) session.getUserProperties().get(USERNAME_KEY);
    }
    
    private void registerUser(String username) {
        session.getUserProperties().put(USERNAME_KEY, username);
        this.updateUserList();
    }
    
    private void updateUserList() {
        List<String> usernames = new ArrayList<>();
        for (Session s : session.getOpenSessions()) {
            String uname = (String) s.getUserProperties().get(USERNAME_KEY);
            usernames.add(uname);
        }
        this.endpointConfig.getUserProperties().put(USERNAMES_KEY, usernames);
    }
    
    private List<String> getUserList() {
        List<String> userList = (List<String>) this.endpointConfig.getUserProperties().get(USERNAMES_KEY);
        return (userList == null) ? new ArrayList<String>() : userList;
    }

    
    private String validateUsername(String newUsername) {
    	
        if (this.getUserList().contains(newUsername)) {
            return this.validateUsername(newUsername + "1");
        }
        
        
        return newUsername;
    }

    private void broadcastUserListUpdate() {
        UserListUpdateMessage ulum = new UserListUpdateMessage(this.getUserList());
        for (Session nextSession : session.getOpenSessions()) {
            try {
                nextSession.getBasicRemote().sendObject(ulum);
            } catch (IOException | EncodeException ex) {
                System.out.println("Error updating a client : " + ex.getMessage());
            }
        }
    }

    private void removeUser() {
        try {
            this.updateUserList();
            this.broadcastUserListUpdate();
            this.session.getUserProperties().remove(USERNAME_KEY);
            this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User logged off"));
        } catch (IOException e) {
            System.out.println("Error removing user");
        }
    }

    private void broadcastTranscriptUpdate() {
    	String temp;
    	
    	
    	if (this.transcript.getLastRecipient().equals("gc")) {
    		for (Session nextSession : session.getOpenSessions()) {
                ChatUpdateMessage cdm = new ChatUpdateMessage(this.transcript.getLastUsername(), this.transcript.getLastMessage());	
                try {
                    nextSession.getBasicRemote().sendObject(cdm);
                } catch (IOException | EncodeException ex) {
                    System.out.println("Error updating a client : " + ex.getMessage());
                }
    		
    		}
    	}
    	
    	else {
        for (Session nextSession : session.getOpenSessions()) {
        
        	 	
            ChatUpdateMessage cdm = new ChatUpdateMessage(this.transcript.getLastUsername(), this.transcript.getLastMessage());
            temp = (String) nextSession.getUserProperties().get(USERNAME_KEY);
         if (temp.equals(this.transcript.getLastRecipient()) )
         {
        	 
        	 
            try {
                nextSession.getBasicRemote().sendObject(cdm);
            } catch (IOException | EncodeException ex) {
                System.out.println("Error updating a client : " + ex.getMessage());
            }
        }
        
        }
    	}
    }

   private void broadcastTranscriptLog() {
      
        
        	try{
                Class.forName("com.mysql.jdbc.Driver");}
               catch (ClassNotFoundException t)
               {}
            
            try {
            	
                String url = "jdbc:mysql://localhost:3306/test"; 
                Connection conn = DriverManager.getConnection(url,"root","root"); 
                java.sql.Statement st = conn.createStatement(); 
              //  st.executeUpdate("INSERT INTO chatlog " +  "VALUES ( NULL,'"+user+"','"+chat1+"','"+reci+"')"); 
                //st.executeUpdate("INSERT INTO username " + 
                  //  "VALUES ( 'McBeal','Boston')"); 
               
                
               String sql = "SELECT chat,uname FROM chatlog where uname='"+getCurrentUsername()+"'";
               ResultSet rs = st.executeQuery(sql);
               
               
                
                while(rs.next()){
                  
                    String chat = rs.getString("chat");
                   String uname = rs.getString("uname");
                 
                   
                
                	   ChatUpdateMessage cdm1 = new ChatUpdateMessage(uname,chat);	
                	   try {
                           this.session.getBasicRemote().sendObject(cdm1);
                       } catch (IOException | EncodeException ex) {
                           System.out.println("Error updating a client : " + ex.getMessage());
                       }
                	  
                	   
                   }
                      
                       
                	   
                
                
                rs.close();
                conn.close(); 
            }catch (Exception e) { 
           	 log.info("Got an exception");
                System.err.println("Got an exception! "); 
                System.err.println(e.getMessage()); 
            } 
        	
        	
            
            
        }
   // }
  
    
    private void addMessage(String message, String recipi) {
        this.transcript.addEntry(this.getCurrentUsername(), message, recipi);
  
       
        
      this.broadcastTranscriptLog();
        this.broadcastTranscriptUpdate();
    }
    
}
