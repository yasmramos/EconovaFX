# 🐳 EconoNova FX - Docker Configuration (Future Enhancement)

## Overview

This document outlines the planned Docker configuration for EconoNova FX to support containerized deployment in production environments.

## Planned Architecture

### Development Environment
```yaml
version: '3.8'
services:
  app:
    build: .
    volumes:
      - ./data:/app/data
    environment:
      - DB_TYPE=h2
      - JAVA_OPTS=-Xmx512m
    ports:
      - "8080:8080"
```

### Production Environment
```yaml
version: '3.8'
services:
  app:
    image: econovafx:latest
    volumes:
      - ./config:/app/config
      - ./logs:/app/logs
    environment:
      - DB_TYPE=mysql
      - DB_HOST=db
      - DB_PORT=3306
      - DB_NAME=econovafx
      - DB_USER=econova_user
      - DB_PASSWORD=${DB_PASSWORD}
      - JAVA_OPTS=-Xmx2g
    depends_on:
      - db
    ports:
      - "8080:8080"
  
  db:
    image: mysql:8.0
    volumes:
      - mysql_data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=econovafx
      - MYSQL_USER=econova_user
      - MYSQL_PASSWORD=${DB_PASSWORD}
    ports:
      - "3306:3306"

volumes:
  mysql_data:
```

## Dockerfile Specification

```dockerfile
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="yasmramos95@gmail.com"
LABEL version="0.1.0"
LABEL description="EconoNova FX - Professional Accounting System"

# Install dependencies
RUN apk add --no-cache fontconfig ttf-dejavu

# Create app directory
WORKDIR /app

# Copy application JAR
COPY target/econovafx-*.jar app.jar

# Create directories for data and logs
RUN mkdir -p /app/data /app/logs /app/config

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Djava.awt.headless=true"
ENV DB_TYPE="h2"

# Expose port (if running in server mode)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD nc -z localhost 8080 || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Build Instructions

### Build Docker Image
```bash
# Build application first
mvn clean package -DskipTests

# Build Docker image
docker build -t econovafx:latest .
```

### Run with Docker Compose
```bash
# Development
docker-compose up -d

# Production
docker-compose -f docker-compose.prod.yml up -d
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JAVA_OPTS` | `-Xmx512m` | JVM options |
| `DB_TYPE` | `h2` | Database type (h2, mysql, postgresql) |
| `DB_HOST` | `localhost` | Database host |
| `DB_PORT` | `3306` | Database port |
| `DB_NAME` | `econovafx` | Database name |
| `DB_USER` | `sa` | Database username |
| `DB_PASSWORD` | `` | Database password |

## Volume Mapping

### Persistent Data
- `/app/data` - Application data (H2 database files)
- `/app/logs` - Application logs
- `/app/config` - Configuration files

## Security Considerations

1. **Never hardcode passwords** - Use environment variables or secrets management
2. **Run as non-root user** - Add USER directive in Dockerfile
3. **Use official base images** - eclipse-temurin for Java
4. **Minimize layers** - Multi-stage builds for smaller images
5. **Scan for vulnerabilities** - Regular security scans

## Future Enhancements

1. **Multi-stage build** for smaller image size
2. **Health checks** for container orchestration
3. **Logging drivers** for centralized logging
4. **Network policies** for container isolation
5. **Kubernetes manifests** for orchestration
6. **Helm chart** for easy deployment

---

**Status**: 📋 Planned  
**Priority**: Medium  
**Target Version**: 1.0.0  
**Last Updated**: August 2024
