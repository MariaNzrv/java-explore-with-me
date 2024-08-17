# syntax=docker/dockerfile:1

# Comments are provided throughout this file to help you get started.
# If you need more help, visit the Dockerfile reference guide at
# https://docs.docker.com/go/dockerfile-reference/

# Want to help us make this template better? Share your feedback here: https://forms.gle/ybq9Krt8jtBL3iCk7

################################################################################

# Create a stage for resolving and downloading dependencies.
FROM eclipse-temurin:17-jdk-jammy as deps

RUN apt update
RUN apt install maven -y

WORKDIR /build/java-explore-with-me

#COPY --chmod=0755 mvnw mvnw
#COPY .mvn/ .mvn/
# java-explore-with-me.pom
COPY pom.xml pom.xml

COPY main-service/pom.xml main-service/pom.xml

COPY stats/pom.xml stats/pom.xml

COPY stats/stats-server/pom.xml stats/stats-server/pom.xml
COPY stats/stats-dto/pom.xml stats/stats-dto/pom.xml
COPY stats/stats-client/pom.xml stats/stats-client/pom.xml

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -DskipTests || true

################################################################################

# Create a stage for building the application based on the stage with downloaded dependencies.
# This Dockerfile is optimized for Java applications that output an uber jar, which includes
# all the dependencies needed to run your app inside a JVM. If your app doesn't output an uber
# jar and instead relies on an application server like Apache Tomcat, you'll need to update this
# stage with the correct filename of your package and update the base image of the "final" stage
# use the relevant app server, e.g., using tomcat (https://hub.docker.com/_/tomcat/) as a base image.
FROM deps as package

WORKDIR /build/java-explore-with-me

COPY stats/stats-client/src stats/stats-client/src/
COPY stats/stats-dto/src stats/stats-dto/src/
COPY main-service/src main-service/src/
RUN --mount=type=cache,target=/root/.m2 mvn package -pl :main-service -am -DskipTests

################################################################################

# Create a new stage for running the application that contains the minimal
# runtime dependencies for the application. This often uses a different base
# image from the install or build stage where the necessary files are copied
# from the install stage.
#
# The example below uses eclipse-turmin's JRE image as the foundation for running the app.
# By specifying the "17-jre-jammy" tag, it will also use whatever happens to be the
# most recent version of that tag when you build your Dockerfile.
# If reproducability is important, consider using a specific digest SHA, like
# eclipse-temurin@sha256:99cede493dfd88720b610eb8077c8688d3cca50003d76d1d539b0efc8cca72b4.
FROM eclipse-temurin:17-jre-jammy AS final

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the executable from the "package" stage.
COPY --from=package /build/java-explore-with-me/main-service/target/main-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
