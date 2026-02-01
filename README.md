# URL Shortener with Analytics

A production-grade URL shortening service built with Spring Boot and MongoDB. Features JWT authentication, analytics tracking, QR code generation, rate limiting, and more.

---

## About The Project

### What is a URL Shortener?
A URL shortener is a web service that converts long, complex URLs into short, easy-to-share links. For example, a URL like `https://example.com/products/category/electronics/item?id=12345&ref=campaign` becomes something like `http://localhost:8080/r/abc123`.

### Why is this useful?
- **Easier Sharing** - Short URLs are easier to share on social media, SMS, or verbally
- **Tracking & Analytics** - Track how many people clicked your links, where they came from, what devices they used
- **Branding** - Create custom aliases like `/r/myproduct` instead of random characters
- **Security** - Add password protection to sensitive links
- **Temporary Links** - Set expiration dates for time-limited campaigns

### How It Works
1. **User registers/logs in** → Gets a JWT token for authentication
2. **User submits a long URL** → System generates a unique short code (or uses custom alias)
3. **Short URL is created** → e.g., `http://localhost:8080/r/abc123`
4. **Someone clicks the short URL** → System records analytics (IP, browser, device, location) and redirects to original URL
5. **User views analytics** → Dashboard shows all click data, graphs, and insights

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

The application uses **MongoDB Atlas** (cloud database) - no local database setup required!

### Access Points
| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |

### Default Admin Login
- **Email**: `admin@urlshortener.com`
- **Password**: `admin123`

---

## Features Explained

### 1. User Authentication System
The application uses **JWT (JSON Web Tokens)** for secure, stateless authentication.

**How it works:**
- When a user logs in, the server generates two tokens:
  - **Access Token** (short-lived, 15 mins) - Used for API requests
  - **Refresh Token** (long-lived, 7 days) - Used to get new access tokens without re-login
- Tokens are stored in the database and validated on each request
- Passwords are hashed using **BCrypt** (industry-standard, one-way encryption)

**Endpoints:**
- `POST /api/v1/auth/register` - Create new account
- `POST /api/v1/auth/login` - Login and get tokens
- `POST /api/v1/auth/refresh` - Get new access token using refresh token
- `POST /api/v1/auth/logout` - Invalidate tokens

### 2. URL Shortening
The core feature that converts long URLs to short ones.

**Features:**
- **Auto-generated codes** - System creates random 6-character codes (e.g., `xK9mPq`)
- **Custom aliases** - Users can specify their own short code (e.g., `mylink`)
- **Password protection** - Optional password required to access the URL
- **Expiration dates** - URLs can auto-expire after a set date
- **Active/Inactive toggle** - Temporarily disable URLs without deleting them

**How short codes are generated:**
- Uses a combination of uppercase, lowercase letters and numbers (62 characters)
- 6-character codes = 62^6 = 56+ billion possible combinations
- System checks for uniqueness before saving

### 3. Analytics & Tracking
Every click on a shortened URL is tracked and analyzed.

**What is tracked:**
- **Click count** - Total number of times the URL was accessed
- **Browser** - Chrome, Firefox, Safari, Edge, etc.
- **Operating System** - Windows, macOS, Linux, Android, iOS
- **Device Type** - Desktop, Mobile, Tablet
- **Referrer** - Where the click came from (Google, Facebook, Direct, etc.)
- **Geographic Location** - Country and city (using IP-based geolocation via ip-api.com)
- **Timestamp** - When each click occurred

**Analytics Endpoints:**
- `GET /api/v1/analytics/dashboard` - Overview of all user's URLs
- `GET /api/v1/analytics/urls/{shortCode}` - Detailed analytics for specific URL

### 4. QR Code Generation
Generate scannable QR codes for any shortened URL.

**How it works:**
- Uses **ZXing library** (Google's open-source barcode library)
- Generates PNG images dynamically
- QR codes can be viewed in browser or downloaded

**Endpoints:**
- `GET /api/v1/qr/{shortCode}` - View QR code as image
- `GET /api/v1/qr/{shortCode}/download` - Download QR code as PNG file

### 5. Rate Limiting
Protects the API from abuse and ensures fair usage.

**How it works:**
- Uses **Bucket4j** library (token bucket algorithm)
- Each IP address gets a "bucket" of tokens
- Each API request consumes a token
- Tokens refill over time
- When bucket is empty, requests are rejected with HTTP 429 (Too Many Requests)

**Current limits:**
- 100 requests per minute per IP address

### 6. Caching
Improves performance by storing frequently accessed data in memory.

**How it works:**
- Uses **Caffeine** (high-performance Java caching library)
- URL lookups are cached to reduce database queries
- Cache automatically expires old entries
- Significantly improves redirect speed for popular URLs

### 7. Admin Panel
Special endpoints for system administrators.

**Capabilities:**
- View system-wide statistics (total users, URLs, clicks)
- List all users in the system
- List all URLs across all users
- Monitor system health

**Endpoints (require ADMIN role):**
- `GET /api/v1/admin/stats` - System statistics
- `GET /api/v1/admin/users` - All users (paginated)
- `GET /api/v1/admin/urls` - All URLs (paginated)

---

## Tech Stack

| Category | Technology | Why We Used It |
|----------|------------|----------------|
| Backend Framework | Spring Boot 3.2 | Industry-standard Java framework, easy to develop REST APIs |
| Database | MongoDB Atlas | NoSQL database, flexible schema, free cloud hosting |
| Security | Spring Security + JWT | Robust security framework with stateless token authentication |
| API Documentation | Swagger/OpenAPI 3.0 | Auto-generates interactive API documentation |
| Caching | Caffeine | Fastest in-memory cache for Java |
| Rate Limiting | Bucket4j | Token bucket algorithm implementation |
| QR Codes | ZXing | Google's reliable barcode/QR library |
| Geolocation | IP-API.com | Free IP-to-location service |

---

## Architecture Overview

### Layered Architecture
The project follows a clean **layered architecture** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT (Browser/Postman)               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     CONTROLLER LAYER                        │
│  (Handles HTTP requests, input validation, response format) │
│  AuthController, UrlController, AnalyticsController, etc.   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                          │
│  (Business logic, data processing, external API calls)      │
│  AuthService, UrlService, AnalyticsService, etc.            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                         │
│  (Database operations, queries)                             │
│  UserRepository, UrlRepository, ClickEventRepository        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     MongoDB DATABASE                        │
│  (Stores users, URLs, tokens, click events)                 │
└─────────────────────────────────────────────────────────────┘
```

### Request Flow Example
When someone clicks a short URL (`http://localhost:8080/r/abc123`):

1. **Request hits RedirectController**
2. **Controller calls UrlService.redirect()**
3. **Service checks cache** for the URL
4. **If not cached, queries MongoDB** via UrlRepository
5. **Service validates URL** (active, not expired, not password-protected)
6. **Service records click event** asynchronously (browser, device, location)
7. **Service returns original URL** to controller
8. **Controller sends HTTP 302 redirect** to browser
9. **Browser navigates** to original URL

---

## Project Structure

```
src/main/java/com/urlshortener/
├── config/          # Configuration classes (Security, Swagger, CORS, Cache)
├── controller/      # REST API controllers (handle HTTP requests)
├── dto/             # Data Transfer Objects (request/response formats)
│   ├── request/     # What client sends (CreateUrlRequest, LoginRequest)
│   └── response/    # What server returns (UrlResponse, AnalyticsResponse)
├── exception/       # Custom exceptions & global error handler
├── filter/          # Security filters (JWT authentication filter)
├── model/           # MongoDB entities (User, Url, ClickEvent, Token)
│   └── enums/       # Enumerations (Role, DeviceType, BrowserType)
├── repository/      # MongoDB repositories (database operations)
├── service/         # Business logic layer
└── util/            # Utility classes (JwtUtil, UserAgentParser)
```

---

## Database Schema

### Collections (Tables in MongoDB)

**1. users**
```json
{
  "_id": "ObjectId",
  "email": "user@example.com",
  "password": "$2a$10$...(hashed)",
  "name": "John Doe",
  "role": "USER",
  "active": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**2. urls**
```json
{
  "_id": "ObjectId",
  "originalUrl": "https://very-long-url.com/...",
  "shortCode": "abc123",
  "userId": "ObjectId (reference to user)",
  "clickCount": 150,
  "active": true,
  "passwordProtected": false,
  "password": null,
  "expiresAt": null,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**3. click_events**
```json
{
  "_id": "ObjectId",
  "urlId": "ObjectId (reference to url)",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "browser": "CHROME",
  "os": "WINDOWS",
  "deviceType": "DESKTOP",
  "referrer": "https://google.com",
  "country": "India",
  "city": "Mumbai",
  "clickedAt": "2024-01-01T12:30:00Z"
}
```

**4. tokens**
```json
{
  "_id": "ObjectId",
  "userId": "ObjectId (reference to user)",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "revoked": false,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

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

### Complete API Reference

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

## Security Features

| Feature | Implementation | Purpose |
|---------|----------------|---------|
| JWT Authentication | Access + Refresh tokens | Stateless, scalable authentication |
| Password Hashing | BCrypt (strength 10) | Secure one-way password storage |
| Rate Limiting | Bucket4j (100 req/min) | Prevent API abuse and DDoS |
| Input Validation | Jakarta Bean Validation | Prevent injection attacks |
| CORS Configuration | Spring Security | Control cross-origin requests |
| Role-Based Access | USER/ADMIN roles | Restrict sensitive operations |

---

## License

MIT License

---

## Author

**Yash** - [GitHub](https://github.com/YashSensei)
