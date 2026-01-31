# URL Shortener with Analytics

A production-grade URL shortening service built with Spring Boot and MongoDB. Features JWT authentication, analytics tracking, QR code generation, rate limiting, and more.

---

## Quick Start

### Prerequisites
- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)

### Run the Application
```bash
# Clone the repository
git clone https://github.com/YashSensei/sb_finals_project.git
cd sb_finals_project

# Run the application
mvn spring-boot:run
```

### Access Points
| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |

### Default Admin Login
- **Email**: `admin@urlshortener.com`
- **Password**: `admin123`

---

## Features

### Core Features
- **URL Shortening** - Create short URLs with custom aliases
- **Analytics Dashboard** - Track clicks, locations, devices, browsers, referrers
- **QR Code Generation** - Generate QR codes for shortened URLs
- **User Management** - Register, login, manage profiles
- **Role-Based Access** - User and Admin roles

### Advanced Features
- **JWT Authentication** - Secure token-based auth with refresh tokens
- **Rate Limiting** - API protection with Bucket4j
- **Caching** - High-performance caching with Caffeine
- **Password Protection** - Optional password for sensitive URLs
- **URL Expiration** - Set expiration dates for temporary links
- **Geolocation Tracking** - Track visitor locations via IP-API

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Backend | Spring Boot 3.2 |
| Database | MongoDB Atlas |
| Security | Spring Security + JWT |
| Documentation | Swagger/OpenAPI 3.0 |
| Caching | Caffeine |
| Rate Limiting | Bucket4j |
| QR Codes | ZXing |

---

## API Testing

### Postman Collection
Import `sb_final_postman_demo.json` into Postman for ready-to-use API requests.

The collection includes:
- Authentication (Login, Register, Refresh, Logout)
- URL Management (Create, Read, Update, Delete)
- URL Features (Custom Alias, Password Protection, Expiration)
- Analytics (Dashboard, URL Stats)
- QR Code Generation
- User Profile Management
- Admin Operations

### API Endpoints

#### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | User logout |

#### URLs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/urls` | Create short URL |
| GET | `/api/v1/urls` | Get user's URLs (paginated) |
| GET | `/api/v1/urls/{shortCode}` | Get URL details |
| PUT | `/api/v1/urls/{shortCode}` | Update URL |
| DELETE | `/api/v1/urls/{shortCode}` | Delete URL |

#### Redirect
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/r/{shortCode}` | Redirect to original URL |
| POST | `/r/{shortCode}/verify` | Verify password & redirect |
| GET | `/r/{shortCode}/preview` | Preview URL details |

#### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/analytics/dashboard` | Get user dashboard |
| GET | `/api/v1/analytics/urls/{shortCode}` | Get URL analytics |

#### QR Codes
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/qr/{shortCode}` | Get QR code image |
| GET | `/api/v1/qr/{shortCode}/download` | Download QR code |

#### User Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get current user |
| PUT | `/api/v1/users/me` | Update profile |
| DELETE | `/api/v1/users/me` | Deactivate account |

#### Admin (Requires ADMIN role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/stats` | System statistics |
| GET | `/api/v1/admin/users` | List all users |
| GET | `/api/v1/admin/urls` | List all URLs |

---

## Project Structure

```
src/main/java/com/urlshortener/
├── config/          # Configuration classes
├── controller/      # REST API controllers
├── dto/             # Data Transfer Objects
│   ├── request/     # Request DTOs
│   └── response/    # Response DTOs
├── exception/       # Custom exceptions & global handler
├── filter/          # Security filters
├── model/           # MongoDB entities
│   └── enums/       # Enumerations
├── repository/      # MongoDB repositories
├── service/         # Business logic
└── util/            # Utility classes
```

---

## Security Features

- **JWT Authentication** - Stateless auth with access and refresh tokens
- **Password Hashing** - BCrypt password encoding
- **Rate Limiting** - IP-based rate limiting to prevent abuse
- **Input Validation** - Jakarta validation on all inputs
- **CORS Configuration** - Configurable cross-origin settings
- **Role-Based Access** - User and Admin roles

---

## License

MIT License

---

## Author

**Yash** - [GitHub](https://github.com/YashSensei)
