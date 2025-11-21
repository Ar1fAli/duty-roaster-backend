# Dockerfile (Render-ready, multi-stage)
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# copy only pom first for layer caching of dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn -B -f pom.xml clean package -DskipTests

# runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# copy artifact (assumes your build creates target/*.jar)
COPY --from=build /app/target/*.jar app.jar

# expose default port (not strictly required, but useful)
EXPOSE 8081

# Use Render's $PORT if set, otherwise fallback to 8081.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -jar /app/app.jar"]

