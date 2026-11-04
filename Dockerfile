# syntax=docker/dockerfile:1

FROM hsldevcom/infodevops-docker-base-images:1.0.2-25-java-jdk AS build
WORKDIR /usr/app
COPY . .
RUN --mount=type=secret,id=github_token \
    --mount=type=secret,id=github_actor \
    export GITHUB_TOKEN="$(cat /run/secrets/github_token)" && \
    export GITHUB_ACTOR="$(cat /run/secrets/github_actor)" && \
    ./gradlew --no-daemon shadowJar

FROM hsldevcom/infodevops-docker-base-images:1.0.2-25-java-jre
WORKDIR /usr/app
COPY --from=build /usr/app/build/libs/transitlog-apc-archive-sink.jar /usr/app/transitlog-apc-archive-sink.jar
ENTRYPOINT ["java", "-XX:InitialRAMPercentage=10.0", "-XX:MaxRAMPercentage=95.0", "-jar", "/usr/app/transitlog-apc-archive-sink.jar"]
