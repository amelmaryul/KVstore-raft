FROM openjdk:28-ea-slim AS builder

WORKDIR /kvstore

COPY src/ ./src

RUN javac -d target/ $(find src -name "*.java")



FROM openjdk:28-ea-slim

WORKDIR /kvstore/target

COPY --from=builder /kvstore/target .

CMD ["java", "-cp", ".", "Main", "5050"] 