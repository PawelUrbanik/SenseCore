FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY services/contracts/pom.xml services/contracts/pom.xml
COPY services/processor-service/pom.xml services/processor-service/pom.xml
COPY services/query-service/pom.xml services/query-service/pom.xml
COPY services/ingestion-service/pom.xml services/ingestion-service/pom.xml


RUN mvn -q -DskipTests -pl services/processor-service -am dependency:go-offline

COPY services/contracts/src services/contracts/src
COPY services/processor-service/src services/processor-service/src

RUN mvn -q -DskipTests -pl services/processor-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 10001 appuser
USER appuser
COPY --from=build /workspace/services/processor-service/target/*.jar /app/app.jar
EXPOSE 8082
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
