# 📐 Arquitectura del Sistema – ShirtMate

Este documento describe la arquitectura del sistema **ShirtMate**, una aplicación full-stack para la gestión de camisetas, grupos de usuarios, colaboradores, clientes y sorteos internos.  
El sistema está compuesto por:

- Backend en Spring Boot  
- Frontend de escritorio en Python + GTK4  
- Base de datos MySQL  
- Comunicación REST  
- Arquitectura por capas y patrón MVP  

---

# 🧱 1. Arquitectura general

```
 ┌───────────────────────┐
 │      Frontend         │
 │   Python + GTK4       │
 │      (MVP)            │
 └───────────▲───────────┘
             │ REST (JSON)
 ┌───────────┴───────────┐
 │       Backend          │
 │     Spring Boot        │
 │  Controllers / DTOs    │
 │     Services / DAO     │
 └───────────▲───────────┘
             │ JPA
 ┌───────────┴───────────┐
 │     MySQL Database     │
 │  (Tablas del negocio)  │
 └────────────────────────┘
```

El frontend se comunica exclusivamente con el backend mediante API REST.  
El backend gestiona toda la lógica de negocio y realiza el acceso a datos mediante JPA.  
MySQL almacena la información principal de clientes, colaboradores, camisetas, tipos, niveles y rifas.

---

# 🧩 2. Backend — Arquitectura por capas

El backend sigue una arquitectura clara basada en controladores, servicios, DAO y entidades.

## 2.1 Controladores (API REST)

Ubicados en `controller/`.

Responsabilidades:
- Recibir peticiones HTTP  
- Validar parámetros de entrada  
- Gestionar errores mediante CommonControllerAdvice y errores personalizados  
- Convertir Entities ↔ DTOs  
- Devolver respuestas JSON  

Principales controladores:
- CustomerController  
- CollaboratorController  
- RaffleController  

---

## 2.2 Servicios (Lógica de negocio)

Ubicados en `service/` y `service.impl/`.

Responsabilidades:
- Encapsular la lógica de negocio real  
- Manejar transacciones  
- Validar reglas de negocio  
- Lanzar excepciones personalizadas  
- Orquestar operaciones entre entidades  

Ejemplos de reglas implementadas:
- Cálculo de beneficios por camiseta  
- Retorno de inversión de colaboradores  
- Gestión del “convincedBy” y “convinces” en Customer  
- Asignación de responsables por tipo de camiseta  
- Lógica de niveles y participantes para rifas  
- Determinación del ganador de una rifa  

Servicios clave:
- CustomerServiceImpl  
- CollaboratorServiceImpl  
- ShirtServiceImpl  
- RaffleServiceImpl  

---

## 2.3 DAO / Repositorios (Acceso a datos)

Ubicados en `dao/`.

Cada entidad tiene su respectivo repositorio Spring Data JPA:

- CustomerDao  
- ResponsibleDao  
- CollaboratorDao  
- ShirtDao  
- ShirtTypeDao  
- RaffleDao  
- LevelDao  

Responsabilidades:
- Consultas automáticas mediante JPARepository  
- Consultas personalizadas cuando es necesario  
- Persistencia de entidades  

---

## 2.4 Capa de Dominio (Entidades)

La capa de dominio contiene las entidades principales:

- Customer  
- Collaborator  
- Responsible  
- Shirt  
- ShirtType  
- Raffle  
- Level  

Y enums como:
- Size  

Relaciones destacadas:
- Customer ↔ Shirt (1:N)  
- Collaborator ↔ Shirt (1:N)  
- ShirtType ↔ Shirt (1:N)  
- Responsible ↔ ShirtType (1:N)  
- Customer ↔ Customer (convinces / convincedBy)  
- Raffle ↔ Level (1:N)  
- Level ↔ Customer (ganador)  
- Raffle ↔ ShirtType (0..1)  

---

# 🧮 3. Base de datos — Esquema general

Definida en `MySQLCreateTables.sql`.

Relaciones principales reflejadas en la BD:

- Customer compra Shirts  
- Collaborator invierte en Shirts  
- ShirtType tiene un Responsible  
- Raffle contiene uno o varios Levels  
- Cada Level puede tener un ganador  
- Customer puede convencer a otros Customers  
- Shirt pertenece a un ShirtType  
- Raffle se asocia a un ShirtType  

El modelo soporta:
- Ventas para grupos de personas  
- Seguimiento de colaboradores y clientes  
- Cálculo de beneficios e inversiones  
- Sorteos internos según número de participantes  

---

# 🖥️ 4. Frontend — Arquitectura MVP (Modelo-Vista-Presentador)

El frontend está implementado en Python + GTK4 siguiendo el patrón MVP.

```
View (GTK4) ←→ Presenter ←→ Model (HTTP/API)
```

## 4.1 Vista (View)

Ubicadas en archivos `*View.py`.

Responsabilidades:
- Renderizado con GTK4  
- Manejo de widgets  
- Eventos de interfaz  
- Sin lógica de negocio  

## 4.2 Presentador (Presenter)

En `Presenter.py`.

Responsabilidades:
- Coordinar Vista ↔ Modelo  
- Validación básica  
- Actualizar la vista según resultados del modelo  
- Manejar errores visuales  

## 4.3 Modelo (Model)

Ubicados en `*Model.py`.

Responsabilidades:
- Realizar peticiones HTTP al backend  
- Parsear JSON a objetos Python  
- Manejar errores de red  
- No contiene lógica visual  
