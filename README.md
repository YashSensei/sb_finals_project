# URL Shortener with Analytics

A production-grade URL shortening service built with Spring Boot and MongoDB. Features include JWT authentication, analytics tracking, QR code generation, rate limiting, and email notifications.

## Features

### Core Features
- **URL Shortening**: Create short, memorable URLs with custom aliases
- **Analytics Dashboard**: Track clicks, geographic data, devices, browsers, and referrers
- **QR Code Generation**: Generate QR codes for any shortened URL
- **User Management**: Register, login, and manage user profiles
- **Role-Based Access**: User and Admin roles with different permissions

### Advanced Features
- **JWT Authentication**: Secure token-based authentication with refresh tokens
- **Rate Limiting**: Protect APIs with configurable rate limits (Bucket4j)
- **Caching**: High-performance caching with Caffeine
- **Password Protection**: Optional password protection for sensitive URLs
- **URL Expiration**: Set expiration dates for temporary links
- **Email Notifications**: Welcome emails and weekly analytics reports
- **Geolocation Tracking**: Track visitor locations using IP-API
- **File Upload**: Profile picture uploads with validation

### External Integration
- **IP Geolocation API**: Real-time visitor location tracking (ip-api.com)
- **SMTP Email**: Configurable email service (Gmail, SendGrid, etc.)

## Tech Stack

- **Backend**: Spring Boot 3.2
- **Database**: MongoDB
- **Security**: Spring Security + JWT
- **Documentation**: Swagger/OpenAPI 3.0
- **Caching**: Caffeine
- **Rate Limiting**: Bucket4j
- **QR Codes**: ZXing
- **Email**: Spring Mail

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

## Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB 6.0+

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd url-shortener
```

### 2. Configure MongoDB

Make sure MongoDB is running on `localhost:27017` or update the connection string in `application.yml`.

### 3. Configure Environment Variables

Copy `.env.example` to `.env` and update the values:

```bash
cp .env.example .env
```

### 4. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Access Swagger UI

Open `http://localhost:8080/swagger-ui.html` to view the API documentation.

## Default Admin User

On first startup, an admin user is created:
- **Email**: admin@urlshortener.com
- **Password**: admin123

**Change this password immediately in production!**

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | User logout |

### URLs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/urls` | Create short URL |
| GET | `/api/v1/urls` | Get user's URLs (paginated) |
| GET | `/api/v1/urls/{shortCode}` | Get URL details |
| PUT | `/api/v1/urls/{shortCode}` | Update URL |
| DELETE | `/api/v1/urls/{shortCode}` | Delete URL |
| POST | `/api/v1/urls/{shortCode}/qr` | Generate QR code |

### Redirect
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/r/{shortCode}` | Redirect to original URL |
| POST | `/r/{shortCode}/verify` | Verify password & redirect |
| GET | `/r/{shortCode}/preview` | Preview URL details |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/analytics/dashboard` | Get user dashboard |
| GET | `/api/v1/analytics/urls/{shortCode}` | Get URL analytics |

### QR Codes
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/qr/{shortCode}` | Get QR code image |
| GET | `/api/v1/qr/{shortCode}/download` | Download QR code |

### User Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get current user |
| PUT | `/api/v1/users/me` | Update profile |
| POST | `/api/v1/users/me/profile-picture` | Upload profile picture |
| DELETE | `/api/v1/users/me` | Deactivate account |

### Admin (Requires ADMIN role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/stats` | System statistics |
| GET | `/api/v1/admin/users` | List all users |
| GET | `/api/v1/admin/urls` | List all URLs |
| PUT | `/api/v1/admin/users/{id}/disable` | Disable user |
| PUT | `/api/v1/admin/users/{id}/enable` | Enable user |
| DELETE | `/api/v1/admin/urls/{id}` | Delete any URL |

## Configuration

### application.yml

Key configuration options:

```yaml
# JWT Settings
jwt:
  secret: your-secret-key
  expiration: 86400000        # 24 hours
  refresh-expiration: 604800000  # 7 days

# Rate Limiting
rate-limit:
  requests-per-minute: 60
  requests-per-hour: 1000

# URL Settings
app:
  base-url: http://localhost:8080
  default-expiration-days: 30
```

## Security Features

1. **JWT Authentication**: Stateless authentication with access and refresh tokens
2. **Password Hashing**: BCrypt password encoding
3. **Rate Limiting**: IP-based rate limiting to prevent abuse
4. **Input Validation**: Jakarta validation on all inputs
5. **CORS Configuration**: Configurable cross-origin settings
6. **Role-Based Access**: User and Admin roles

## Testing the API

### Register a new user
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Create short URL
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "originalUrl": "https://www.example.com/very-long-url",
    "customAlias": "my-link",
    "title": "Example Link",
    "generateQrCode": true
  }'
```

## Deployment

### Docker (Optional)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t url-shortener .
docker run -p 8080:8080 url-shortener
```

## License

MIT License

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
