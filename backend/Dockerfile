# ---- сборка ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- запуск ----
FROM eclipse-temurin:17-jre
RUN useradd --system --create-home appuser
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER appuser
ENV JAVA_OPTS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "echo \"[start] PORT=$PORT\"; ls -la /app; java -version; echo \"[start] launching\"; exec java $JAVA_OPTS -jar app.jar"]