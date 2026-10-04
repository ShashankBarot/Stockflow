# Stockflow - Stock Movement & Inventory Management System

A production-ready full-stack inventory management system built with **Next.js**, **Spring Boot**, and **MySQL**. Track stock, warehouses, purchases, sales, transfers, and inventory analytics with a modern data-driven interface.

## Tech Stack

| Layer    | Technology                                    |
| :------- | :-------------------------------------------- |
| Frontend | Next.js 14, TypeScript, Tailwind CSS, shadcn/ui, TanStack Query, Axios |
| Backend  | Spring Boot 3.3, Java 21, Spring Security, Spring Data JPA, JWT, Lombok |
| Database | MySQL 8.0                                     |
| DevOps   | Docker, Docker Compose                        |

## Project Structure

```
stock-management-system/
├── frontend/          # Next.js + TypeScript + Tailwind CSS + shadcn/ui
├── backend/           # Spring Boot 3.3 + Java 21
├── mysql/             # Database initialization scripts
├── docker-compose.yml # Docker Compose orchestration
├── .env.example       # Environment variable template
└── README.md
```

## Quick Start

### Prerequisites
- Java 21+
- Node.js 20+
- Maven 3.9+
- Docker & Docker Compose (optional, for containerized deployment)

### 1. Clone and Configure
```bash
cp .env.example .env
# Edit .env with your values
```

### 2. Start with Docker Compose (Recommended)
```bash
docker-compose up -d
```

### 3. Start Manually

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

### 4. Access
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html

## Default Credentials
- **Username:** admin
- **Password:** admin123

## Development Roadmap

| Phase | Features |
| :---- | :------- |
| Phase 1 | Authentication, Product CRUD, Warehouse CRUD, Basic Inventory |
| Phase 2 | Stock Movements (Inbound/Outbound/Transfer), Audit Trail |
| Phase 3 | Analytics Dashboard, Alerts, Reports, Barcode Scanning |

## API Documentation
The API is documented via Swagger/OpenAPI. Access it at `/api/v1/swagger-ui.html` when the backend is running.
