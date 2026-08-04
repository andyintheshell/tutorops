# Build with the full JDK; it is not included in the runtime image.
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /build

# Copy wrapper metadata first so dependency layers can be reused.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/

# Run the complete Maven verification lifecycle in the image build.
RUN ./mvnw --batch-mode --no-transfer-progress verify \
    && cp target/tutorops-api-*.jar /build/app.jar

# Distroless provides a small JRE-only runtime with no shell or package manager.
# The nonroot variant runs as an unprivileged user by default.
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Keep the runtime unprivileged even if the base image defaults change.
USER 65532:65532

COPY --from=build /build/app.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
