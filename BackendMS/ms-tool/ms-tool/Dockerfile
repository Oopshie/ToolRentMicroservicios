FROM eclipse-temurin:17-jdk
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-tool.jar
ENTRYPOINT ["java","-jar","/ms-tool.jar"]