FROM eclipse-temurin:25-jdk-alpine
WORKDIR app
COPY build/libs/deps/ ./deps/
COPY run.sh ./
RUN chmod +x /app/run.sh
COPY build/libs/*.jar ./
ENTRYPOINT ["/app/run.sh"]
