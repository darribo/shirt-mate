# 👕 ShirtMate — Sistema de Gestión de Camisetas y Sorteos

ShirtMate es una aplicación **full-stack** para gestionar camisetas, grupos de usuarios, colaboradores, responsables y sorteos internos.  
Fue creada originalmente para **practicar desarrollo real**, pero se diseñó desde un punto de vista plenamente **ingenieril**, ya que resolvía una necesidad real de gestión, con un backend en **Spring Boot + MySQL** y un frontend de escritorio en **Python + GTK4** siguiendo el patrón **MVP**.

---

## 🚀 Funcionalidades principales

- Gestión de **clientes**, **colaboradores** y **responsables**.  
- Registro y venta de **camisetas** y **tipos de camiseta**.  
- Gestión de niveles y retorno de inversión para colaboradores.  
- Configuración de **rifas** basadas en número de participantes.  
- Determinación automática de ganadores.  
- Cálculo de beneficios, márgenes y análisis económico.  
- Frontend propio en Python que consume la API REST del backend.  

---

## 🧱 Tecnologías utilizadas

### Backend (Java)
- **Spring Boot**
- **Spring Web / REST**
- **Spring Data JPA**
- **Hibernate**
- **MySQL**
- **Spring Validation**
- **Mockito + JUnit 5**
- **Maven**
- **springdoc-openapi (Swagger UI)**

### Frontend (Python)
- **Python 3**
- **GTK4 (PyGObject)**
- **Patrón MVP**
- **Requests para consumo de API**
- **Diseño modular por vistas**

---

## 📂 Estructura del repositorio

```
backend/
│── src/main/java/...
│── src/test/java/...
│── src/sql/MySQLCreateTables.sql
│── pom.xml
│── application.yml

frontend/
│── *.py (Views, Models, Presenters)
│── icons/
│── requirements.txt

docs/
│── architecture.md
│── Diagrama_Clases.png

README.md
LICENSE
```

---

## 🔌 Comunicación Frontend ↔ Backend

Toda la comunicación se realiza mediante:

```
HTTP + JSON (REST API)
```

Ejemplos:

- `GET /collaborators`
- `GET /shirts/shirtType/{id}`
- `POST /shirts/shirt`
- `GET /raffles/raffle/{id}`
- `POST /raffles/level/play/{id}`

---

## 🧪 Testing

El backend incluye:
- Tests unitarios de servicios  
- Tests transaccionales con rollback automático  

Se validan:
- Reglas de negocio  
- Excepciones personalizadas  
- Cálculos económicos  
- Lógica de sorteo  

---

## 📦 Instalación

### 🗄️ Requisitos de base de datos (MySQL)

El backend está configurado para usar **estas bases de datos y credenciales**, definidas en el `pom.xml`:

- Base de datos principal: `camisetas`  
- Base de datos de tests: `camisetastest`  
- Usuario: `camisetas`  
- Contraseña: `camisetas`  

1️⃣ **Crear bases de datos y usuario**

Ejecutar en MySQL:

```sql
CREATE DATABASE camisetas CHARACTER SET latin1 COLLATE latin1_bin;
CREATE DATABASE camisetastest CHARACTER SET latin1 COLLATE latin1_bin;

CREATE USER 'camisetas'@'localhost' IDENTIFIED BY 'camisetas';

GRANT ALL PRIVILEGES ON camisetas.* TO 'camisetas'@'localhost';
GRANT ALL PRIVILEGES ON camisetastest.* TO 'camisetas'@'localhost';
```

2️⃣ **Crear tablas**

El esquema de la base de datos está definido en:

```
backend/src/sql/MySQLCreateTables.sql
```

Puedes ejecutarlo manualmente:

```bash
mysql -u camisetas -p camisetas < backend/src/sql/MySQLCreateTables.sql
mysql -u camisetas -p camisetastest < backend/src/sql/MySQLCreateTables.sql
```

---

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

El backend se levantará por defecto en:

```
http://localhost:8080
```

---

### Frontend

El frontend de escritorio se conecta a la API REST expuesta por el backend en `http://localhost:8080`.

---

## 🧩 Dependencias necesarias para ejecutar el frontend (GTK4 / PyGObject)

El frontend está construido con **GTK4 (PyGObject)**.  
Estas librerías **NO se instalan mediante pip**, sino que deben estar instaladas en el sistema.

En Ubuntu/Linux instala:

```bash
sudo apt install python3-gi gir1.2-gtk-4.0 libgtk-4-dev gir1.2-adw-1

Una vez instaladas, puedes activar el entorno virtual e instalar las dependencias Python:

cd frontend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python3 Main.py

Si no instalas las dependencias del sistema, aparecerá el error:

ModuleNotFoundError: No module named 'gi'

---

## 📈 Estado del proyecto

Proyecto desarrollado en verano de **2025** y subido ahora a GitHub para:

- conservarlo públicamente,  
- documentar aprendizaje,  
- mostrar arquitectura full-stack real.  

Aunque no es un producto profesional, incluye un diseño limpio, modular y ampliable.

---

## 📘 Documentación de la API (Swagger / OpenAPI)

La documentación completa de todos los controladores, endpoints, parámetros y modelos de la API REST está disponible en:

```
http://localhost:8080/docs
```

Incluye:
- Descripciones completas  
- Ejemplos de uso  
- Modelos (DTOs)  
- Códigos de respuesta  
- Errores personalizados (`ErrorsDto`, `FieldErrorDto`)  

---

## 📄 Licencia

Este proyecto está publicado bajo licencia **MIT**.
