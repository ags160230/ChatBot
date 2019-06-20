
package botdriver;

import org.jibble.pircbot.*;
import java.io.*;
import java.net.*;

public class ISSBot extends PircBot {
    
    private static String SERVER;
    private static String CHANNEL;
    private final static String NICK = "SpaceMan";
    private final static String LOGIN = "SpaceMan";
    private final static String INTRO = "Hello World! Ask me about the location "
                                        + "of the International Space Station, "
                                        + "and I'll provide the cordinates! "
                                        + "(Enter 'iss position' when desired)";
    private final String API_ENDPOINT = "https://api.wheretheiss.at/v1/satellites/";
    private final String API_KEY = "25544";
   

    // Constructor that introduces the chatbot to the chatroom upon instantiation
    public ISSBot(String server, String channel)
    {
        this.SERVER = server;
        this.CHANNEL = channel;
        this.setName(NICK);
        this.sendMessage("#irchacks", INTRO);
    }
    
    public void main (String[] args) throws Exception
    { 
        try
        {            
            // Connect directly to the IRC server
            Socket socket = new Socket(SERVER, 6667);
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream( )));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream( )));

            // Log on to the server.
            writer.write("NICK " + NICK + "\r\n");
            writer.write("USER " + LOGIN + " 8 * : Java IRC Hacks Bot\r\n");
            writer.flush( );

            // Read lines from the server until it tells us we have connected
            String line = null;
            while ((line = reader.readLine( )) != null) 
            {
                if (line.indexOf("004") >= 0) 
                {
                    // We are now logged in
                    break;
                }
                else if (line.indexOf("433") >= 0) 
                {
                    System.out.println("Nickname is already in use.");
                    return;
                }
            }

            // Join the channel
            writer.write("JOIN " + CHANNEL + "\r\n");
            writer.flush( );

            // Keep reading lines from the server
            while ((line = reader.readLine( )) != null) 
            {
                if(line.toLowerCase( ).startsWith("PING ")) 
                {
                    // We must respond to PINGs to avoid being disconnected
                    writer.write("PONG " + line.substring(5) + "\r\n");
                    writer.write("PRIVMSG " + CHANNEL + " :I got pinged!\r\n");
                    writer.flush( );
                }
                else 
                {
                    // Print the raw line recieved by the bot
                    System.out.println(line);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
        }
    }
    
    // Method to build a web request parameter for a REST API
    public String getISSData(String line) 
    {
        // First we need to build the url parameters using the requested city
        // and the given API key
        
        String urlParameters;
    
        try 
        {
            urlParameters = URLEncoder.encode(this.API_KEY, "UTF-8");
        }
        catch(Exception e) 
        {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
            return "";
        }

        // Return statement calls the method which makes the connection to the 
        // REST API with the given URL parameter
        return WeatherBot.executeRequest(this.API_ENDPOINT, urlParameters);
    }
 
    // Method to connect to the API & request a JSON obect data
    public static String executeRequest(String targetURL, String urlParameters)
    {
        URL url;
        HttpURLConnection connection = null;  

        try 
        {
            //Create connection
            url = new URL(targetURL + urlParameters);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Get Response	
            InputStream istream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
            StringBuffer response = new StringBuffer(); 
            String line;
            
            while((line = reader.readLine()) != null) 
            {
                response.append(line);
                response.append('\r');
            }
            
            reader.close();
            
            return response.toString();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        } 
        finally 
        {
            if(connection != null) 
            {
                connection.disconnect(); 
            }
        }
    }
    
    // Method which instantiates a ISS object with data from the parsed JSON 
    // object & reports the data to the chatroom
    public void reportPosition(String line)
    {
        ISS report = new ISS(getISSData(line));
        
        sendMessage("#irchacks", report.reportPosition());
    }
    
    // Overriden method that executes a function upon a new message 
    // entering the chatroom
    @Override
    public void onMessage(String channel, String sender, String login, 
             String hostname, String message) 
    {
        // Condition to prohibit interaction between my two chatbots
        if(sender.matches("WeatherMan"))
        {
            return;
        }
        
        message = message.toLowerCase();
        
        if(message.contains("iss") && message.contains("position"))
        {
            reportPosition(message);
        }
    }
}