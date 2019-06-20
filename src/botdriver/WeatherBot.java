
package botdriver;

import org.jibble.pircbot.*;
import java.io.*;
import java.net.*;

public class WeatherBot extends PircBot {
    
    private static String SERVER;
    private static String CHANNEL;
    private final static String NICK = "WeatherMan";
    private final static String LOGIN = "WeatherMan";
    private final static String INTRO = "Hello World! Give me the name of a "
                                        + "particular city and I can report the "
                                        + "weather forecast for today!";
    private final String API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather?";
    private final String API_KEY = "dd25fcbce60f278eb21d54c30abb2e77";
   
    // Constructor that introduces the chatbot to the chatroom upon instantiation
    public WeatherBot(String server, String channel)
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

            // Log on to the server
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
                    // Print the raw line received by the bot
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
    public String getWeatherDataByCity(String city) 
    {
        // First we need to build the url parameters using the requested city
        // and the given API key
        
        String urlParameters;
    
        try  
        {
            urlParameters = "q=" + URLEncoder.encode(city, "UTF-8") + 
                    "&APPID=" + URLEncoder.encode(this.API_KEY, "UTF-8");
            
            if(WeatherBot.executeRequest(this.API_ENDPOINT, urlParameters) != null)
            {
                // Return statement calls the method which makes the connection to the 
                // REST API with the given URL parameter
                return WeatherBot.executeRequest(this.API_ENDPOINT, urlParameters);
            }
            else
            {
                throw new CustomException();
            }
        }
        catch(Exception e) 
        {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
            return "";
        }
    }
    
    // Method to build a web request parameter for a REST API
    public String getWeatherDataByID(String id) 
    {
        // First we need to build the url parameters using the requested city
        // and the given API key
        
        String urlParameters;
    
        try  
        {
            urlParameters = "id=" + URLEncoder.encode(id, "UTF-8") + 
                    "&APPID=" + URLEncoder.encode(this.API_KEY, "UTF-8");
            
            if(WeatherBot.executeRequest(this.API_ENDPOINT, urlParameters) != null)
            {
                // Return statement calls the method which makes the connection to the 
                // REST API with the given URL parameter
                return WeatherBot.executeRequest(this.API_ENDPOINT, urlParameters);
            }
            else
            {
                throw new CustomException();
            }
        }
        catch(Exception e) 
        {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
            return "";
        }
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
    
    // Method which instantiates a Weather object with data from the parsed JSON 
    // object & reports the data to the chatroom
    public void reportWeather(String line)
    {
        try
        {
            Weather report = new Weather(getWeatherDataByCity(line));

            if(report.city == null)
            {
                throw new CustomException();
            }
            else
            {
                sendMessage("#irchacks", report.forecast());
            }
        }
        catch(CustomException e)
        {
            sendMessage("#irchacks", e.toString());
        }
    }
   
    // Overriden method that executes a function upon a new message 
    // entering the chatroom
    @Override
    public void onMessage(String channel, String sender, String login, 
             String hostname, String message) 
    {
        message = message.toLowerCase();

        // Condition to prohibit interaction between my two chatbots
        if(sender.matches("SpaceMan"))
        {
            return;
        }
 
        try
        {
            /*
            // Error checking for errorneous entries
            if(message.matches("[a-zA-Z]+") == false)
            {
                if(message.length() > 5 && message.length() < 8)
                {
                    reportWeather(message);
                }
            }
                    */
            // Error checking for erroneous entries
            if(message.length() > 35)
            {
                throw new CustomException();
            }
            else
            {
                reportWeather(message);
            }
        }
        catch(CustomException e)
        {
            sendMessage("#irchacks", e.toString());
        }
    }
    
    // Custom Exception class
    public static class CustomException extends Exception 
    {
        String str1;

        CustomException(){}

        @Override
        public String toString()
        { 
            return ("I do not recognize that city. Please try again.");
        }
    }
}