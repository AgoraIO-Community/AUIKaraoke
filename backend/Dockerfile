FROM ubuntu:20.04

ENV JAVA_HOME=/usr/lib/jvm/default-java

# Change apt repository to a China network friendly site.
RUN sed -i s@/archive.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list
RUN sed -i s@/security.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list

RUN apt-get clean
RUN apt-get update

RUN apt-get install -y --no-install-recommends \
    apt-utils \
    software-properties-common \
    lsb-release \
    wget \
    curl \
    libssl-dev \
    libssl-dev \
    libcrypto++-dev \
    libc++-dev

# Add dependencies for RTC.
RUN apt-get install -y libxdamage1 \
    libxcomposite-dev \
    libdrm-dev

# Install java
RUN apt-get -y install default-jdk

# Install maven
RUN apt-get -y install maven

# Copy dynamic library
COPY src/main/resources/lib/*.so /usr/lib/

WORKDIR /app

ARG JAR_FILE
ENV PROFILES=${PROFILES}
ENV MONGODB_URI=${MONGODB_URI}
ENV NCS_SECRET=${NCS_SECRET}
ENV REDIS_HOST=${REDIS_HOST}
ENV REDIS_PASSWORD=${REDIS_PASSWORD}
ENV TOKEN_APP_ID=${TOKEN_APP_ID}
ENV TOKEN_APP_CERTIFICATE=${TOKEN_APP_CERTIFICATE}

RUN echo "> JAR_FILE"
RUN echo ${JAR_FILE}

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT java -jar -Dspring.profiles.active=$PROFILES \
    -Dspring.data.mongodb.uri=$MONGODB_URI \
    -Dspring.redis.host=$REDIS_HOST \
    -Dspring.redis.password=$REDIS_PASSWORD \
    -Dncs.secret=$NCS_SECRET \
    -Dtoken.appId=$TOKEN_APP_ID \
    -Dtoken.appCertificate=$TOKEN_APP_CERTIFICATE \
    app.jar
