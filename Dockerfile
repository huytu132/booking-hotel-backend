# ===== Stage 1: Build =====
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy POM và download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo user không phải root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR từ build stage
COPY --from=builder /app/target/*.jar app.jar

# Chuyển sang non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
