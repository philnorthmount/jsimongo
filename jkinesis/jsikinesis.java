// Title: JSIkinesis
// Version: Alpha 0.01
// Change Notes
// 06/03/2014 Initial Alpha Versionbeing developed - once into beta changes will be recorded
// 16/09/2014 Modified from mongo version tio incoprate the write to kinesis stream technology
// 
// Dependencies include:
// JDK 1.7.xxx
// mongo-java-driver-2.11.4-javadoc 

//import com.mongodb.*;

import java.io.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.DeleteStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.ListStreamsRequest;
import com.amazonaws.services.kinesis.model.ListStreamsResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.model.Shard;
import com.amazonaws.services.kinesis.model.*;



public class jsikinesis{

    static AmazonKinesisClient kinesisClient;
    private static final Log LOG = LogFactory.getLog(AmazonKinesisSample.class);

    private static void init() throws Exception {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        kinesisClient = new AmazonKinesisClient(credentials);
    }



	public static void main(String[] args)
	{		
		try
		{

        // kinesis specific
        init();
        final String myStreamName = "philsteststream";
        final Integer myStreamSize = 1;
        int j = 0;

        // Create a stream. The number of shards determines the provisioned throughput.

        CreateStreamRequest createStreamRequest = new CreateStreamRequest();
        createStreamRequest.setStreamName(myStreamName);
        createStreamRequest.setShardCount(myStreamSize);

        // pt
        kinesisClient.createStream(createStreamRequest);

        // The stream is now being created.
        LOG.info("Creating Stream : " + myStreamName);
        waitForStreamToBecomeAvailable(myStreamName);

        // list all of my streams
        ListStreamsRequest listStreamsRequest = new ListStreamsRequest();
        listStreamsRequest.setLimit(10);
        ListStreamsResult listStreamsResult = kinesisClient.listStreams(listStreamsRequest);
        List<String> streamNames = listStreamsResult.getStreamNames();
        while (listStreamsResult.isHasMoreStreams()) {
            if (streamNames.size() > 0) {
                listStreamsRequest.setExclusiveStartStreamName(streamNames.get(streamNames.size() - 1));
            }

            listStreamsResult = kinesisClient.listStreams(listStreamsRequest);
            streamNames.addAll(listStreamsResult.getStreamNames());

        }
        LOG.info("Printing my list of streams : ");

        // print all of my streams.
        if (!streamNames.isEmpty()) {
            System.out.println("List of my streams: ");
        }
        for (int i = 0; i < streamNames.size(); i++) {
            System.out.println(streamNames.get(i));
        }
        // kinesis specific list streams end


        // Create streaming content
        //PutRecordRequest putRecordRequest = new PutRecordRequest();

        jsikinesis myobj = new jsikinesis();

        // load jsiconfig.txt
        ArrayList<String> myconfiglist = new ArrayList<String>();
        myconfiglist = myobj.loadArray("jsiconfig.txt");

        // The text uri
        // "mongodb://xyz:MyPass_XYZ@ds023288.mongolab.com:23288/sample";                
        String textUri = myconfiglist.get(0);

        // Create MongoClientURI object from which you get MongoClient obj
        //MongoClientURI uri = new MongoClientURI(textUri);

        // Connect to that uri
        //MongoClient m = new MongoClient(uri);

        // get the mongodb database named sample
        String DBname = myconfiglist.get(1);
        //DB d=m.getDB(DBname);

        // get the mongodb collection mycollection in sample
        String collectionName = myconfiglist.get(2); 
        //DBCollection collection = d.getCollection(collectionName);

        // get delay in milliseconds
        String ActDelay = myconfiglist.get(3);
        int IntActDelay = Integer.valueOf(ActDelay);


        // System Date
        Date sd = new Date();
        String sysdate = sd.toString();

        // Create a time lapsed loop that 16mins is 1 day based on mproductlist, myproductpricelist and product weighting
        //jsimongo myobj = new jsimongo();
        ArrayList<String> myproductlist = new ArrayList<String>();
        myproductlist = myobj.loadArray("prodlist.txt");

        // setup mirror of above for price array
        ArrayList<String> myproductpricelist = new ArrayList<String>();
        myproductpricelist = myobj.loadArray("prodpricelist.txt");

        // check hwa (hour ref retrieves weighting 9am = 1, 10am = 1, 11am = 1, 12am = 2 etc.. with 7am as peak of 5)
        // setup above array
        ArrayList<String> myhourweight = new ArrayList<String>();
        myhourweight = myobj.loadArray("hourweight.txt");

        // setup a date based array for day simulations
        ArrayList<String> myday = new ArrayList<String>();
        myday = myobj.loadArray("daylist.txt");

        // Setup a top level entity based array for multi entity simulations
        ArrayList<String> myentity = new ArrayList<String>();
        myentity = myobj.loadArray("entitylist.txt");

        // Random number object
        Random randomNum = new Random();

        // Total Product number currently not used
        int TotalPrd = randomNum.nextInt(300);
        TotalPrd = TotalPrd + 700;
        
        // Initialise for time period simulation loop
        int hrInd = 61;
        int hrAct = 8;
        int actHrInd = 0;
        int strToCheck = 0;
        int totalProducts = 0;
        double totalValueOfProducts = 0;

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

        // loop through days
        for(int i=0; i<myday.size(); i++)
        {

        // Re-Initialise due to time period simulation loop
        hrInd = 61;
        hrAct = 8;
        actHrInd = 0;
        strToCheck = 0;
        totalProducts = 0;
        totalValueOfProducts = 0;
        String actEntity = "";
        String simDay = "";        

        // Time period simulation loop
        for(int x = 0; x < 900; x++)
        {
            if(hrInd > 60)
            {   
                hrAct++;
                actHrInd++;
                //System.out.println("Simulated Day: "+hrAct+":00");
                hrInd = 0;

                //get hourly weight for this particular hour to allow multi selection of products                
                strToCheck = Integer.parseInt(myhourweight.get(actHrInd));                
            }
            else
            {
                hrInd++;
            };
            
            // Multiple Entity selection loop through entities
            for(int ee=0; ee<myentity.size(); ee++)
            {
                actEntity = myentity.get(ee); 

                // for loop based upon the above hourly weighting this alows for mutiple product in minute selection            
                for(int xy = 0; xy < strToCheck; xy++ )
                {
                
                    // randomly select what if a product is bought or not currently not used
                    int isPrdBought = randomNum.nextInt(2);

                    // Minute simulation
                    int isBoughtMinute = randomNum.nextInt(59);
                    String isBoughtMinPadded = String.format("%02d", isBoughtMinute);

                    // randomly sleep for 60/hourly weighting - note this should be additional feature tbc

                    // if product bought then randonly select from myproductlist
                    int prodListLength = myproductlist.size();
                    int buyProduct = randomNum.nextInt(prodListLength);

                    // get product selection index of product selected
                    String buyActProduct = myproductlist.get(buyProduct);

                    // get price of selected product
                    String buyActPriceProduct = myproductpricelist.get(buyProduct); 

                    // Increment totals for UI Information and output                
                    totalProducts++;
                    totalValueOfProducts = totalValueOfProducts + Double.valueOf(buyActPriceProduct);

                    // Create simulated time period
                    simDay = myday.get(i);
                    String simDateTime = simDay+" "+hrAct+":"+isBoughtMinPadded+":00 GMT 2014";

                    // Output randomly selected sales values to the console
                    if(showtrans == true)
                    {
                        System.out.println("AZTEC_Code : "+buyActProduct);
                        System.out.println("Qty_Indicator : -");
                        System.out.println("Quantity : 1");
                        System.out.println("STV_Amount : "+buyActPriceProduct);
                        System.out.println("STV_UOM : ml");
                        System.out.println("SalesItemTaxes : {Tax_Sign : +}");
                        System.out.println("SalesItemsTaxesValues : {Tax_Indicator : 1, Tax_Amount 0.00}");
                        System.out.println("Created_DateTime : "+simDateTime);  // was sysdate
                        System.out.println("Entity : "+actEntity);                    

                        // should also output to a log file to be added
                    };

                    // Insert JSON into MongoDB or Kinesis as appropriate
                    if(inserttrans == true)
                    {
                        LOG.info("Putting records in stream : " + myStreamName);
                        // Write 10 records to the stream
                        //for (int j = 0; j < 10; j++) {
                            try
                            {
                                j++;
                                PutRecordRequest putRecordRequest = new PutRecordRequest();
                                putRecordRequest.setStreamName(myStreamName);

                                // line below is concat version of this display of product data above, with simulated date, price and time of purchase across entity i.e:retail outlet
                                // this can all be modified in the config parameters and related text files
                                putRecordRequest.setData(ByteBuffer.wrap(String.format("testData-%d"+"AZTEC_Code: "+buyActProduct+"Qty_Indicator : -"+"Quantity : 1"+"STV_Amount : "+buyActPriceProduct+"STV_UOM : ml"+"SalesItemTaxes : {Tax_Sign : +}"+"SalesItemsTaxesValues : {Tax_Indicator : 1, Tax_Amount 0.00}"+"Created_DateTime : "+simDateTime+"Entity : "+actEntity, j).getBytes()));

                                // partition key below used to distrubuted data across shards although this simulated only configured for 1 shard at the moment
                                putRecordRequest.setPartitionKey(String.format("partitionKey-%d", j));

                                // puts the data and meta data into kinesis as the producer ready for a kinesis application to pick this up - have related java app AmazonKinesisGet that reads off this stream
                                PutRecordResult putRecordResult = kinesisClient.putRecord(putRecordRequest);
                                System.out.println("Successfully putrecord, partition key : " + putRecordRequest.getPartitionKey() + ", ShardID : " + putRecordResult.getShardId());
                                Thread.sleep(1000);
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        //}
        

                        // mongodb
                        //BasicDBObject b=new BasicDBObject();
                        //b.put("AZTEC_Code",buyActProduct);
                        //b.put("Qty_Indicator","-");
                        //b.put("Quantity","1");
                        //b.put("STV_Amount",buyActPriceProduct);
                        //b.put("STV_UOM","ml");
                        //b.append("SalesItemsTaxes", new BasicDBObject("Tax_Sign","1"));
                        //b.append("SalesItemTaxesValues", new BasicDBObject("Tax_Indicator","+").append("Tax_Amount", "0.00"));                     
                        //b.append("Created_DateTime",simDateTime);
                        //b.append("Entity", actEntity);            

                        // Insert the JSON object into the chosen collection
                        //collection.insert(b);
                    };


                 };
                // End of for loop based upon the above hourly weighting

            };
            // end of entity for loop based            

            //This should be adjusted with the original config a value of 1000 will give a 16mins run time equating
            // to 16hrs simulation per entity code per date selected
            Thread.sleep(IntActDelay);
        };
        // End of Time period simulation loop
        
        // Date of Entity simulations
        System.out.println("Simulated Date: "+simDay);

        };
        // End of loop through days

         
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}

	}
    // end of main



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



    private static void waitForStreamToBecomeAvailable(String myStreamName) {

        System.out.println("Waiting for " + myStreamName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000 * 20);
            } catch (InterruptedException e) {
                // Ignore interruption (doesn't impact stream creation)
            }
            try {
                DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
                describeStreamRequest.setStreamName(myStreamName);
                // ask for no more than 10 shards at a time -- this is an optional parameter
                describeStreamRequest.setLimit(10);
                DescribeStreamResult describeStreamResponse = kinesisClient.describeStream(describeStreamRequest);

                String streamStatus = describeStreamResponse.getStreamDescription().getStreamStatus();
                System.out.println("  - current state: " + streamStatus);
                if (streamStatus.equals("ACTIVE")) {
                    return;
                }
            } catch (AmazonServiceException ase) {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) {
                    throw ase;
                }
                throw new RuntimeException("Stream " + myStreamName + " never went active");
            }
        }
    }


    
}