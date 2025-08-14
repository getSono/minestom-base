FROM openjdk:21 as build

LABEL author="Marlon Röder"
LABEL description="Dockerfile for building the Test App"

WORKDIR /build
COPY . .
RUN ./gradlew clean build -x test

FROM openjdk:21
LABEL author="Marlon Röder"
LABEL description="Dockerfile for running the Test App"

WORKDIR /app
COPY --from=build /build/testapp/build/libs/testapp-*-all.jar app.jar

EXPOSE 25565
ENTRYPOINT ["java", "-jar", "app.jar"]