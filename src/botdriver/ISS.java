
package botdriver;

import com.google.gson.*;

public class ISS {
    
    private JsonObject obj; 
  
    private double latitude;
    private double longitude;
    private double altitude;
    private double velocity;
    
    // Constructor that takes json object as a parameter, parses the object,
    // and extracts the appropriate data fields
    public ISS(String response)
    {
       try
       {
            this.obj = new JsonParser().parse(response).getAsJsonObject();
            
            setCoord();
       }
       catch(Exception e)
       {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
       }
    }
   
    // Method to extract appropriate data fields from json object
    public void setCoord()
    {
        this.latitude= obj.get("latitude").getAsDouble();
        this.longitude = obj.get("longitude").getAsDouble();
        this.altitude = obj.get("altitude").getAsDouble();
        this.velocity = obj.get("velocity").getAsDouble();
    }

    // Method that returns a general weather forecast for the provided city
    public String reportPosition()
    {
        return ("The current position of the International Space Station is " 
               + latitude + "° (latitude) and " + longitude + "° (longitude), "
               + "traveling at a speed of " + velocity + " mph at an alitude of "
               + altitude + " kilometers above sea level.");
    }
}

