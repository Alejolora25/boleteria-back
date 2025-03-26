# boleteria-back

# 🎟️ Boletería - Backend

Este es el backend del sistema de venta de boletos, desarrollado en **Java con Spring Boot**. Maneja la autenticación, la gestión de eventos, la venta de boletos y la generación de códigos QR para los boletos.

## 🚀 Características

- Gestión de eventos (creación, actualización, eliminación).
- Venta y administración de boletos.
- Autenticación y autorización con **JWT**.
- Generación de boletos en **PDF** con código **QR**.
- Base de datos **PostgreSQL**.
- API REST para la comunicación con el frontend.

## 🛠️ Tecnologías utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Security (JWT)**
- **PostgreSQL**
- **Hibernate (JPA)**
- **Lombok**
- **Swagger (Documentación de API)**

## 📦 Instalación

### Prerrequisitos
- Tener instalado **Java 17**.
- Tener configurado **PostgreSQL** y crear una base de datos.
- Configurar un archivo `application.properties` en `src/main/resources` con las credenciales de la BD.

### Pasos para correr el proyecto

```sh
git clone https://github.com/tu-usuario/boleteria-back.git
cd boleteria-back
mvn clean install
mvn spring-boot:run

La API estará disponible en http://localhost:8080.

📄 Documentación API
Una vez corriendo, puedes acceder a la documentación en:
http://localhost:8080/swagger-ui.html
✨ Autores
Alejandro Lora Tovar

📩 Contacto
Si deseas contribuir o reportar un problema, abre un issue o contáctanos en alejandroloratovar@outlook.com



