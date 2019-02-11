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
package com.aws.CF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackStatus;

public class CreateWorkerNodeStack {
	
	public static String main(String ACCESS_KEY_ID,String SECRET_ACCESS_KEY, String KEY_NAME ,Collection<String> ClusterControlPlaneSecurityGroup, Collection<String> Subnets, Collection<String> VpcId, String NodeInstanceType)throws Exception {
		
		InputStream cf_file = new FileInputStream("amazon-eks-nodegroup.json");
		String CF_TEMPLATE_LINK = convertStreamToString(cf_file);
        String stackName = "ow-jenkins-eks-worker-nodes";
        String logicalResourceName = "SampleNotificationTopic";
        String NodeGroupName = "ow-jenkins-node-group";
        String ClusterName = "ow-jenkins-eks";
        String NodeImageId = "ami-04358410d28eaab63";
    	
		String[] sg_ids =ClusterControlPlaneSecurityGroup.toArray(new String[0]);
		String[] subnets_ids = Subnets.toArray(new String[0]);
		String[] vpc_ids = VpcId.toArray(new String[0]);
		
		String sg_id = sg_ids[0];
        String subnet = subnets_ids[0]+","+subnets_ids[1]+","+subnets_ids[2];
        String vpc_id = vpc_ids[0];
        

        
        
		List<ArrayList<String>> RESOURCE_LIST = new ArrayList<>();
		 ArrayList<String> ResourceType = new ArrayList<String>();
	     ArrayList<String> LogicalResourceId = new ArrayList<String>();
	     ArrayList<String> PhysicalResourceId = new ArrayList<String>();
	     

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY) ;
        AmazonCloudFormation stackbuilder = AmazonCloudFormationClientBuilder.standard()
        	.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
            .withRegion(Regions.US_EAST_1)
            .build();

      	
        System.out.println("===========================================");
        System.out.println("Getting Started with AWS CloudFormation");
        System.out.println("===========================================\n");


        try {
            // Create a stack
            CreateStackRequest createRequest = new CreateStackRequest();
            createRequest.setStackName(stackName);

            //Feed Template
            createRequest
            .withTemplateBody(CF_TEMPLATE_LINK)
            .withParameters(new Parameter().withParameterKey("NodeGroupName").withParameterValue(NodeGroupName))
            .withParameters(new Parameter().withParameterKey("ClusterControlPlaneSecurityGroup").withParameterValue(sg_id))
            .withParameters(new Parameter().withParameterKey("KeyName").withParameterValue(KEY_NAME))
            .withParameters(new Parameter().withParameterKey("NodeImageId").withParameterValue(NodeImageId))
            .withParameters(new Parameter().withParameterKey("Subnets").withParameterValue(subnet))
            .withParameters(new Parameter().withParameterKey("VpcId").withParameterValue(vpc_id))
            .withParameters(new Parameter().withParameterKey("ClusterName").withParameterValue(ClusterName))
            .withParameters(new Parameter().withParameterKey("NodeInstanceType").withParameterValue(NodeInstanceType))
            .withCapabilities("CAPABILITY_NAMED_IAM");
            				
            System.out.println("Creating a stack called " + createRequest.getStackName() + ".");
            stackbuilder.createStack(createRequest);

            // Wait for stack to be created
            // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
            System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName));

            // Show all the stacks for this account along with the resources for each stack
            for (Stack stack : stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()) {
                System.out.println("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");
               
                DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest();
                stackResourceRequest.setStackName(stack.getStackName());
                for (StackResource resource : stackbuilder.describeStackResources(stackResourceRequest).getStackResources()) {
                    System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
                    
                    ResourceType.add(resource.getResourceType());
                    LogicalResourceId.add(resource.getLogicalResourceId());
                    PhysicalResourceId.add(resource.getPhysicalResourceId());       
           
                }
            }
            RESOURCE_LIST.add(ResourceType);
            RESOURCE_LIST.add(LogicalResourceId);
            RESOURCE_LIST.add(PhysicalResourceId);
 
            // Lookup a resource by its logical name
            DescribeStackResourcesRequest logicalNameResourceRequest = new DescribeStackResourcesRequest();
            logicalNameResourceRequest.setStackName(stackName);
            logicalNameResourceRequest.setLogicalResourceId(logicalResourceName);
            System.out.format("Looking up resource name %1$s from stack %2$s\n", logicalNameResourceRequest.getLogicalResourceId(), logicalNameResourceRequest.getStackName());
            for (StackResource resource : stackbuilder.describeStackResources(logicalNameResourceRequest).getStackResources()) {
                System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
            }
 
            
            System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName));

        } catch (AmazonCloudFormationException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS CloudFormation, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    	Collection<String> NodeInstanceRole = new ArrayList<String>();
		for (int i = 0; i < RESOURCE_LIST.get(0).size(); i++) {
		    if (RESOURCE_LIST.get(0).get(i).contentEquals("AWS::IAM::Role")) { // Or use equals() if it actually returns an Object.
		    	NodeInstanceRole.add(RESOURCE_LIST.get(2).get(i));
		    }
		}
		
	   String[] node_instance_roles = NodeInstanceRole.toArray(new String[0]);
	   String node_instance_role =node_instance_roles[0];
       return node_instance_role;
    }
 
    // Convert a stream into a single, newline separated string
    public static String convertStreamToString(InputStream in) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder stringbuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          stringbuilder.append(line + "\n");
        }
        in.close();
        return stringbuilder.toString();
      }

    // Wait for a stack to complete transitioning
    // End stack states are:
    //    CREATE_COMPLETE
    //    CREATE_FAILED
    //    DELETE_FAILED
    //    ROLLBACK_FAILED
    // OR the stack no longer exists
    public static String waitForCompletion(AmazonCloudFormation stackbuilder, String stackName) throws Exception {

        DescribeStacksRequest wait = new DescribeStacksRequest();
        wait.setStackName(stackName);
        Boolean completed = false;
        String  stackStatus = "Unknown";
        String  stackReason = "";

        System.out.print("Waiting");

        while (!completed) {
            List<Stack> stacks = stackbuilder.describeStacks(wait).getStacks();
            if (stacks.isEmpty())
            {
                completed   = true;
                stackStatus = "NO_SUCH_STACK";
                stackReason = "Stack has been deleted";
            } else {
                for (Stack stack : stacks) {
                    if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                            stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
                        completed = true;
                        stackStatus = stack.getStackStatus();
                        stackReason = stack.getStackStatusReason();
                    }
                }
            }

            // Show we are waiting
            System.out.print(".");

            // Not done yet so sleep for 10 seconds.
            if (!completed) Thread.sleep(10000);
        }

        // Show we are done
        System.out.print("done\n");

        return stackStatus + " (" + stackReason + ")";
    }
	
	
	
	}


