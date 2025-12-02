# Stage 1: Build WAR bằng Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy file cấu hình Maven trước để cache
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build file WAR
RUN mvn -q -e -DskipTests package

# Stage 2: Chạy Tomcat + WAR
FROM tomcat:10.1

# Xoá app ROOT mặc định của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/ROOT*

# Copy WAR vừa build thành ROOT.war
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
