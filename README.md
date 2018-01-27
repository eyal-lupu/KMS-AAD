# KMS-AAD [![Build Status](https://travis-ci.org/eyal-lupu/KMS-AAD.svg?branch=master)](https://travis-ci.org/eyal-lupu/KMS-AAD)

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

## Starting The Server
### Local
The project is using spring-boot, use the following to build and boot a server (but read this section to the end before doing so!):
```shell
[~eyal]$ ./gradlew bootRun
```

In practice the question if the above will boot successfully or not depends on your environment. As the process has to connect
with AWS it requires the correct configuration to authenticate with AWS. If you environment is setup for that (for example
the credentials are available via AWS\_ACCESS\_KEY\_ID and AWS\_SECRET\_ACCESS\_KEY or the default credentials file exists 
the above will work - otherwise the environment needs to be setup - see below about 'Authentication with AWS').

The port on which the server will listen will be 8080.

### As a Docker Container
For a successful execution as a Docker container the appropriate AWS credentials and region **must** be provided to the container. One way
is using environment variables as demonstrated below but there are better (and more secure) ways (i.e. mapping ~/.aws into your container
as a volume). See https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html for more details 
```shell
[~eyal]$ docker run -d  -P --env AWS_ACCESS_KEY_ID=<replace> \
    --env AWS_SECRET_ACCESS_KEY=<replace> --env AWS_REGION=<replace>  \
    eyallupu/aad-sample-webapp
```

The next is to note down the host port which was mapped into the container's 8080 port, the following will list that out:
```shell
[~eyal]$ docker ps -n 1 --format "{{.ID}}" | xargs docker port
8080/tcp -> 0.0.0.0:32768
``` 
In the above example the port is 32768.

## Client Example
Once the server is running we can give it a try using CURL. The server is preconfigured with two users 'bob' and 'alice' with passwords
the same as the user name (e.g. bob/bob). We can now upload a secret as Alice (remember to replace host and port with actual values):

```shell
[~eyal]$ curl -X POST -H 'Content-type: text/plain' -d 'my-secret-value' \
   --user alice:alice  http://localhost:8080/secrets/my-secret-name
```

We can now retrieve the server using Alice's credentials:

```shell
[~eyal]$ curl -X GET --user alice:alice  http://localhost:8080/secrets/my-secret-name
```

However, if Bob will try to fetch the same secret he will be blocked by the KMS

```shell
[~eyal]$ curl -X GET --user bob:bob  http://localhost:8080/secrets/my-secret-name

{"timestamp":1517092879867,"status":500,"error":"Internal Server Error",
"exception":"com.amazonaws.services.kms.model.InvalidCiphertextException",
 "message":"org.springframework.web.util.NestedServletException: Request processing failed; nested exception is com.amazonaws.services.kms.model.InvalidCiphertextException: null (Service: AWSKMS; Status Code: 400; Error Code: InvalidCiphertextException; Request ID: 3a45ce22-03b4-11e8-8872-c1cc2f0068f1)",
 "path":"/secrets/my-secret-name"}
```

## Costs
Do remember that this example is using AWS KMS which involves costs.
