# ⚡ TaskFlow — Full Stack Task & Productivity Management System

A production-style, full-stack web application built with **Java Spring Boot** (backend) and **React.js** (frontend), using **MySQL** as the database. Ideal for fresher resumes and technical interviews.

---

## 🖼️ Application Screenshots (UI Description)

### 1. Login Page
- Dark-themed authentication page with animated gradient orbs in the background
- Logo with lightning bolt icon, clean card layout
- Username/email + password form with show/hide toggle
- Demo credentials hint box

### 2. Dashboard
- Greeting card with user's first name
- **4 Stat Cards**: Total Tasks, In Progress, Completed, Overdue
- **Bar Chart**: Tasks grouped by status (To Do / In Progress / Completed / Cancelled)
- **Pie Chart**: Task priority distribution (Low / Medium / High / Urgent)
- **Recent Tasks** list and **Due This Week** panel

### 3. Tasks Page
- Full task list with card grid layout
- Color-coded priority bars on each card (green=low, yellow=medium, orange=high, red=urgent)
- Status badges and overdue indicators
- Search bar + filter panel (by status, priority, category)
- Create/Edit task modal with all fields
- Three-dot context menu per card: Complete, Edit, Delete

### 4. Profile Page
- Avatar with user initials, full name, role badge
- Task statistics with completion rate progress bar
- Editable profile form (first name, last name, bio)
- Inline password change form with validation

---

## 🏗️ Project Structure

```
taskflow/
├── backend/                          # Spring Boot Application
│   ├── pom.xml                       # Maven dependencies
│   └── src/main/
│       ├── java/com/taskflow/
│       │   ├── TaskFlowApplication.java      # Main entry point
│       │   ├── config/
│       │   │   └── SecurityConfig.java       # Spring Security + CORS
│       │   ├── controller/
│       │   │   ├── AuthController.java       # /api/auth/*
│       │   │   ├── TaskController.java       # /api/tasks/*
│       │   │   └── UserController.java       # /api/users/*
│       │   ├── service/
│       │   │   ├── AuthService.java          # Login/Register logic
│       │   │   ├── TaskService.java          # Task CRUD + stats
│       │   │   └── UserService.java          # Profile management
│       │   ├── repository/
│       │   │   ├── UserRepository.java       # User DB queries
│       │   │   ├── RoleRepository.java       # Role DB queries
│       │   │   └── TaskRepository.java       # Task DB queries
│       │   ├── entity/
│       │   │   ├── User.java                 # User JPA entity
│       │   │   ├── Role.java                 # Role JPA entity
│       │   │   └── Task.java                 # Task JPA entity
│       │   ├── dto/
│       │   │   └── TaskFlowDTOs.java         # All DTOs (request/response)
│       │   ├── security/
│       │   │   ├── JwtUtils.java             # JWT generate/validate
│       │   │   ├── AuthTokenFilter.java      # JWT per-request filter
│       │   │   ├── UserDetailsImpl.java      # Spring Security adapter
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── exception/
│       │       └── GlobalExceptionHandler.java  # Centralized error handling
│       └── resources/
│           ├── application.properties        # App configuration
│           └── schema.sql                    # DB schema + seed data
│
└── frontend/                         # React Application
    ├── package.json
    └── src/
        ├── App.js                    # Root component + routing
        ├── index.js                  # ReactDOM entry point
        ├── context/
        │   └── AuthContext.js        # Global auth state (useAuth hook)
        ├── services/
        │   └── api.js                # Axios configuration + API functions
        ├── pages/
        │   ├── LoginPage.js          # Login form
        │   ├── RegisterPage.js       # Registration form
        │   ├── DashboardPage.js      # Stats + charts
        │   ├── TasksPage.js          # Task CRUD UI
        │   └── ProfilePage.js        # Profile management
        ├── components/
        │   └── layout/
        │       ├── Layout.js         # Sidebar + topbar shell
        │       └── Layout.css
        └── styles/
            └── global.css            # Design system + global styles
```

---

## 🚀 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Framework | Spring Boot 3.2 |
| REST API | Spring MVC (REST) |
| Database ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8+ |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Build Tool | Maven |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Frontend | React 18 |
| HTTP Client | Axios |
| Charts | Recharts |
| Icons | Lucide React |
| Routing | React Router DOM v6 |
| Notifications | React Hot Toast |
| Date Utils | date-fns |

---

## 🛠️ Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- Node.js 18+ and npm
- Git

---

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/taskflow.git
cd taskflow
```

---

### Step 2: Set Up the MySQL Database

```bash
# Log into MySQL
mysql -u root -p

# Run the schema script
source backend/src/main/resources/schema.sql;

# Or manually:
CREATE DATABASE taskflow_db;
```

---

### Step 3: Configure the Backend

Edit `backend/src/main/resources/application.properties`:

```properties
# Update with your MySQL credentials
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE

# Change this to a strong random string in production!
app.jwt.secret=your_super_secure_jwt_secret_key_here
```

---

### Step 4: Run the Spring Boot Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start at: **http://localhost:8080**

✅ Swagger UI: **http://localhost:8080/swagger-ui.html**

---

### Step 5: Run the React Frontend

```bash
cd frontend
npm install
npm start
```

The frontend will start at: **http://localhost:3000**

---

### Step 6: Test the Application

1. Go to **http://localhost:3000/register** → Create an account
2. Or use demo: **admin / password123**
3. Explore the Dashboard, Tasks, and Profile pages

---

## 📡 API Endpoints Reference

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |

### Tasks (🔒 Requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks (supports ?status=, ?priority=, ?search=) |
| GET | `/api/tasks/dashboard` | Get dashboard statistics |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create new task |
| PUT | `/api/tasks/{id}` | Update task |
| PATCH | `/api/tasks/{id}/complete` | Mark task as completed |
| DELETE | `/api/tasks/{id}` | Delete task |

### Users (🔒 Requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/profile` | Get current user profile |
| PUT | `/api/users/profile` | Update profile |
| PATCH | `/api/users/change-password` | Change password |
| GET | `/api/users` | Get all users (🔐 Admin only) |
| PATCH | `/api/users/{id}/toggle-status` | Toggle user active status (🔐 Admin only) |

---

## 🔐 Authentication Flow

```
1. User submits login form
2. POST /api/auth/login → Spring Security authenticates credentials
3. Server returns JWT token (valid for 24 hours)
4. React stores token in localStorage
5. Every subsequent API call includes: Authorization: Bearer <token>
6. AuthTokenFilter validates token and sets SecurityContext
7. Controllers/Services can now access the logged-in user
```

---

## 🧱 Backend Architecture (Layered)

```
HTTP Request
    ↓
Controller (@RestController)       — Handles HTTP, validates input, returns JSON
    ↓
Service (@Service)                 — Business logic, authorization checks
    ↓
Repository (JpaRepository)         — Database queries (JPA/Hibernate)
    ↓
Entity (@Entity)                   — JPA models mapped to MySQL tables
    ↓
MySQL Database
```

---

## 💡 Key Concepts for Interviews

### Why DTOs?
DTOs (Data Transfer Objects) separate the API contract from the database model. We never expose `User` entity directly (it contains the password hash). `UserResponse` DTO includes only safe fields.

### Why JWT over Sessions?
JWT is stateless — the server doesn't store session data. Every request is self-contained. This enables horizontal scaling (multiple servers) and works well with mobile/SPA clients.

### What is BCrypt?
BCrypt is a password hashing algorithm. It adds a random salt and applies multiple rounds of hashing, making brute-force attacks computationally expensive. Never store plain-text passwords!

### What does @Transactional do?
It wraps a method in a database transaction. If any operation fails, all changes are rolled back, maintaining data consistency.

### What is Spring Security's SecurityContext?
A thread-local storage that holds the currently authenticated user for the duration of a request. `SecurityContextHolder.getContext().getAuthentication()` retrieves it anywhere.

---

## 🧪 Sample API Test with curl

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"test123","firstName":"Test"}'

# 2. Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# 3. Create a task (replace TOKEN with the JWT from step 2)
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"title":"My first task","priority":"HIGH","category":"Work"}'

# 4. Get dashboard stats
curl http://localhost:8080/api/tasks/dashboard \
  -H "Authorization: Bearer TOKEN"
```

---

## 🎯 Features Summary

- ✅ User Registration & Login with JWT
- ✅ Role-Based Authorization (USER / ADMIN)
- ✅ Full Task CRUD (Create, Read, Update, Delete)
- ✅ Task filtering by status, priority, category
- ✅ Full-text search on task titles
- ✅ Mark tasks as complete with timestamp
- ✅ Dashboard with real-time statistics
- ✅ Bar chart (task status) + Pie chart (priority)
- ✅ Overdue task detection
- ✅ User profile management
- ✅ Password change with BCrypt verification
- ✅ Responsive design (mobile-friendly)
- ✅ Swagger API documentation
- ✅ Centralized exception handling
- ✅ Input validation (frontend + backend)
- ✅ CORS configured for React dev server

---

## 📦 Building for Production

### Backend JAR
```bash
cd backend
mvn clean package -DskipTests
java -jar target/taskflow-backend-1.0.0.jar
```

### Frontend Build
```bash
cd frontend
npm run build
# Serve the 'build/' folder with any static file server
```

---

## 👨‍💻 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

MIT License — feel free to use this project for learning, portfolios, and interviews.

---

*Built with ❤️ for learning full-stack Java + React development*
