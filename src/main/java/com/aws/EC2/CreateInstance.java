/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.aws.EC2;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;

/**
 * Creates an EC2 instance
 */
public class CreateInstance
{

	public static void main(String[] args) throws IOException, InterruptedException{
		final String USAGE =
	            "To run this example, supply an instance name and AMI image id\n" +
	            "Ex: CreateInstance <instance-name> <ami-image-id>\n";
		String ACCESS_KEY_ID = args[0];
		String SECRET_ACCESS_KEY = args[1];
		String REGION = args[2];
		String INSTANCE_TYPE = args[3];
		Random rand = new Random(); 
		String security_group_id = "OpenWhiskJenkinsSecurityGroup" + rand.nextInt(50); 
	   
        String name = "Testing";
	    String ami_id = "ami-0123b531fc646552f";
	        
	    //BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJE7BTQAXOCLLOQDQ", "a6qJcyG3VHGjPoozyPLuzqdbuzZiKyrLwcNgOCbx");
	    BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY) ;
        
	        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
	        		.withRegion(Regions.fromName(REGION))
	        	    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
	        	    .build();

	     // Create a new security group
	        try {
	            CreateSecurityGroupRequest securityGroupRequest =
	                new CreateSecurityGroupRequest(security_group_id,
	                "OpenWhisk Security Group");
	            ec2.createSecurityGroup(securityGroupRequest);
	        } catch (AmazonServiceException ase) {
	            // Likely this means that the group is already created, so ignore.
	            System.out.println(ase.getMessage());
	        }

	         String ipAddr = "0.0.0.0/0";

	
	        // Create a range that you would like to populate.
	        ArrayList<String> ipRanges = new ArrayList<String>();
	        ipRanges.add(ipAddr);

	        // Open up port 22 for TCP traffic to the associated IP from
	        // above (e.g. ssh traffic).
	        ArrayList<IpPermission> ipPermissions = new ArrayList<IpPermission> ();
	        IpPermission ipPermission = new IpPermission();
	        ipPermission.setIpProtocol("-1");
	        ipPermission.setFromPort(new Integer(-1));
	        ipPermission.setToPort(new Integer(-1));
	        ipPermission.setIpRanges(ipRanges);
	        ipPermissions.add(ipPermission);

	        try {
	            // Authorize the ports to the used.
	            AuthorizeSecurityGroupIngressRequest ingressRequest =
	                new AuthorizeSecurityGroupIngressRequest(
	                		security_group_id,ipPermissions);
	            ec2.authorizeSecurityGroupIngress(ingressRequest);
	        }
	        catch (AmazonServiceException ase) {
	            // Ignore because this likely means the zone has already
	            // been authorized.
	            System.out.println(ase.getMessage());
	        }
	        
	        RunInstancesRequest run_request = new RunInstancesRequest()
	            .withImageId(ami_id)
	            .withInstanceType(InstanceType.fromValue(INSTANCE_TYPE))
	            .withMaxCount(1)
	            .withMinCount(1) 
	            .withSecurityGroups(security_group_id)
	            .withKeyName("ow-jenkins");

	        RunInstancesResult run_response = ec2.runInstances(run_request);
	        String instanceId = run_response.getReservation().getInstances().get(0).getInstanceId();
	       
	 
	        CreateTagsRequest request = new CreateTagsRequest().withResources(instanceId).withTags(new Tag().withKey("ow-demo").withValue("ow-jenkins"));      
	        CreateTagsResult response = ec2.createTags(request);
	        
	        
	        CreateInstance.getPublicIP(ACCESS_KEY_ID, SECRET_ACCESS_KEY,instanceId);      
		
	}

	public static String getPublicIP(String ACCESS_KEY_ID,String SECRET_ACCESS_KEY,String instanceId) throws IOException, InterruptedException
    {
	       
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY);
        
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
        		.withRegion(Regions.US_EAST_1)
        	    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        	    .build();
        // Getting EC2 public IP
        Thread.sleep(120000);
        String publicIP = ec2.describeInstances(new DescribeInstancesRequest()
                                .withInstanceIds(instanceId))
                                .getReservations()
                                .stream()
                                .map(Reservation::getInstances)
                                .flatMap(List::stream)
                                .findFirst()
                                .map(Instance::getPublicIpAddress)
                                .orElse(null);  

        CreateInstance.isReachable(publicIP, 22, 600000);
        
        System.out.println(publicIP);
        System.out.println(instanceId);
        return publicIP;

    }
	
	private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
	    // Any Open port on other machine
	    // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
	    try {
	        try (Socket soc = new Socket()) {
	            soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
	        }
	        return true;
	    } catch (IOException ex) {
	        return false;
	    }
	}
	
	
}
