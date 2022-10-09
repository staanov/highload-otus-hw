FROM maven:3.8.3-openjdk-17 AS build
COPY pom.xml /usr/src/highload/
COPY ./src /usr/src/highload/src/
WORKDIR /usr/src/highload
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk AS run
COPY --from=build /usr/src/highload/target/highload-otus-hw.jar \
/usr/src/highload/highload-otus-hw.jar
COPY --from=build /usr/src/highload/src /usr/src/highload/src/
WORKDIR /usr/src/highload
EXPOSE 8080
CMD ["java", "-jar", "/usr/src/highload/highload-otus-hw.jar"]
