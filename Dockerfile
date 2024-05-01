# Utiliza una imagen base que incluye Java y Maven
FROM maven:3.6.3-openjdk-11 as build

# Establece el directorio de trabajo
WORKDIR /app

# Copia tu código fuente al contenedor
COPY src /app/src
COPY pom.xml /app
COPY .env /app   # Asegúrate de que tu archivo .env está en la misma carpeta que tu Dockerfile

# Construye la aplicación
RUN mvn package

# Inicia una nueva etapa de construcción para la imagen final
FROM openjdk:11

# Establece el directorio de trabajo
WORKDIR /usr/app

# Copia el archivo JAR de la etapa de construcción
COPY --from=build /app/target/franks-lyrics-jar-with-dependencies.jar /usr/app/franks-lyrics-jar-with-dependencies.jar

# Copia el archivo .env de la etapa de construcción
COPY --from=build /app/.env /usr/app/.env

# Ejecuta tu bot
CMD ["java", "-jar", "franks-lyrics-jar-with-dependencies.jar"]
