FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "user-service.jar", "--spring.profiles.active=docker"]