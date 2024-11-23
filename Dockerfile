# Usa una imagen de Maven para construir la aplicación
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copia los archivos del proyecto al contenedor
COPY . .

# Compila el proyecto y genera el archivo .jar
RUN mvn clean package -DskipTests

# Usa una imagen de JDK ligera para ejecutar la aplicación
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia el archivo .jar generado desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto usado por la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]