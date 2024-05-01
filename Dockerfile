# Utiliza una imagen base de Java
FROM openjdk:11

# Copia tu archivo JAR al contenedor
COPY ./target/franks-lyrics.jar /usr/app/franks-lyrics.jar

# Copia el archivo .env al directorio de trabajo en el contenedor
COPY .env /usr/app/.env

# Establece el directorio de trabajo
WORKDIR /usr/app

# Ejecuta tu bot
CMD ["java", "-jar", "franks-lyrics.jar"]
