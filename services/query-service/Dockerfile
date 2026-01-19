FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# --- Copy root + ALL module POMs (so Maven can read reactor) ---
COPY pom.xml ./pom.xml
COPY services/contracts/pom.xml services/contracts/pom.xml
COPY services/persistence/pom.xml services/persistence/pom.xml
COPY services/ingestion-service/pom.xml services/ingestion-service/pom.xml
COPY services/processor-service/pom.xml services/processor-service/pom.xml
COPY services/query-service/pom.xml services/query-service/pom.xml

# Download deps for query-service (and required modules)
RUN mvn -q -DskipTests -pl services/query-service -am dependency:go-offline

# --- Copy sources only for required modules ---
COPY services/contracts/src services/contracts/src
COPY services/query-service/src services/query-service/src

RUN mvn -q -DskipTests -pl services/query-service -am package
RUN ls -la /workspace/services/query-service/target || true


FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /workspace/services/query-service/target/*.jar /app/app.jar
EXPOSE 8083
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
