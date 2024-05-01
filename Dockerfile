# Utiliza una imagen base de Java
FROM openjdk:11

# Copia tu archivo JAR al contenedor
COPY ./target/franks-lyrics.jar /usr/app/franks-lyrics.jar

# Establece el directorio de trabajo
WORKDIR /usr/app

# Ejecuta tu bot
CMD ["java", "-jar", "franks-lyrics.jar"]
