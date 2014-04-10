// Title: meet 
// Version: Alpha 0.01
// Change Notes
// 02/04/2014 Initial Alpha Versionbeing developed - once into beta changes will be recorded
// 
// Dependencies include:
// JDK 1.7.xxx
// mongo-java-driver-2.11.4-javadoc 
// twitter4j-4.0.0

import com.mongodb.*;
import twitter4j.*;
import java.util.*;
import java.io.*;

public class meet{
	public static void main(String[] args)
	{		

		try
		{

        meet myobj = new meet();

        // load jsiconfig.txt
        ArrayList<String> myconfiglist = new ArrayList<String>();
        myconfiglist = myobj.loadArray("jsiconfig.txt");

        // The text uri
        // "mongodb://xyz:MyPass_XYZ@ds023288.mongolab.com:23288/sample";                
        String textUri = myconfiglist.get(0);

        // Create MongoClientURI object from which you get MongoClient obj
        MongoClientURI uri = new MongoClientURI(textUri);

        // Connect to that uri
        MongoClient m = new MongoClient(uri);

        // get the database named sample
        String DBname = myconfiglist.get(1);
        DB d=m.getDB(DBname);

        // get the collection mycollection in sample
        String collectionName = myconfiglist.get(2); 
        DBCollection collection = d.getCollection(collectionName);

        //System.out.println("Config: "+textUri+":"+DBname+":"+collectionName);


        // twitter4j
        //Twitter twitter = new TwitterFactory().getInstance();
        Twitter twitter = new TwitterFactory().getSingleton();
        User user = twitter.verifyCredentials();



        // Twitter collection of latest tweets into the home account - defaulted to latest 20 tweets//
        //////////////////////////////////////////////////////////////

        ArrayList<String> mylatesttweetslist = new ArrayList<String>();
        // get list of tweets from a user or the next tweet listed
        try
        {
            long actid = 0;
            //twitter.createFriendship(actid);

            // The factory instance is re-useable and thread safe.
            //Twitter twitter = TwitterFactory.getSingleton();
            List<Status> statuses = twitter.getHomeTimeline();
            System.out.println("Showing home timeline.");

            for (Status status : statuses) {

                //System.out.println(status.getUser().getName() + ":" +status.getText());
                // Addes timeline to an array
                String mytweets = status.getUser().getName() + ":" +status.getText();
                mylatesttweetslist.add(mytweets);

            };


        }
        catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }



        // MongoDB Insert Below //
        //////////////////////////

        // System Date
        Date sd = new Date();
        String sysdate = sd.toString();

        // Toggle the below to display and insert the transactions as required
        boolean showtrans = true;        
        boolean inserttrans = true;

        // checkArray - loads args to a text string to allow the contains function
        String checkString = "";
        for(int ck = 0; ck < args.length; ck++)
        {
            checkString = checkString + args[ck];
        };
    
        // display transactions flag on runnning jsimongo eg: java jsimongo -d  will NOT display transactions
        // insert transactions flag on runnning jsimongo eg: java jsimongo -i  will NOT insert transactions


        if(args.length > 0)
        {    
            if(checkString.contains("-d")) showtrans = false;
            if(checkString.contains("-i")) inserttrans = false;
        };

                int x = 0;
                for(String atweet : mylatesttweetslist)
                {
                    x++;
                    // Display tweets to console
                    if(showtrans == true)
                    {
                        System.out.println("tweet : "+atweet);
                        System.out.println("Created_DateTime : "+sysdate);  // was sysdate                    
                    };

                    // Insert JSON into MongoDB
                    if(inserttrans == true)
                    {
                        BasicDBObject b=new BasicDBObject();
                        System.out.println("tweet : "+atweet);
                        System.out.println("Created_DateTime : "+sysdate);  // was sysdate                    

                        // Insert the JSON object into the chosen collection
                        collection.insert(b);
                    };
                    Thread.sleep(1);
                };

                System.out.println("End, the number of tweets inserted at this time was: "+x);

    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}

	}


    // loadArray Method loads a file into an array and returns the array
    ArrayList<String> loadArray(String thefile)
    {
        //System.out.println("Load array");

        ArrayList<String> myarray = new ArrayList<String>();

        try
        {
            BufferedReader an = new BufferedReader(new FileReader(thefile));
            String line;

            while((line = an.readLine()) != null)
            {   
                myarray.add(line);                          
            }
            an.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();

        };

        return myarray;
    }

    
}