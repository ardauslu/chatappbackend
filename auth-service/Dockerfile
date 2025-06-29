# Auth Service Dockerfile
FROM eclipse-temurin:17-jdk-alpine

# Uygulama jar dosyasını kopyala
ARG JAR_FILE=target/auth-service-*.jar
COPY ${JAR_FILE} app.jar

# Portu aç
EXPOSE 8080

# Uygulamayı başlat
ENTRYPOINT ["java","-jar","/app.jar"]
