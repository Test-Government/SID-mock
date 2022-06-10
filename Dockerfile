FROM gradle:jdk17-alpine AS test
LABEL stage=test
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle --no-daemon test

FROM gradle:jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle --no-daemon assemble

FROM docker.io/openjdk:17 as app
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*all.jar /app/sid-mock.jar
EXPOSE 6666
ENTRYPOINT java -jar /app/sid-mock.jar
