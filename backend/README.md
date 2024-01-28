# UIKit Karaoke Scene Backend Service
English | [中文](README_zh.md)
## Project Introduction
- This project is developed based on the Spring Boot framework, relying on Redis/MongoDB components.
- Redis is primarily used to provide distributed caching and distributed locking mechanisms to ensure data consistency during updates.
- MongoDB is mainly used to maintain the room list.

## Service Deployment
### Quick Experience
- The service installation environment needs to have the latest Docker environment installed, and the [docker-compose](https://docs.docker.com/compose/) deployment tool installed.
- You can download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/), which has already installed docker-compose.
- To launch the service locally, you need to open the docker-compose.yaml file and fill in the [appId and Secret](https://docs.agora.io/en/video-calling/reference/manage-agora-account?platform=android#get-the-app-id) obtained from Agora and [Huanxin](<https://docs-im-beta.easemob.com/document/server-side/enable_and_configure_IM.html）>).
- For local deployment, before starting the service, you need to create a `.env` file in the root directory of the project and fill in the following fields:
    - WHITELIST_TOKEN_APP_ID=< Your WHITELIST_TOKEN_APP_ID >
    - WHITELIST_TOKEN_APP_CERT=< Your WHITELIST_TOKEN_APP_CERT >
    - WHITELIST_CHAT_ROOM_APP_ID=< Your WHITELIST_CHAT_ROOM_APP_ID >
    - WHITELIST_CHAT_ROOM_ORG_NAME=< Your WHITELIST_CHAT_ROOM_ORG_NAME >
    - WHITELIST_CHAT_ROOM_APP_NAME=< Your WHITELIST_CHAT_ROOM_APP_NAME >
    - WHITELIST_CHAT_ROOM_CLIENT_ID=< Your WHITELIST_CHAT_ROOM_CLIENT_ID >
    - WHITELIST_CHAT_ROOM_CLIENT_SECRET=< Your WHITELIST_CHAT_ROOM_CLIENT_SECRET >
- Execute `docker compose up -d --build` in the current project root directory, which will pull related images and start Redis/MongoDB/Web service.
- After the service is started, you can use curl http://localhost:8080/health/check to test.
- To debug local services using the app, you need to replace the corresponding backend service domain with http://service_machine_IP:8080 on the app. After replacing the domain, you can experience the related services on the app.
- To stop the service, execute `docker compose down`.

### Local Development
- Java version is recommended to be >=11.
- The editor may use [Visual Studio Code](https://code.visualstudio.com/) and install the following plug-ins:
    - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- RTM operation relies on the Linux environment.
- Open the project directory in vscode and enter container development.
- Modify the configuration file [application.yml](src/main/resources/application.yml):
    - spring.data.mongodb.uri
    - spring.redis.host
    - spring.redis.password
    - whitelist.token.appId
    - whitelist.token.appCert
    - whitelist.chatRoom.appId
    - whitelist.chatRoom.orgName
    - whitelist.chatRoom.appName
    - whitelist.chatRoom.clientId
    - whitelist.chatRoom.clientSecret

### Online Deployment
- Before going online, Redis/MongoDB and other configurations need to be adjusted, and the service needs to be deployed behind the gateway. The gateway can provide authentication/traffic limit and other capabilities, and this service does not have gateway capabilities.
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
│   │   │               │   ├── req                                     // Request class
│   │   │               │   └── valid                                   // Verify
│   │   │               │       ├── EnumValid.java
│   │   │               │       └── EnumValidator.java
│   │   │               ├── config                                      // Configuration
│   │   │               │   ├── GlobalExceptionHandler.java             // Global exception capture
│   │   │               │   ├── RedisConfig.java                        // Redis configuration
│   │   │               │   ├── ChatRoomAPIClient.java                  // Huanxin IM configuration
│   │   │               │   ├── WhitelistConfig.java                    // Whitelist configuration
│   │   │               │   └── WebMvcConfig.java                       // MVC configuration
│   │   │               ├── controller                                  // Controller
│   │   │               │   ├── HealthController.java                   // Health check
│   │   │               │   ├── RoomV2Controller.java                   // Room management
│   │   │               │   ├── ChatRoomV2Controller.java               // ChatRoom management
│   │   │               │   └── TokenV2Controller.java                  // Token management
│   │   │               ├── interceptor                                 // Interceptor
│   │   │               │   ├── PrometheusMetricInterceptor.java        // Indicator interceptor
│   │   │               │   └── TraceIdInterceptor.java                 // Trace link
│   │   │               ├── metric                                      // Indicator report
│   │   │               │   └── PrometheusMetric.java
│   │   │               │── repository                                  // DB access layer
│   │   │               │   └── RoomListV2Repository.java
│   │   │               ├── service                                     // Service layer
│   │   │               │   ├── IRoomV2Service.java                     // Room
│   │   │               │   ├── ITokenV2Service.java                    // Token
│   │   │               │   ├── IEMAPIService.java                      // Huanxin IM API
│   │   │               │   ├── IChatRoomAPIService.java                // ChatRoom API
│   │   │               │   └── impl
│   │   │               │       ├── RoomV2ServiceImpl.java
│   │   │               │       ├── ChatRoomV2ServiceImpl.java
│   │   │               │       ├── ChatRoomAPIServiceImpl.java
│   │   │               │       └── TokenV2ServiceImpl.java
│   │   │               └── utils                                       // Tool class
│   │   │                   ├── RedisUtil.java                          // Redis operation
│   │   │                   └── TokenUtil.java                          // Token operation
│   │   └── resources
│   │       ├── application.yml                                         // Configuration
│   │       └── logback-spring.xml                                      // Log Configuration
│