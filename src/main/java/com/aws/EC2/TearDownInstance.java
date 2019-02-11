/*
 *
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
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class TearDownInstance {
	
	public static void main (String[] args) throws IOException {

		String ACCESS_KEY_ID = args[0];
		String SECRET_ACCESS_KEY = args[1];
		String INSTANCE_ID = args[2].toString();
		
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY) ;
        
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
        		.withRegion(Regions.US_EAST_1)
        	    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        	    .build();
        		System.out.println(INSTANCE_ID);
            	TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
    			terminateInstancesRequest.withInstanceIds(INSTANCE_ID);
    	        TerminateInstancesResult terminateInstancesResult = ec2.terminateInstances(terminateInstancesRequest);
    	       /* 
    	        DeleteSecurityGroupRequest del_security_request = new DeleteSecurityGroupRequest()
    	        		   .withGroupName("OpenWhiskJenkinsSecurityGroup");
                DeleteSecurityGroupResult response = ec2.deleteSecurityGroup(del_security_request);  
                */  
    	        System.out.println("Instance Terminated"); 
            }
        	
}
