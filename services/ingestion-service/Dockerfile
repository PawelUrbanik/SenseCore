# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first for caching dependencies
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as non-root
RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
