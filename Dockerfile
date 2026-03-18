FROM maven:3.9.1 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY flyway /home/app/flyway
RUN mvn -f /home/app/pom.xml clean compile -P docker assembly:single dependency:copy-dependencies

FROM openjdk:19-ea-jdk-alpine3.16
RUN apk add --no-cache netcat-openbsd
COPY --from=build /home/app/target/*-jar-with-dependencies.jar app.jar
COPY --from=build /home/app/target/dependency/*.jar dependency/

ENTRYPOINT ["java","-p","dependency/","-jar","app.jar"]