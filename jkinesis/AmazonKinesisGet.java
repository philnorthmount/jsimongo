/*
 * Copyright 2012-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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



public class AmazonKinesisGet {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WANRNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

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

    public static void main(String[] args) throws Exception {
        init();

        final String myStreamName = "philsteststream";
        final Integer myStreamSize = 1;

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

        //System.out.println(streamNames.get(0));
        String myownstream = streamNames.get(0);



        // Retrieve the Shards from a Stream
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
        describeStreamRequest.setStreamName(myownstream);
        DescribeStreamResult describeStreamResult;
        List<Shard> shards = new ArrayList<>();
        String lastShardId = null;

        do {
            describeStreamRequest.setExclusiveStartShardId(lastShardId);
            describeStreamResult = kinesisClient.describeStream(describeStreamRequest);
            shards.addAll(describeStreamResult.getStreamDescription().getShards());
            if (shards.size() > 0) {
                lastShardId = shards.get(shards.size() - 1).getShardId();
            }
        } while (describeStreamResult.getStreamDescription().getHasMoreShards());

        // Get Data from the Shards in a Stream
        // Hard-coded to use only 1 shard
        String shardIterator;
        GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
        getShardIteratorRequest.setStreamName(myownstream);
        //get(0) shows hardcoded to 1 stream
        getShardIteratorRequest.setShardId(shards.get(0).getShardId());
        // using TRIM_HORIZON but could use alternatives
        getShardIteratorRequest.setShardIteratorType("TRIM_HORIZON");

        GetShardIteratorResult getShardIteratorResult = kinesisClient.getShardIterator(getShardIteratorRequest);
        shardIterator = getShardIteratorResult.getShardIterator();

        // Continuously read data records from shard.
        List<Record> records;
    
        while (true) {
            // Create new GetRecordsRequest with existing shardIterator.
            // Set maximum records to return to 1000.
            
            GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
            getRecordsRequest.setShardIterator(shardIterator);
            getRecordsRequest.setLimit(1000);

            GetRecordsResult result = kinesisClient.getRecords(getRecordsRequest);

            // Put result into record list. Result may be empty.
            records = result.getRecords();

            // Print records
            for (Record record : records) {
                ByteBuffer byteBuffer = record.getData();
                System.out.println(String.format("Seq No: %s - %s", record.getSequenceNumber(),
                        new String(byteBuffer.array())));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }

            shardIterator = result.getNextShardIterator();
        }



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
