pipeline {
    agent any
    
     parameters {
             choice(
        name: 'action',
        choices: 'deployow\nteardownow',
        description: 'Either deploy OW or tear it down' )
        choice(
         name: 'deploymentType',
        choices: 'awsSingleNode\nawsEKS',
        description: 'Either deploy OW or tear it down' 
        )  
           password(name: 'aws_access_key', defaultValue: '',description:'AWS Key Value of IAM User having EKS permissions');
           password(name: 'aws_access_secret', defaultValue: '',description:'AWS Secret Value of IAM User having EKS permissions');
           string(name: 'awsKeyName', defaultValue: '',description:'AWS Key Pair Name saved in Jenkins KeyStore');
           string(name: 'role_arn', defaultValue: '',description:'AWS IAM Role having EKS permissions.Only for EKS deployments');
        }
 
 //Deployment Stages Start       
      stages { 
       //Stage -1
          stage("Select AWS Deployment Region & Instance Type") {
            steps {
                script {
                if (env.action == 'deployow' && env.deploymentType != 'awsEKS') {  
                    // Load the list into a variable
                    env.LIST1 = readFile (file: "${WORKSPACE}/src/main/java/com/aws/utils/aws-regions")
                    // Show the select input
                    env.region = input message: 'User input required', ok: 'Release!',
                    parameters: [choice(name: 'AWS_REGION', choices: env.LIST1, description: 'Which AWS Region do you want to deploy OW?')]              
                                  }
                                  else {
                                      env.region = 'us-east-1';
                                  }

						echo "Region selected: ${env.region}"
                }
                 script {
                if (env.action == 'deployow') {   
                    // Load the list into a variable
                    env.LIST2 = readFile (file: "${WORKSPACE}/src/main/java/com/aws/utils/aws-instance-types")
                    // Show the select input
                    env.instancetype = input message: 'User input required', ok: 'Release!',
                    parameters: [choice(name: 'AWS_INSTANCE_TYPE', choices: env.LIST2, description: 'Select the AWS Instance type where you want to deploy OW?')]              
                                  }
						echo "Region selected: ${env.instancetype}"
                }
                     script {
                if (env.action == 'teardownow' && env.deploymentType =='awsSingleNode' ) {  
                   instance_ids = sh (returnStdout: true, script:'sudo ./gradlew InstanceDetails -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=$instancetype -PINSTANCE_ID= -PKEY_NAME= -PROLE_ARN= --no-daemon|grep -E -o "i-.*"').trim()
         		    writeFile file: "${WORKSPACE}/src/main/java/com/aws/utils/instance_ids", text: "$instance_ids\n"
                    // Load the list into a variable${WORKSPACE}/src/main/java/com/amazonaws/utils/
                    env.LIST3 = readFile (file: "${WORKSPACE}/src/main/java/com/aws/utils/instance_ids")
                    // Show 3he select input
                    env.instanceID = input message: 'User input required', ok: 'Release!',
                    parameters: [choice(name: 'AWS_INSTANCE', choices: env.LIST3, description: 'Select the AWS instance you want to tear down')]              
                                  }
						echo "Instance selected: ${env.instance}"
                }           
            }
        }
      
  //Set up AWS EC2 instace      
        stage('Create an EC2 instance for setting up OW') {
        when {
         expression {env.action == 'deployow'}
         }
            steps {
                echo 'Setting Up AWS..'
       			// Get some code from a GitHub repository
     			//	 checkout scm 	 
         		// Run the gradle build
         		 script {  
         		if (env.deploymentType == 'awsEKS'){                
                 instance_details = sh (returnStdout: true, script:'sudo ./gradlew CreateInstance -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=t2.micro -PINSTANCE_ID= -PKEY_NAME= -PROLE_ARN= --no-daemon|grep -E -o "*i-.*|([0-9]{1,3}[\\.]){3}[0-9]{1,3}"').split()
         		 env.host = instance_details[0];	
         		 env.instanceID = instance_details[-1]	
         		}
         		else {              
                 public_ip = sh (returnStdout: true, script:'sudo ./gradlew CreateInstance -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=$instancetype -PINSTANCE_ID= -PKEY_NAME= -PROLE_ARN= --no-daemon|grep -E -o "*i-.*|([0-9]{1,3}[\\.]){3}[0-9]{1,3}"').split()
         		 env.host = public_ip[0];	
         		 env.instanceID = public_ip[-1];	               
         		}
         		}
         	     echo 'Waiting for 30 seconds before moving to next stage'
                 sleep 30 // seconds
            }
        }
 
 // Deploy OW on a Single Node EC2      
        stage('Deploy OpenWhisk on Single EC2 node') {
         when {
         expression {env.action == 'deployow' && env.deploymentType =='awsSingleNode' }
         }
            steps {             
            script {
               echo 'Deploying OpenWhisk on the Ec2 Machine'
               sshagent (credentials: ["$awsKeyName"]) {                   
                sh '''ssh -o  StrictHostKeyChecking=no ubuntu@$host "ls;
 				echo $awsKeyName;
                sudo apt-get update -y; 
                sudo apt install -y openjdk-8-jdk && echo "Intalling Java";
                sudo apt update -y;
                sudo apt-get install -y zip && echo "Installing Zip";
                sudo apt install -y docker.io && echo "Installing docker";
                sudo apt update -y;
                sudo apt install -y make && echo "Installing make";
                sudo curl -L https://github.com/docker/compose/releases/download/1.21.2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose;
                sudo chmod +x /usr/local/bin/docker-compose && echo "Installing docke-compose";
                sudo docker-compose --version;
                sudo mkdir \\$HOME/ow-deploy;
                cd \\$HOME/ow-deploy;
                sudo git clone https://github.com/apache/incubator-openwhisk-devtools.git && echo "Installing OpenWhisk";
                sudo make -C \\$HOME/ow-deploy/incubator-openwhisk-devtools/docker-compose quick-start>/dev/null >/dev/null </dev/null;"'''

              }
               echo 'OW Setup Complete. Access OW using Rest APIs or CLI. More details -->"https://github.com/apache/incubator-openwhisk/blob/master/docs/rest_api.md"'
               echo "Use this API host in your CLI or Rest APIs: \n\n==========================\n\n${host}\n\n=========================="
   }
      }	}   
                                                    
  // Deploy OW on AWS EKS
     stage('Deploy OW on EKS Cluster') {
     when {
     expression {env.action == 'deployow' && env.deploymentType == 'awsEKS'}
     }
     steps {
         //Step 1 Create EKS Cluster
     	script {                 
                cluster_details = sh (returnStdout: true, script:'sudo ./gradlew CreateEksCluster -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=$instancetype -PINSTANCE_ID= -PKEY_NAME=$awsKeyName -PROLE_ARN=$role_arn --no-daemon|grep -E -o "*vpc-.*|arn.*ow-jenkins-eks-worker-nodes-NodeInstanceRole-.*"').split(); 		               
         		env.vpcId = cluster_details[0];	
         		env.instance_role = cluster_details[-1]
         		} 
         		 echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
         		echo 'Creating VPC,Worker Nodes & EKS Cluster'
         		
             //Step-2 Setup aws-iam-authenticator & enable worker nodes to join your cluster            
            script {
               echo 'Setup aws-iam-authenticator & enable worker nodes to join your cluster'
               sshagent (credentials: ["$awsKeyName"]) {                   
                 sh '''ssh -o  StrictHostKeyChecking=no -v ubuntu@$host "ls;            
 				echo $awsKeyName;
                sudo apt-get update -y; 
                sudo apt-get install python-pip -y;
                sudo pip install awscli;
                sudo apt-get update -y;
                curl -o kubectl https://amazon-eks.s3-us-west-2.amazonaws.com/1.11.5/2018-12-06/bin/linux/amd64/kubectl;
                chmod +x kubectl;
                sudo cp "kubectl /usr/local/bin";
                sudo apt-get update -y;
                curl -o aws-iam-authenticator  https://amazon-eks.s3-us-west-2.amazonaws.com/1.11.5/2018-12-06/bin/linux/amd64/aws-iam-authenticator;
                chmod +x aws-iam-authenticator;
                sudo cp "aws-iam-authenticator /usr/local/bin";
                sudo mkdir ~/.aws;
                sudo chmod 777 ~/.aws;
                sudo echo '[default]\naws_access_key_id=$aws_access_key\naws_secret_access_key=$aws_access_secret' >> ~/.aws/credentials;
				sudo echo '[default]\nregion=us-east-1\noutput=json' >> ~/.aws/config;				
				curl -O https://amazon-eks.s3-us-west-2.amazonaws.com/cloudformation/2019-01-09/aws-auth-cm.yaml;
				sed -i 's/<ARN of instance role (not instance profile)>/$instance_role/g' aws-auth-cm.yaml;	
                sudo snap install helm --classic;
                "'''}
                 echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
                }
                
            //Step 3 Updating KubeConfig to talk to EKS
            script {
            echo 'Updating KubeConfig to talk to EKS'
            try { 
               sshagent (credentials: ["$awsKeyName"]) {
              sh '''ssh -o  StrictHostKeyChecking=no -v ubuntu@$host "
                aws eks --region us-east-1 update-kubeconfig --name ow-jenkins-eks;
                 "'''
                 sleep 180
               }
               
                   for (int i = 0; i < 10; i++) {
              sh "echo Checking Kubeconfig ${[i]}"
                 sshagent (credentials: ["$awsKeyName"]) {
             env.svc = sh (returnStdout: true,script :'''ssh -o  StrictHostKeyChecking=no ubuntu@$host "kubectl get svc|grep -oh "ClusterIP"
                "''').trim();
                if (env.svc == 'ClusterIP'){         
              echo "svc: ${svc}"
                }
                else {
                    echo 'Trying to get svc again';
                    sleep 10 // seconds
                }
          }	} 
          echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
            }
            catch (Exception ex) {
            println("There seems to be a problem fetching your SVC details")
} }
           
           //Step 4 Adding worker nodes to the cluster
           script {
           echo 'Adding worker nodes to the cluster'
           try{
               
               echo 'Deploying OpenWhisk on the Ec2 Machine'
               sshagent (credentials: ["$awsKeyName"]) {
              sh '''ssh -o  StrictHostKeyChecking=no -v ubuntu@$host "ls;
              kubectl apply -f aws-auth-cm.yaml;"'''
               }
                               for (int i = 0; i < 10; i++) {
              sh "echo Checking Kubeconfig ${[i]}"
                 sshagent (credentials: ["$awsKeyName"]) {
             env.workerNodes = sh (returnStdout: true,script :'''ssh -o  StrictHostKeyChecking=no ubuntu@$host "kubectl get nodes|grep -oh "Ready" -m1
                "''').trim();
                if (env.workerNodes == 'Ready'){         
              echo "workernodes: ${workerNodes}"
                }
                else {
                    echo 'Trying to get nodes again';
                    sleep 10 // seconds
                }
          }	}
               echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
           }
                          catch (Exception ex) {
            println("Unable to the the worker nodes.There seems to be a problem with you worked nodes")
} } 
 
  
    //Step 5 Deploying OpenWhisk
           script {
               echo 'Deploying OpenWhisk on the Ec2 Machine'
               sshagent (credentials: ["$awsKeyName"]) {
                sh '''ssh -o  StrictHostKeyChecking=no ubuntu@$host "ls;
                sudo mkdir \\$HOME/ow-deploy;
                sudo chmod 777 -R \\$HOME/ow-deploy;
                cd \\$HOME/ow-deploy;
                sudo git clone https://github.com/rahulqelfo/incubator-openwhisk-deploy-kube.git;
                cd \\$HOME/ow-deploy/incubator-openwhisk-deploy-kube;
                "''' } 
                echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
 }
            //Step 6 Upload cert and get details
            script {
                 try {
               sshagent (credentials: ["$awsKeyName"]) {
             env.ssl_cert = sh (returnStdout: true,script :'''ssh -o  StrictHostKeyChecking=no ubuntu@$host "aws iam delete-server-certificate --server-certificate-name ow-self-signed
                "''').trim();
          }
           echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
            }
            catch (Exception ex) {
            println("There was a problem deploying OW .Try again");
            echo 'Uploading OW Server cert to AWS '
            }
            try { 
               sshagent (credentials: ["$awsKeyName"]) {
             env.ssl_cert = sh (returnStdout: true,script :'''ssh -o  StrictHostKeyChecking=no ubuntu@$host "aws iam upload-server-certificate --server-certificate-name ow-self-signed --certificate-body file://\\$HOME/ow-deploy/incubator-openwhisk-deploy-kube/helm/openwhisk-server-cert.pem --private-key file://\\$HOME/ow-deploy/incubator-openwhisk-deploy-kube/helm/openwhisk-server-key.pem|grep -Po \'\\"Arn\\": *\\"[^\\"]*\'| grep -o \'[^\\"]*$\'
                "''').trim();
              echo "SSL certification: ${ssl_cert}"
          }
           echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
            }
            catch (Exception ex) {
            println("There was a problem deploying OW .Try again");
} }
     
                
            //Step 7 tiller-deploy pod in the kube-system namespace to be in the Running state
            script {
            echo 'tiller-deploy pod in the kube-system namespace to be in the Running state'
            try {
               sshagent (credentials: ["$awsKeyName"]) {
                 sh '''ssh -o  StrictHostKeyChecking=no ubuntu@$host "ls;      
                 sudo helm init;  
                 kubectl create clusterrolebinding tiller-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default;
                 "'''
               }
                 echo 'Waiting 2 minutes for deployment to complete prior tasks'
                 sleep 120 // seconds
            }
            catch (Exception ex) {
            println("Kubeconfig not installed correctly")
} }
              
            //Step 8 Deploying OpenWhisk using Helm
            script {
            echo 'Deploying OpenWhisk using Helm'
            try {
               sshagent (credentials: ["$awsKeyName"]) {
                 sh '''ssh -o  StrictHostKeyChecking=no ubuntu@$host "ls;
                kubectl label nodes --all openwhisk-role=invoker;
                cd \\$HOME/ow-deploy;
                sudo echo 'whisk:\n  ingress:\n    type: LoadBalancer\n    annotations:\n      service.beta.kubernetes.io/aws-load-balancer-internal: 0.0.0.0/0\n      service.beta.kubernetes.io/aws-load-balancer-ssl-cert: ${ssl_cert}\nk8s:\n  persistence:\n    enabled: false' >> \\$HOME/ow-deploy/mycluster.yaml;
                cd \\$HOME/ow-deploy/incubator-openwhisk-deploy-kube;
                sudo helm install ./helm/openwhisk --namespace=openwhisk --name=owdev -f \\$HOME/ow-deploy/mycluster.yaml;
                "'''
               }
            sleep 120 // seconds
            sshagent (credentials: ["$awsKeyName"]) {
             env.apiHost = sh (returnStdout: true,script :'''ssh -o  StrictHostKeyChecking=no ubuntu@$host "aws elb describe-load-balancers --query \'LoadBalancerDescriptions[?VPCId==\\`$vpcId\\`]|[].CanonicalHostedZoneName\'
                "''').trim(); 
                }
                echo "Use this API host in your CLI or Rest APIs: \n\n==========================\n\n${apiHost}\n\n=========================="
                 sleep 120 // seconds
            }
            catch (Exception ex) {
            println("Kubeconfig not installed correctly")
} }
				 //Step 9 Deleting the EC2 instance added to create EKS cluster for OW
            script {
            echo 'Deleting the EC2 instance added to create EKS cluster for OW'
            try { 
 				sh (returnStdout: true, script:'sudo ./gradlew TearDownInstance -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=$instancetype -PINSTANCE_ID=$instanceID -PKEY_NAME= -PROLE_ARN= --no-daemon').trim()
         		echo 'Instance terminated successfully. OW EKS Setup complete' 
            }
            catch (Exception ex) {
            println("Unable to delete the instance. Check EC2 console.")
} }
     } }
          
      // Tear down OW Cluster on Single node
     stage('Tear down instances or OW Cluster') {
     when {
     expression {env.action == 'teardownow' && env.deploymentType == 'awsSingleNode'}
     }
      steps {                     
            script {
				sh (returnStdout: true, script:'sudo ./gradlew TearDownInstance -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=$instancetype -PINSTANCE_ID=$instanceID -PKEY_NAME= -PROLE_ARN= --no-daemon').trim()
         		echo 'Waiting 2 minutes to complete prior tasks'
                 sleep 120 // seconds
         		echo 'Instance terminated successfully'
        }  	   }     }
        
   // Tear down OW Cluster on EKS cluster
     stage('Tear down OW cluster on EKS') {
     when {
     expression {env.action == 'teardownow' && env.deploymentType == 'awsEKS'}
     }
      steps {                      
            script {
              sh (returnStdout: true, script:'sudo ./gradlew DeleteEksCluster -PACCESS_KEY_ID=$aws_access_key -PSECRET_ACCESS_KEY=$aws_access_secret -PREGION=$region -PINSTANCE_TYPE=$instancetype -PINSTANCE_ID=$instance -PKEY_NAME= -PROLE_ARN= --no-daemon').trim()
         		sleep 120 // seconds
         		echo 'Cluster terminated successfully'
        }  	   }     } 
          
             }    
     } 
            