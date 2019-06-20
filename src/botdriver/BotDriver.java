
package botdriver;

public class BotDriver {
    
    // The server to connect to and connection details
    static String server = "irc.freenode.net";
    
    // The channel which the bot will join
    static String channel = "#irchacks";
    
    public static void main (String[] args) throws Exception
    { 
        try
        {
            // Instantiate weather chatbot
            WeatherBot bot1 = new WeatherBot(server, channel);

            bot1.setVerbose(true);
            bot1.connect(server);
            bot1.joinChannel("#irchacks");
            
            // Instantiate ISS chatbot
            ISSBot bot2 = new ISSBot(server, channel);
            
            bot2.setVerbose(true);
            bot2.connect(server);
            bot2.joinChannel(channel);
        }
        catch(Exception e)
        {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
        }
    }
}
