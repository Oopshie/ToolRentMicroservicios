FROM eclipse-temurin:17-jdk
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-rent.jar
ENTRYPOINT ["java","-jar","/ms-rent.jar"]