# UIKit Karaoke Scene Backend Service
## Project Introduction
- This project is developed based on the Spring Boot framework, relying on Redis/MongoDB/RTM/NCS components.
- Redis is mainly used for online user number real-time updates and data consistency through distributed lock.
- MongoDB is mainly used to maintain the room list.
- RTM is used to store scene data and message transmission channels.
- NCS is used for RTC channel event callback notification and processing of personnel entering and leaving/room destruction logic.

## Service Deployment
### Quick Experience
- The service installation environment needs to have Docker environment installed, and the [docker-compose](https://docs.docker.com/compose/) deployment tool installed.
- You can download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/), which has already installed docker-compose.
- To launch the service locally, you need to open the docker-compose.yaml file and fill in the [appId and Secret](https://docs.agora.io/en/video-calling/reference/manage-agora-account?platform=android#get-the-app-id) obtained from Agora.
    - TOKEN_APP_ID
    - TOKEN_APP_CERTIFICATE
- Execute docker-compose up in the current project root directory, which will start Redis/MongoDB/Web service and pull related images. If the image fails to be pulled, you can configure a domestic image source to solve the problem.
- After the service is started, you can use curl http://localhost:8080/health/check to test.
- To debug local services using the app, you need to replace the corresponding backend service domain with http://service_machine_IP:8080 on the app. After replacing the domain, you can experience the related services on the app.
- To stop the service, execute docker-compose down.
- Note! NCS message notification is not turned on, and personnel entering and leaving and room destruction logic cannot be automatically processed. If you need to enable this feature, NCS service needs to be enabled.
- RTM and KTV permissions are not enabled, and the functional experience will be limited. To experience all the functions, refer to [Online Deployment Permission Access Instructions](#online-deployment).

### Local Development
- Java version is recommended to be >=11.
- The editor may use [Visual Studio Code](https://code.visualstudio.com/) and install the following plug-ins:
    - [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) (provides Dockerfile_dev for local container development)
    - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- RTM operation relies on the Linux environment.
- Open the project directory in vscode and enter container development.
- Modify the configuration file [application.yml](src/main/resources/application.yml):
    - spring.data.mongodb.uri
    - spring.redis.host
    - spring.redis.password
    - token.appId
    - token.appCertificate

### Online Deployment
- Before going online, Redis/MongoDB and other configurations need to be adjusted, and the service needs to be deployed behind the gateway. The gateway can provide authentication/traffic limit and other capabilities, and this service does not have gateway capabilities.
- At the same time, the following services need to be enabled:
    - RTM, [Contact customer service to enable](https://www.agora.io)
    - NCS, RTC channel event callback notification and processing of personnel entering and leaving/room destruction logic
        - [Enable Message Notification Service](https://docs-beta.agora.io/en/video-calling/develop/receive-notifications?platform=android#enable-notifications)
            - Choose the following event types
                - channel create, 101
                - channel destroy, 102
                - broadcaster join channel, 103
                - broadcaster leave channel, 104
            - Callback URL
                - https://yourdomain.com/v1/ncs/callback
            - Modify Secret
                - Based on the Secret value provided on the configuration page, modify the ncs.secret field in the project configuration file application.yml.
        - [Channel Event Callback](https://docs-beta.agora.io/en/video-calling/develop/receive-notifications?platform=android#channel-events)
    - KTV, contact sales to open KTV permission for the AppID (If you do not have contact information for sales, you can contact sales through intelligent customer service [Agora Support](https://agora-ticket.agora.io/)).
- Metric collection, https://yourdomain:9090/metrics/prometheus, can collect corresponding indicators to monitor the service as needed.
- The service can be deployed on cloud platforms, such as [Alibaba Cloud Container Service ACK](https://www.alibabacloud.com/en/product/kubernetes).

## Directory Structure
```
.
├── Dockerfile                                                          // Project build image
├── Dockerfile_dev                                                      // Local development image
├── HELP.md
├── README.md
├── README_zh.md
├── docker-compose.yaml                                                 // Local one-click deployment
├── init-mongo.js                                                       // MongoDB initialization
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── io
│   │   │       └── agora
│   │   │           └── uikit
│   │   │               ├── Application.java                            // Startup file
│   │   │               ├── bean                                        // Object class
│   │   │               │   ├── domain                                  // Entity class
│   │   │               │   ├── dto                                     // Transfer class
│   │   │               │   ├── entity                                  // DB entity class
│   │   │               │   ├── enums                                   // Enumeration
│   │   │               │   ├── exception                               // Exception handling
│   │   │               │   │   └── BusinessException.java
│   │   │               │   ├── repository                              // DB access layer
│   │   │               │   │   └── RoomListRepository.java
│   │   │               │   ├── req                                     // Request class
│   │   │               │   └── valid                                   // Verify
│   │   │               │       ├── EnumValid.java
│   │   │               │       └── EnumValidator.java
│   │   │               ├── config                                      // Configuration
│   │   │               │   ├── GlobalExceptionHandler.java             // Global exception capture
│   │   │               │   ├── RedisConfig.java                        // Redis configuration
│   │   │               │   └── WebMvcConfig.java                       // MVC configuration
│   │   │               ├── controller                                  // Controller
│   │   │               │   ├── ChorusController.java                   // Chorus management
│   │   │               │   ├── HealthController.java                   // Health check
│   │   │               │   ├── MicSeatController.java                  // Mic management
│   │   │               │   ├── NcsController.java                      // NCS message notification
│   │   │               │   ├── RoomController.java                     // Room management
│   │   │               │   ├── SongController.java                     // Song management
│   │   │               │   └── TokenController.java                    // Token management
│   │   │               ├── interceptor                                 // Interceptor
│   │   │               │   ├── PrometheusMetricInterceptor.java        // Indicator interceptor
│   │   │               │   └── TraceIdInterceptor.java                 // Trace link
│   │   │               ├── metric                                      // Indicator report
│   │   │               │   └── PrometheusMetric.java
│   │   │               ├── service                                     // Service layer
│   │   │               │   ├── IChorusService.java                     // Chorus
│   │   │               │   ├── IMicSeatService.java                    // Mic
│   │   │               │   ├── INcsService.java                        // NCS message notification
│   │   │               │   ├── IRoomService.java                       // Room
│   │   │               │   ├── IService.java
│   │   │               │   ├── ISongService.java                       // Song
│   │   │               │   ├── ITokenService.java                      // Token
│   │   │               │   └── impl
│   │   │               │       ├── ChorusServiceImpl.java
│   │   │               │       ├── MicSeatServiceImpl.java
│   │   │               │       ├── NcsServiceImpl.java
│   │   │               │       ├── RoomServiceImpl.java
│   │   │               │       ├── SongServiceImpl.java
│   │   │               │       └── TokenServiceImpl.java
│   │   │               ├── task                                        // Task
│   │   │               │   └── RoomListTask.java                       // Room list timing processing
│   │   │               └── utils                                       // Tool class
│   │   │                   ├── HmacShaUtil.java                        // Encryption
│   │   │                   ├── RedisUtil.java                          // Redis operation
│   │   │                   ├── RtmUtil.java                            // RTM operation
│   │   │                   └── TokenUtil.java                          // Token operation
│   │   └── resources
│   │       ├── application.yml                                         // Configuration
│