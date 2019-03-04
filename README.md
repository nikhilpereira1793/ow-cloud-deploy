<!--
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
-->
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
# OPENWHISK DEPLOY ON CLOUD
This project uses the [AWS JAVA SDK], [OPENWHISK DEVTOOLS] and [OPENWHISK KUBE DEPLOY] to deploy and run and OW instance on Amazon EC2 machines
It utilizes the capabilities of Jenkinsfile to automate the OpenWhisk deployment. Currently its a single node and  [AWS EKS] deployment but AWS self managed Kube, Azure single node ,Azure self managed Kube and [Azure AKS] deployment is coming soon.

### Jenkins Input Variables:
| Variable | Type | Description |
| --- | --- | --- |
| `aws_access_key` | string | The AWS access key |
| `aws_access_secret` | string | The AWS secret access key |
| `aws_instance_type` | input | AWS Instance Type.Default (T1 Micro) .This parameter is selected from the console at the time of Job execution|
| `aws_region` | input| AWS Region.Default  Region (UE1).This parameter is selected from the console at the time of Job execution|
| `awsKeyName` | string | Variable name of the aws key stored in Jenkins |
| `role_arn` | string | AWS IAM Role having EKS permissions.Only for EKS deployments |



### Build Parameters

 **ActionType:**

**deployow**  : Choose this option if you want to deploy Openwhisk on any cloud based provider.
This parameter would create an EC2 machine and deploys the necessary softwares/tools needed to run OW.Once the tools are install it would deploy OpenWhisk on the EC2 machine.This task does the following:
1. Creates EC2 resources
2. Installs OpenWhisk on the EC2 instance

**teardownow** : This parameter would delete/terminate  the EC2 instance where the OpenWhisk was hosted.This task does the following:
1. Terminates the EC2 instance based on the tag
2. Deletes the security group based on the tag


 **DeploymentType:**

**awsSingleNode**: Selecting this option will deploy OpenWhisk on a single EC2 instance.


**awsEKS**: Selecting this option will deploy OpenWhisk on Amazon EKS will default 3 worker nodes.The node configurations are configurable. You can select the size of the worker nodes from the Jenkins Console after the Job is started. At present the awsEKS deployment can only be done for `us-east-1` region.
You can create [Kube Dashboard] and start monitory your clusters.



### Prerequisites:

1. An AWS user with EC2 permissions. You will need the access key and secret for this user.Check [Managing AWS Keys]
for more details.
2. Amazon EKS service role. [Follow this to create EKS role](#create-your-amazon-eks-service-role)
3. Running Jenkins . Follow [Installing Jenkins on Ubuntu] to install Jenkins or use any running Jenkins environment
4. A Key for for each region or target region. Check [AWS Key Pair] help doc on how to create a key pair


### Create your Amazon EKS Service Role

***To create your Amazon EKS service role in the IAM console:***

1. Open the IAM console at https://console.aws.amazon.com/iam/.

2. Choose Roles, then Create role.

3. Choose EKS from the list of services, then Allows Amazon EKS to manage your clusters on your behalf for your use case, then Next: Permissions.

4. Choose Next: Review.

5. For Role name, enter a unique name for your role, such as eksServiceRole, then choose Create role.


### Jenkins Setup
1. Add the private key credentials to the [Jenkins global credentials]

2. Install ssh-agent plugin

3. Install pipeline plugins

4. Create a pipeline job

5. Add this [ow-cloud-deploy] repo to the Pipeline SCM script with the Script path `Jenkinsfile`

6. Build the job to import Jenkinsfile and populate the input parameters

### How to Run
##### Deploy OpenWhisk on either Single Node EC2 instance or AWS EKS cluster:

1. From the Job choose build with parameter

2. Select `deployow` from the action drop-down

3. Choose the `deploymentType`

4. Navigate to console output and when prompted select the required region/instance type

5. Once the job is complete ,copy the APIHOST from the Jenkins console and use it to connect to the Open Cluster


##### Terminate OpenWhisk and Tear Down AWS resources:

1. From the Job choose build with parameter

2. Select `teardown` from the action drop-down

3. Choose the `deploymentType`

4. Note that this job will tear down the EC2 machine/machines or clusters where OpenWhisk was deployed


### Deploy OpenWhisk on a Single Node EC2 instance using CloudFormation Template

[![Launch Template](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)](https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/new?stackName=myOpenWhiskStack&templateURL=https://s3.amazonaws.com/kerberos-cf-templates/aws-deploy-singlenode.yml)


## Demo
***Click on the gif to see the demo:***

[![Demo OpenWhikSingleNode](https://github.com/rahulqelfo/ow-cloud-deploy/blob/master/demo.gif)](https://spark.adobe.com/video/nJ6MFOd6agmyc)

 [OPENWHISK DEVTOOLS]: https://github.com/apache/incubator-openwhisk-devtools
 [AWS JAVA SDK]: https://aws.amazon.com/sdk-for-java/
 [AWS EKS]: https://aws.amazon.com/eks/
 [Azure AKS]: https://azure.microsoft.com/en-ca/services/kubernetes-service/
 [OPENWHISK KUBE DEPLOY]: https://github.com/apache/incubator-openwhisk-deploy-kube
 [Managing AWS Keys]: https://docs.aws.amazon.com/general/latest/gr/managing-aws-access-keys.html
 [Installing Jenkins on Ubuntu]: https://linuxize.com/post/how-to-install-jenkins-on-ubuntu-18-04/
 [AWS Key Pair]: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html#having-ec2-create-your-key-pair
 [EKS Getting Started]: https://docs.aws.amazon.com/eks/latest/userguide/getting-started.html
 [Jenkins global credentials]: https://jenkins.io/doc/book/using/using-credentials/
 [ow-cloud-deploy]: https://github.com/rahulqelfo/ow-cloud-deploy
 [Kube Dashboard]: https://docs.aws.amazon.com/eks/latest/userguide/dashboard-tutorial.html
