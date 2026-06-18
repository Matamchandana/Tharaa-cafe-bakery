# 🧁 Tharaa Café & Bakery — Full-Stack Storefront Application

A beautiful, high-performance web storefront built from the ground up to handle customer order box processing for a local confectionery. This application implements a decoupled architectural model to bind interactive DOM manipulation events to a transactional relational database schema.

## 🛠️ Tech Stack Architecture
* **Front-End Client:** Responsive Vanilla HTML5, CSS3 Custom Variables, and ES6 JavaScript (Fetch API, Async/Await).
* **Back-End API Layer:** Java 17+, Spring Boot Framework, Spring Data JPA, Hibernate ORM.
* **Relational Database:** MySQL 8.x (Implements One-to-Many relational constraints).

## 🚀 Key Functional Engineering Patterns
* **Decoupled Payload Delivery:** Implements strict DTO (Data Transfer Objects) payloads to ensure client web fields remain separate from system-persistent physical database records.
* **Atomic Transactions:** Designed a standard schema using intermediate intersection records (`order_items`) that allows complex parent orders to hold distinct product variations without structure leakage.

## 💾 Local Environment Spin-Up

### 1. Database Implementation
Execute the schema definitions inside your native MySQL server environment:
```sql
CREATE DATABASE bakerydb;
-- Paste the remaining table definition block scripts here
