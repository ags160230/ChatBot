
package botdriver;

import com.google.gson.*;
import java.util.Date;

public class Weather {
    
    private JsonObject obj; 
      
    protected String city;
   
    private double tempCur;
    private double tempHigh;
    private double tempLow;
    
    // Constructor that takes json object as a parameter, parses the object,
    // and extracts the appropriate data fields
    public Weather(String response)
    {
       try
       {
            this.obj = new JsonParser().parse(response).getAsJsonObject();
           
            setTemps();
       }
       catch(Exception e)
       {
            System.out.println("ERROR OCCURRED: " + e.getMessage());
       }
    }
   
    // Method to extract appropriate data fields from json object
    public void setTemps()
    {
        JsonObject tempObj = obj.get("main").getAsJsonObject();
        
        this.city = obj.get("name").getAsString();
        this.tempCur = (tempObj.get("temp").getAsDouble() - 273);
        this.tempHigh = (tempObj.get("temp_max").getAsDouble() - 273);
        this.tempLow = (tempObj.get("temp_min").getAsDouble() - 273);
        
    }

    // Method that returns a general weather forecast for the provided city
    public String forecast()
    {
        return ("The current temperature in " + city + " is " + tempCur + "°C, "
               + "with a possible high of " + tempHigh + "°C and a possible low"
               + " of " + tempLow + " °C for " + new Date().toString() + ".");
    }
}
