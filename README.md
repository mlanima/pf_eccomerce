# Horse Riding E-commerce Platform

A specialized online marketplace for equestrian equipment and accessories built with Spring Boot and Next.js.

## Technology Stack

- **Backend**: Java Spring Boot 3.5.3, MySQL 8.4, Spring Security
- **Frontend**: Next.js 15.4, React 19, TypeScript, Tailwind CSS, Framer Motion
- **Payment**: PayPal JavaScript SDK
- **Deployment**: Docker & Docker Compose

## Project Structure

```
horse-riding-ecommerce/
├── backend/                 # Spring Boot application
├── frontend/               # Next.js application
├── docker-compose.yml      # Multi-service container orchestration
├── .env                    # Environment variables
└── README.md              # Project documentation
```

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- MySQL 8.4

### Development Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and configure environment variables
3. Start services with Docker Compose:
   ```bash
   docker-compose up -d
   ```

### Backend Development

```bash
cd backend
mvn spring-boot:run
```

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## Features

- Product catalog with search and filtering
- Shopping cart and PayPal checkout
- User authentication and account management
- Admin dashboard for product and order management
- Role-based access control (Customer, Admin, Superadmin)
- Responsive design with smooth animations

## API Documentation

The REST API follows standard conventions:

- Base URL: `/api`
- Authentication: JWT tokens
- Error handling: Standard HTTP status codes
