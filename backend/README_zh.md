# UIKit K歌场景后台服务
[English](README.md) | 中文
## 项目简介
- 本项目基于Spring Boot框架开发, 依赖Redis/MongoDB/RTM/NCS组件
    - Redis, 主要用于在线人数定时刷新/分布式锁方式保证数据更新的一致性
    - MongoDB, 主要用于维护房间列表
    - RTM, 存储场景数据, 消息传输通道
    - NCS, RTC频道事件回调通知, 处理人员进出/房间销毁逻辑

## 服务部署
### 快速体验
- > 采用Docker部署服务, 服务安装环境需要提前安装好Docker环境, 并安装[docker-compose](https://docs.docker.com/compose/)部署工具
    - 可以下载安装[Docker Desktop](https://www.docker.com/products/docker-desktop/), 并已默认安装docker-compose
- 本地部署
    - > 本地启动服务前, 需要打开docker-compose.yaml文件, 填入在声网申请的[appId和Secret](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
        - TOKEN_APP_ID
        - TOKEN_APP_CERTIFICATE
    - 在当前项目根目录下执行 docker-compose up, 会拉取相关镜像并启动Redis/MongoDB/Web服务. 如镜像拉取失败, 可配置国内镜像源解决
    - 服务启动后, 可使用 curl http://localhost:8080/health/check 测试
    - 如果使用App调试本地服务, 需要在App上替换对应后端服务域名为http://服务机器IP:8080, 替换域名后可以使用App体验相关服务
    - 停止服务, 执行 docker-compose down

    > 注意! 未开启NCS消息通知, 不能自动处理人员进出和房间销毁逻辑, 如果需要开启此功能, 需开通NCS服务.
    
    > RTM和K歌权限未开通, 功能体验会受限, 如需完整体验功能, 可以参考[上线部署权限开通说明](#上线部署)

### 本地开发
- Java版本推荐>=11
- 编辑器可采用 [Visual Studio Code](https://code.visualstudio.com/), 安装以下插件
    - [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) (已提供本地容器开发[Dockerfile](Dockerfile_dev)文件)
    - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
    > RTM运行依赖Linux环境
- vscode打开项目目录, 进入容器开发
- 修改配置文件[application.yml](src/main/resources/application.yml)
    - spring.data.mongodb.uri
    - spring.redis.host
    - spring.redis.password
    - token.appId
    - token.appCertificate

### 上线部署
- 正式上线前, 需要调整Redis/MongoDB等配置, 并将服务部署在网关后, 网关可提供鉴权/限流等能力, 本服务不带网关能力
- 同时还需要开通以下服务
    - RTM, [联系客服人员开通](https://www.shengwang.cn)
    - NCS, RTC频道事件回调通知, 处理人员进出/房间销毁逻辑
        - [开通消息通知服务
    ](https://docs.agora.io/cn/video-call-4.x/enable_webhook_ncs?platform=All%20Platforms)
            - 选择以下事件类型
                - channel create, 101
                - channel destroy, 102
                - broadcaster join channel, 103
                - broadcaster leave channel, 104
            - 回调地址
                - https://你的域名/v1/ncs/callback
            - 修改Secret
                - 根据配置界面提供的Secret值, 修改项目配置文件application.yml的ncs.secret
        - [频道事件回调
    ](https://docs.agora.io/cn/video-call-4.x/rtc_channel_event?platform=All%20Platforms)
    - K歌, 联系销售给 AppID 开通 K歌权限(如果您没有销售人员的联系方式可通过智能客服联系销售人员 [Agora 支持](https://agora-ticket.agora.io/))
- 指标收集, https://您的域名:9090/metrics/prometheus, 可根据需要收集相应指标监控服务
- 服务可以部署在云平台, 比如[阿里云容器服务ACK](https://www.alibabacloud.com/zh/product/kubernetes)

## 目录结构
```
.
├── Dockerfile                                                          // 项目构建镜像
├── Dockerfile_dev                                                      // 本地开发镜像
├── HELP.md
├── README.md
├── README_zh.md
├── docker-compose.yaml                                                 // 本地一键部署
├── init-mongo.js                                                       // MongoDB初始化
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── io
│   │   │       └── agora
│   │   │           └── uikit
│   │   │               ├── Application.java                            // 启动文件
│   │   │               ├── bean                                        // 对象类
│   │   │               │   ├── domain                                  // 实体类
│   │   │               │   ├── dto                                     // 传输类
│   │   │               │   ├── entity                                  // DB实体类
│   │   │               │   ├── enums                                   // 枚举
│   │   │               │   ├── exception                               // 异常处理
│   │   │               │   │   └── BusinessException.java
│   │   │               │   ├── req                                     // 请求类
│   │   │               │   └── valid                                   // 校验
│   │   │               │       ├── EnumValid.java
│   │   │               │       └── EnumValidator.java
│   │   │               ├── config                                      // 配置
│   │   │               │   ├── GlobalExceptionHandler.java             // 全局异常捕获
│   │   │               │   ├── RedisConfig.java                        // Redis配置
│   │   │               │   └── WebMvcConfig.java                       // MVC配置
│   │   │               ├── controller                                  // 控制器
│   │   │               │   ├── ChorusController.java                   // 合唱管理
│   │   │               │   ├── HealthController.java                   // 健康检查
│   │   │               │   ├── MicSeatController.java                  // 麦位管理
│   │   │               │   ├── NcsController.java                      // NCS消息通知
│   │   │               │   ├── RoomController.java                     // 房间管理
│   │   │               │   ├── SongController.java                     // 歌曲管理
│   │   │               │   └── TokenController.java                    // Token管理
│   │   │               ├── interceptor                                 // 拦截器
│   │   │               │   ├── PrometheusMetricInterceptor.java        // 指标拦截器
│   │   │               │   └── TraceIdInterceptor.java                 // 链路追踪
│   │   │               ├── metric                                      // 指标上报
│   │   │               │   └── PrometheusMetric.java
│   │   │               │── repository                                  // DB访问层
│   │   │               │   └── RoomListRepository.java
│   │   │               ├── service                                     // 服务层
│   │   │               │   ├── IChorusService.java                     // 合唱
│   │   │               │   ├── IMicSeatService.java                    // 麦位
│   │   │               │   ├── INcsService.java                        // NCS消息通知
│   │   │               │   ├── IRoomService.java                       // 房间
│   │   │               │   ├── IService.java
│   │   │               │   ├── ISongService.java                       // 歌曲
│   │   │               │   ├── ITokenService.java                      // Token
│   │   │               │   └── impl
│   │   │               │       ├── ChorusServiceImpl.java
│   │   │               │       ├── MicSeatServiceImpl.java
│   │   │               │       ├── NcsServiceImpl.java
│   │   │               │       ├── RoomServiceImpl.java
│   │   │               │       ├── SongServiceImpl.java
│   │   │               │       └── TokenServiceImpl.java
│   │   │               ├── task                                        // 任务
│   │   │               │   └── RoomListTask.java                       // 房间列表定时处理
│   │   │               └── utils                                       // 工具类
│   │   │                   ├── HmacShaUtil.java                        // 加密
│   │   │                   ├── RedisUtil.java                          // Redis操作
│   │   │                   ├── RtmUtil.java                            // RTM操作
│   │   │                   └── TokenUtil.java                          // Token操作
│   │   └── resources
│   │       ├── application.yml                                         // 配置文件
│   │       ├── lib                                                     // RTM依赖库
│   │       │   ├── agora-rtm-sdk.jar
│   │       │   ├── libagora-fdkaac.so
│   │       │   ├── libagora-ffmpeg.so
│   │       │   ├── libagora-soundtouch.so
│   │       │   ├── libagora_rtc_sdk.so
│   │       │   └── libagora_rtm_sdk.so
│   │       └── logback-spring.xml                                      // 日志配置
│   └── test                                                            // 单元测试
```


