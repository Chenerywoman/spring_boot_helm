# spring_boot_helm

## Make an ECR

Make a ECR (Amazon Elastic Container Registry) repository using terraform:
 
Go to the provisioning_eks_terraform folder
 
```cd provisioning_eks_terraform_master```
 
open it in Visual Studio code:
 
```code . ```
 
Create a new folder in the modules folder called ‘ecr’
 
Inside the ecr folder, create two files:

main.tf

variables.tf
 
n.b. for documentation, you can consult the [terraform website](terraform.io)
click on terraform CLI -> Providers -> Major Cloud -> AWS -> ECR -> Resources -> aws_ecr_respository.

However, this involves some hard-coding, e.g. the name, so can use a variable.tf to make this more re-usable.
 
Type the following in variables.tf:
 
```

variable “ecr_repository_names” {
                Type = list(string)```
}

```
In main.tf, type the following:

```
resource "aws_ecr_repository" "ecr-container-registry"{
    count                   = length(var.ecr_repository_names)
    name                    = var.ecr_repository_names[count.index]
    image_tag_mutability    = "MUTABLE"
    image_scanning_configuration {
        scan_on_push = true
    }
}

# scan on push means it will scan for vulnerabilities 
```
In __environments/dev__ bring in the ECR module by adding the following code to the __main.tf__ file:
 
This puts the names of repository/ies into the variable.tf in the ecr module in the ecr folder.

```

module "ecr" {
    source                      = "../../modules/ecr"
    ecr_repository_names        = ["course-day-service"]
}
```

On the command line, switch to the environments/dev folder
 
```cd ./environments/dev```
 
Run the terraform commands:
 
Initialise terraform as we have brought in a new module:
 
```terraform init```
 
Then run the following commands:
 
```terraform plan``` & input ‘yes’ when requested.
 
When a plan has been created, ```terraform apply``` & input ‘yes’ when requested.
 
Check that the ECR has been created on the Amazon Console:
 
Console -> Services -> Resource Groups -> ECR -> Repositories
 
## Jenkins: Build application, run tests & push Docker image to ECR 
 
Run the following command:
 
```kubectl get service```
 
Copy the ExternalIP for the Jenkins-app LoadBalancer & paste into web explorer
 
Username for Jenkins is ‘admin’
 
Run the following commands to get your Jenkins password:
 
Go into the Jenkins project folder ```cd jenkins-helm```
 
Look at the README file ```cat README``` & copy the command to get the password & run that on the command line:
 
```printf $(kubectl get secret --namespace default jenkins-app -o jsonpath="{.data.jenkins-admin-password}" | base64 --decode);echo```
 
__Install plugins on Jenkins UI:__
 
Click on Manage Jenkins -> manage plugins -> available
Filter by ‘ecr’ & click ‘Amazon ecr’ -> install without restart
 
The plugin enables pushing Docker image up to ECR & can use AWS credentials to login:
Credentials -> System -> Global Credentials -> Add Credentials -> AWS Credentials
 
For the id, need to map to the Jenkins file in the SpringBoot app:
 
Go to the spring boot folder: ```cd spring_boot_helm```
 
Look at the __Jenkinsfile__ & in the ‘push to ecr’ stage, ‘aws-access’  is what will be the id & this is what to put in the id box on the website.
 
Get the AWS keys: ```cat ~/.aws/credentials```
  * copy __aws_access_key_id__ into 'Access Key ID'
  * copy __aws_secret_access_key__ into 'Secret Access Key'
 
__Install plugins with code:__

 Plugins to Jenkins app can be done by adding them to the values.yaml, instead of through the UI. 
 
To get details of the wording to plug in:
 
* Get the versions from [jenkins plugin site](https://plugins.jenkins.io/). 
* Search ECR [https://plugins.jenkins.io/ui/search?query=ecr] 
* the specific plugin: [https://plugins.jenkins.io/amazon-ecr/]. 
* Scroll right down to the bottom to see the versions.
* use the plugin ID which is _amazon-ecr_
 
Go to the Jenkins_helm folder: ```cd Jenkins_helm```
 
Look at the contents of the __values.yaml__: ```vi values.yaml``` then ```\plugin``` until you find ‘_List of plugins to be installed during Jenkins master start_’ which has a list of the plugins. 

__Add app to Jenkins__
 
* Click on ‘create new job’ on Jenkins home page
  * Name: course-day-service

* Click on pipeline -> ok Pipeline -> Select ‘pipeline script from SCM’ , then SCM – select Git Repository url

* go to GitHub & clone the https url of the spring_boot_helm app & paste the url 

* tick ‘this project is parameterised’  & choose string parameter

* Add parameters:
    * name: ECR_ADDRESS (from the push to ecr stage) in Jenkinsfile in spring_boot_helm project
    * default value: copy from AWS Console -> Elastic Container Registry -> course-day-service -> copy URI into value 
    * put https:// at the beginning (the protocol is required).  This is the ECR address
    * tick ‘trim string’ just in case there are leading or trailing spaces

* Click Apply -> Save
 
__Build app on Jenkins & push to the ECR__
 
Click on Build with Parameters & click on Console to see the building happening.

When finished, check the Console again to see if the Docker image has appeared on the ECR.
 
Can set Build Triggers to check for changes in the source code:

* Click on the tab for the app in Jenkins -> General -> Build triggers -> Poll SCM

* In Description, ```H/15 * * * *``` (see examples on the page) will check for changes every 15 minutes
                
* To check, change something in the front end of the spring-boot app & push to Github.  This should push up a new version of the Docker image to the ECR
 
## Deploy Spring-Boot app with Helm
 
Go to the folder ```cd spring_boot_helm``` & open ```code .```

In the __values.yaml__ file, need to add in the values for image: 
* repository 
    * address from AWS Console – ECR-Repositories-course-day-service up to the colon
* tag
    * tag from AWS Console – ECR-Repositories-course-day-service

Go into the helm directory ```cd helm```

Type the following command:
```helm install –set image.repository={repository - as above} –set image.tag={tag - as above} course-day-service ./spring-boot```
 
Check it is installed ```kubectl get pods```
 
To get the url: ```kubectl get service``` & copy external IP address into web browser
 
To look at the logs: ```kubectl get pods``` copy name then ```kubectl logs -f {name}```
 
**To install a later version of the docker image:**
 
Get the later docker image tag from the AWS Console & change in the command below, n.b.‘upgrade’ has been substituted for ‘install’:
 
```helm upgrade –set image.repository={address from AWS Console – ECR-Repositories-course-day-service up to the colon} –set image.tag={tag from AWS Console – ECR-Repositories-course-day-service} course-day-service ./spring-boot```
 
## Pull down
 
```helm uninstall Jenkins-app```

```helm uninstall course-day-service```

```terraform destroy```
 