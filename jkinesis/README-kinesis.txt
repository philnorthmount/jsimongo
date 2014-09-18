AWS Kinesis Streaming - fast setup and configure steps

SETUP
-----
1.) Create AWS account
2.) In AWS Create an internal AWS User with full rights
3.) Download the aws_access_key_id, aws_secret_access_key
4.) Use the template credentials file provided in the aws-java-sdk-1.8.5 in folder 
samples\AmazonKinesis and paste the access keys into this file.
5.) Save the credentials file in folder c:\users\ptyl..insert your user\.aws\
6.) Download from GIT the jsikinesis library and unzip into appropriate folder
7.) You will need have JDK 1.6 or higher - when compiling it does give a warning that code is for 1.6 but ignore
check that java -version works in the folder
8.) Make sure your class path points to the relevant folder, example given below for windows env vars, I basically
made sure the code pointed at all the aws-java-sdk jar files.

My CLASSPATH = .;C:\jkinesis\com\aws-java-sdk-1.8.5\lib\aws-java-sdk-1.8.5.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\lib\aws-java-sdk-1.8.5-javadoc.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\lib\aws-java-sdk-1.8.5-sources.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\lib\aws-java-sdk-flow-build-tools-1.8.5.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\aspectj-1.6\aspectjrt.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\aspectj-1.6\aspectjweaver.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\commons-codec-1.3\commons-codec-1.3.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\commons-logging-1.1.1\commons-logging-1.1.1.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\freemarker-2.3.18\freemarker-2.3.18.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\httpcomponents-client-4.2.3\httpclient-4.2.3.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\httpcomponents-client-4.2.3\httpcore-4.2.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\jackson-annotations-2.1\jackson-annotations-2.1.1.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\jackson-core-2.1\jackson-core-2.1.1.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\jackson-databind-2.1\jackson-databind-2.1.1.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\java-mail-1.4.3\mail-1.4.3.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\joda-time-2.2\joda-time-2.2.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\spring-3.0\spring-beans-3.0.7.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\spring-3.0\spring-context-3.0.7.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\spring-3.0\spring-core-3.0.7.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\stax-api-1.0.1\stax-api-1.0.1.jar;C:\jkinesis\com\aws-java-sdk-1.8.5\third-party\stax-ri-1.2.0\stax-1.2.0.jar


That's you done! for setup.

CONFIGURE
---------
To configure note:
1.) To compile to your folder type "javac jsiKinesis.java" and then to run "java jsikinesis"
2.) Note: just to test a run you can add -i or -d flags to the end -i = excludes inserts, -d = excludes display
"java jsikinesis -i" for example will display but will not insert, very handy for kinesis.
3.) There are a couple of other utilities you will need just to list and remove streams
a.) java AmazonKinesisList = lists all your streams you have available
b.) java AmazonKinesisDelete = deletes all your streams - amazon charges a small amount for open streams so worth doing
4.) FINALLY java AmazonKinesisGet retrieves the data from your stream  [configure your stream name in here and match with the one in jsikinesis.java]
unless just keep it as philsteststream in the meantime! Note this runs for ever while there are streams so either  CTRL - C out or run the
java AmazonKinesisDelete code and then you will see it stop. 

NOTE: This is just configured to 1 shard per stream at the moment, but not that tricky to expand. As you optimize a real
scenario more complexity starts as you get to split and merge shards on the fly depending on throughput required. 
Maybe handy for those big footy match days!!!

Just to demo - its nice to bring up 2* terminals/consoles 1 with the producer running and 1 with the consumer app. 
Also go to the AWS Console and it gives you a nice breakdown of the streaming analysis. Start the producer 1st as that creates the
stream and shard and then once streaming started, bring up the consumer.

Easy to put this via the stream into mongo using the MONGODB API.

Give me a shout if you need any help, you can always connet to me via my gmail etc...

Hope you find useful anyway.


[Apologies for the code design - only a quick alpha version ;-)]






    

