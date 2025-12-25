# Stage 1: Build
FROM gradle:8.7 AS builder
WORKDIR /app

COPY . .
RUN ./gradlew --scan clean build shadowJar -x test

# Stage 2: Final image
FROM adoptopenjdk:11-jre-hotspot

RUN addgroup appuser && useradd -g appuser -ms /bin/bash appuser

RUN mkdir /app

RUN chown -R appuser:appuser /app

COPY --from=builder /app/build/libs/VectorSearch*all.jar /app/VectorSearch.jar

WORKDIR /app
USER appuser

# Run the application
ENTRYPOINT ["/opt/java/openjdk/bin/java", "-classpath", "VectorSearch.jar", "org.vsearch.Main"]
