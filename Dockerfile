FROM openjdk:17
WORKDIR /app
COPY target/quarkus-app/lib/ /app/lib/
COPY target/quarkus-app/*.jar /app/
COPY target/quarkus-app/app/ /app/app/
COPY target/quarkus-app/quarkus/ /app/quarkus/
EXPOSE 8081
CMD ["java", "-Dquarkus.http.port=8081", "-jar", "/app/quarkus-run.jar"]