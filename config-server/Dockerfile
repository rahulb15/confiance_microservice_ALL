FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/config-server-0.0.1-SNAPSHOT.jar config-server.jar


RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8888

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8888/actuator/health || exit 1

#ENTRYPOINT ["java", "-jar", "config-server.jar", "--spring.profiles.active=docker"]
ENTRYPOINT ["java", "-jar", "config-server.jar", "--spring.profiles.active=native"]

