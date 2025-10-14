# ===== build =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Trae dependencias al cache
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Compila y reempaca el jar ejecutable de Spring Boot
COPY src ./src
RUN mvn -q -e -DskipTests clean package spring-boot:repackage

# ===== runtime =====
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Necesario para el HEALTHCHECK
RUN apt-get update && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 👇 OJO: esta línea depende de la etapa llamada EXACTAMENTE "build"
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
  CMD curl -fsS http://localhost:8080/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

