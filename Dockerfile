# Usar uma imagem base do OpenJDK
FROM openjdk:21-jdk-slim

# Definir o diretório de trabalho no container
WORKDIR /app

# Copiar o arquivo JAR da aplicação para o diretório de trabalho
COPY build/libs/*.jar MyJWTAuthenticationAPI-0.0.1-SNAPSHOT.jar
# Expor a porta que a aplicação vai usar
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "MyJWTAuthenticationAPI-0.0.1-SNAPSHOT.jar"]
