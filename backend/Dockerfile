# 阶段 1: 使用 Maven 进行构建
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline
# 复制源码并打包
COPY src ./src

# 关键步骤：添加阿里云镜像配置
RUN echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
      <mirrors> \
        <mirror> \
          <id>aliyunmaven</id> \
          <mirrorOf>*</mirrorOf> \
          <name>阿里云公共仓库</name> \
          <url>https://maven.aliyun.com/repository/public</url> \
        </mirror> \
      </mirrors> \
    </settings>' > /usr/share/maven/conf/settings.xml

# 然后再运行打包
RUN mvn clean package -DskipTests

# 阶段 2: 使用轻量级 JRE 运行
FROM openjdk:17-jdk-slim
WORKDIR /app
# 从构建阶段拷贝 jar 包
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8123
ENTRYPOINT ["java", "-jar", "app.jar"]