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
package com.aws.EKS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.eks.AmazonEKS;
import com.amazonaws.services.eks.AmazonEKSClientBuilder;
import com.amazonaws.services.eks.model.CreateClusterRequest;
import com.amazonaws.services.eks.model.DescribeClusterRequest;
import com.amazonaws.services.eks.model.VpcConfigRequest;
import com.aws.CF.CreateVpcStack;
import com.aws.CF.CreateWorkerNodeStack;

public class CreateEksCluster {
	
	public static List createEksCluster(String ACCESS_KEY_ID,String SECRET_ACCESS_KEY,String ROLE_ARN,Collection<String> subnets,Collection<String> security_ids)throws Exception {
	
        String clusterName = "ow-jenkins-eks";
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY) ;
        AmazonEKS clusterbuilder = AmazonEKSClientBuilder.standard()
        		.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.US_EAST_1)
                .build();
        
        System.out.println("===========================================");
        System.out.println("Getting Started with EKS Cluster");
        System.out.println("===========================================\n");
        		
        VpcConfigRequest vpc_request = new VpcConfigRequest();
        vpc_request.setSecurityGroupIds(security_ids);
        vpc_request.setSubnetIds(subnets);
   		
        //Create EKS Cluster
        CreateClusterRequest createClusterRequest = new CreateClusterRequest()
        		.withName(clusterName)
        		.withRoleArn(ROLE_ARN.toString())
        		.withVersion("1.10")
        		.withResourcesVpcConfig(vpc_request);
          clusterbuilder.createCluster(createClusterRequest);   
	      System.out.println("Stack creation completed, the stack " + clusterName + " completed with " + waitForCompletion(clusterbuilder, clusterName));
	      return null;
	}
	
	public static void main (String[] args) throws Exception {

		String ACCESS_KEY_ID = args[0];
		String SECRET_ACCESS_KEY = args[1];
		String KEY_NAME = args[2];
		String ROLE_ARN = args[3];
		String NodeInstanceType = args[4];

		//Create VPC Stack
		CreateVpcStack cf_stack = new CreateVpcStack();
		List<ArrayList<String>> RESOURCE_LIST = CreateVpcStack.main(ACCESS_KEY_ID,SECRET_ACCESS_KEY);
		
		//Get Subnets	
		Collection<String> subnets = new ArrayList<String>();
		for (int i = 0; i < RESOURCE_LIST.get(0).size(); i++) {
		    if (RESOURCE_LIST.get(0).get(i).contentEquals("AWS::EC2::Subnet")) {
		    	subnets.add(RESOURCE_LIST.get(2).get(i));
		    }
		}
		//Get Security IDs
		Collection<String> security_ids = new ArrayList<String>();
		for (int i = 0; i < RESOURCE_LIST.get(0).size(); i++) {
		    if (RESOURCE_LIST.get(0).get(i).contentEquals("AWS::EC2::SecurityGroup")) {
		    	security_ids.add(RESOURCE_LIST.get(2).get(i));
		    }
		}
		//Get VPC ID
		Collection<String> vpc_ids = new ArrayList<String>();
		for (int i = 0; i < RESOURCE_LIST.get(0).size(); i++) {
		    if (RESOURCE_LIST.get(0).get(i).contentEquals("AWS::EC2::VPC")) { // Or use equals() if it actually returns an Object.
		    	vpc_ids.add(RESOURCE_LIST.get(2).get(i));
		    }
		}
	

	System.out.print("Creating Eks Cluster");
	 CreateEksCluster.createEksCluster(ACCESS_KEY_ID,SECRET_ACCESS_KEY,ROLE_ARN,subnets,security_ids);

	 System.out.print("Creating Worker Node Stack");	
	 String node_instance_role = CreateWorkerNodeStack.main(ACCESS_KEY_ID, SECRET_ACCESS_KEY, KEY_NAME,security_ids, subnets, vpc_ids, NodeInstanceType);
	 String[] resulta = ROLE_ARN.split("\\/eksServiceRole");
	 String resultc = resulta[0]+ "\\/" + node_instance_role;
     System.out.println(resultc);

	}
	
    // Wait for a cluster to be up and running
 
    public static String waitForCompletion(AmazonEKS clusterbuilder, String clusterName) throws Exception {

    	String endpoint = null;
    	DescribeClusterRequest describeClusterRequest = new DescribeClusterRequest();
    	describeClusterRequest.setName(clusterName); 	
    	Boolean completed = false;
    	while (!completed)
    	{
    		  endpoint = clusterbuilder.describeCluster(describeClusterRequest).getCluster().getEndpoint(); 
    		  
    		if(endpoint != null && !endpoint.trim().isEmpty())
    		{
    			completed   = true;
    		}
    		else {
    			completed   = false;		
    		}
    		 // Show we are waiting
            System.out.print(".");
            // Not done yet so sleep for 10 seconds.
            if (!completed) Thread.sleep(10000);
    	}
 					
    	System.out.print(endpoint);
		return endpoint;
		

    }
}

