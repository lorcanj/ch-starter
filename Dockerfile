# Stage 1: Build and Package
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
#LABEL author="Lorcan James"
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/docsapp/target/docsapp-0.0.1-SNAPSHOT.jar"]
WORKDIR /docsapp
# this will not include files in .dockerignore
COPY ./pom.xml ./
COPY ./src ./src
RUN mvn package -Dmaven.test.skip=true

# Stage 2: Production
FROM eclipse-temurin:21-jre-alpine
LABEL author="Lorcan James"
# only a declaration
EXPOSE 80
ENTRYPOINT ["java","-jar","/docsapp/docsapp.jar"]
WORKDIR /docsapp
VOLUME /docsapp/h2
COPY --from=build /docsapp/target/docsapp-0.0.1-SNAPSHOT.jar /docsapp/docsapp.jar