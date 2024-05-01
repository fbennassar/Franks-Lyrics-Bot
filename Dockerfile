# Utiliza una imagen base de Java
FROM openjdk:11

# Copia tu archivo JAR al contenedor
COPY ./target/LyricsBot.jar /usr/app/LyricsBot.jar

# Establece el directorio de trabajo
WORKDIR /usr/app

# Ejecuta tu bot
CMD ["java", "-jar", "LyricsBot.jar"]