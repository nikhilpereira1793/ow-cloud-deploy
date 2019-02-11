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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.eks.AmazonEKS;
import com.amazonaws.services.eks.AmazonEKSClientBuilder;

import com.amazonaws.services.eks.model.DeleteClusterRequest;
import com.aws.CF.DeleteCouldFormationStack;


public class DeleteEksCluster {

	public static String deleteEKSCluster(String ACCESS_KEY_ID,String SECRET_ACCESS_KEY)throws Exception {
		
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY) ;
        
        AmazonEKS clusterbuilder = AmazonEKSClientBuilder.standard()
        		.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.US_EAST_1)
                .build();
        
        DeleteClusterRequest deleteRequest = new DeleteClusterRequest();
        deleteRequest.setName("ow-jenkins-eks");
        
        System.out.println("Deleting the stack called " + deleteRequest.getName() + ".");
        clusterbuilder.deleteCluster(deleteRequest);
        Thread.sleep(180000);
   
        System.out.println("Stack deletion completed, the stack ");
		return "Cluster Deleted";

	}
	
	public static void main (String[] args)throws Exception{	
		String ACCESS_KEY_ID = args[0];
		String SECRET_ACCESS_KEY = args[1];
		String vpc_stack = "ow-eks-vpc";
		String worker_nodes = "ow-jenkins-eks-worker-nodes";
		
		DeleteEksCluster.deleteEKSCluster(ACCESS_KEY_ID, SECRET_ACCESS_KEY);
		DeleteCouldFormationStack.main(ACCESS_KEY_ID, SECRET_ACCESS_KEY, worker_nodes);
		DeleteCouldFormationStack.main(ACCESS_KEY_ID, SECRET_ACCESS_KEY, vpc_stack);
		
		
		
	}
}
