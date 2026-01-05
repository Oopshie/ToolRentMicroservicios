FROM eclipse-temurin:17-jdk
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-rate.jar
ENTRYPOINT ["java","-jar","/ms-rate.jar"]