# KMS-AAD

This git repository is the implementation of my blog post at: http://blog.eyallupu.com/2018/01/encryption-context-aad-with-aws-kms.html


## Build

### As a JAR File
This is a Java based project built using Gradle. To build use the grade wrapper script in the repository root:

```shell
[~eyal]$ ./gradlew build
```

(or gradlew.bat on Windows). The 'build' task will build and test the project binaries. 

### As a Docker Image
To build a Docker image use the following command:
```shell
[~eyal]$ ./gradlew buildDocker
```
Which will build the image and tag it as *eyallupu/aad-sample-webapp*. Alternatively a ready made  image can be downloaded directly from Docker Hub:
```shell
[~eyal]$ docker pull eyallupu/aad-sample-webapp
```

## Run
### Run locally
The project is using spring-boot, use the following to build and boot a server (but read this section to the end before doing so!):
```shell
[~eyal]$ ./gradlew bootRun
```

In practice the question if the above will boot successfully or not depends on your environment. As the process has to connect
with AWS it requires the correct configuration to authenticate with AWS. If you environment is setup for that (for example
the credentials are available via AWS\_ACCESS\_KEY\_ID and AWS\_SECRET\_ACCESS\_KEY or the default credentials file exists 
the above will work - otherwise the environment needs to be setup - see below about 'Authentication with AWS').

### Run as a Docker Container
For a successful execution as a Docker container the appropriate AWS credentials and region **must** be provided to the container. One way
is using environment variables as demonstrated below but there are better (and more secure) ways (i.e. mapping ~/.aws into your container
as a volume). See https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html for more details 
```shell
[~eyal]$ docker run -d  -P -name aad-example  --env AWS_ACCESS_KEY_ID=<replace> --env AWS_SECRET_ACCESS_KEY=<replace> --env AWS_REGION=<replace>  eyallupu/aad-sample-webapp
```



## Costs
Do remember that this example is using AWS KMS which involves costs.

TODO: client example