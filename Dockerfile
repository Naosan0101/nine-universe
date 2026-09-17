# syntax=docker/dockerfile:1
# docker compose の app サービス用（マルチステージで bootJar を作成）
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src
# bootJar は archiveFileName = nine-universe.jar（*-SNAPSHOT.jar ではない）
RUN chmod +x gradlew \
	&& ./gradlew bootJar --no-daemon -x test \
	&& JAR="$(find /app/build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -print -quit)" \
	&& test -n "$JAR" \
	&& cp "$JAR" /opt/app.jar

FROM eclipse-temurin:25-jre-noble
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=production
# Render 無料 Web は 512MB。VPS の -Xmx384m 相当をコンテナ RAM 比で確保する。
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"
COPY --from=builder /opt/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
