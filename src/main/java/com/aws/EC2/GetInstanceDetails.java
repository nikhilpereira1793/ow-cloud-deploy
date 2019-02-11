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

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class GetInstanceDetails {

	public static List getInstanceID(String ACCESS_KEY_ID, String SECRET_ACCESS_KEY ) {	

		    List<String> instanceIds = new ArrayList<String>();	    
		    BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY) ;
	        
	        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
	        		.withRegion(Regions.US_EAST_1)
	        	    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
	        	    .build();
		        
	        DescribeInstancesRequest request = new DescribeInstancesRequest();
	        List<String> tagvalue = new ArrayList<String>();
	        tagvalue.add("ow-jenkins");
	        Filter filter = new Filter("tag:ow-demo", tagvalue);

	        DescribeInstancesResult result = ec2.describeInstances(request.withFilters(filter));

	        List<Reservation> reservations = result.getReservations();

	        for (Reservation reservation : reservations) {
	            List<Instance> instances = reservation.getInstances();
	            

	            for (Instance instance : instances) {
	            	
	            	instanceIds.add(instance.getInstanceId());
	                System.out.println(instance.getInstanceId());
	            }
	            
	        }
	        if (reservations.size() == 0)
	        System.out.println("No instances found");
			return instanceIds;
		}
		
		
	
	public static void main (String[] args) {
		
		String ACCESS_KEY_ID = args[0];
		String SECRET_ACCESS_KEY = args[1];
		GetInstanceDetails.getInstanceID(ACCESS_KEY_ID,SECRET_ACCESS_KEY);
	
	}
}
