# Sunrise Dental Clinic - Appointment & Patient Management System

This system is a Java-based web application that allows authorized staff members to securely manage patient appointments, patient records, billing, and system help. The application replaces the existing paper-based process with a computerized solution to reduce double bookings, billing errors, and lost patient records.

**GitHub repository:** https://github.com/pubudya/Sunrise-dental-clinic-

Built with **HTML + CSS** (frontend), **Java OOP** (backend), and **MySQL** (database via WAMP).

> **No JSP. No Spring Boot.** Plain HTML pages connected to Java Servlets through a small JSON API layer.

## Architecture

```
HTML/CSS/JS (src/main/webapp/) → Java Servlets (/api/*) → Services → DAOs → MySQL (WAMP)
```

## Roles

| Role | Access |
|------|--------|
| **STAFF** | Register/view appointments, search, billing, help |
| **ADMIN** | All staff features + manage staff accounts + manage dentists |

- Staff self-registration creates an **active STAFF** account immediately (`register-staff.html`)
- Admin login redirects to **admin-dashboard.html**
- Staff login redirects to **dashboard.html**
- Authorization is enforced server-side in Java (`AuthFilter`, `AuthUtil`)

## Default Admin Account

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |

Password is stored with **BCrypt** hashing. The application seeds/resets the admin hash on startup.

## WAMP Server Setup

### Step 1 — Start WAMP
- Launch WAMP Server
- Wait until the icon turns **green** (Apache + MySQL running)

### Step 2 — Database
The Java application creates the `sunrise_dental` database, tables, seed dentists/treatments, and admin account automatically when it starts.

Alternatively, import `database/schema.sql` through http://localhost/phpmyadmin.

### Step 3 — Configure MySQL Password
Edit `src/main/resources/db.properties` if your MySQL root password is not empty:

```properties
db.password=your_password
```

### Step 4 — Run the Java Backend

**From IntelliJ IDEA:**
1. Maven → Reload Project
2. Build → Build Project
3. Run `DentalClinicApplication.java`
4. Open: **http://localhost:8080/login.html**

**From terminal:**
```bash
mvn compile exec:java -Dexec.mainClass="com.sunrisedental.clinic.DentalClinicApplication"
```

## Pages

| Page | Purpose |
|------|---------|
| `login.html` | Staff/admin login |
| `register-staff.html` | Public staff self-registration |
| `dashboard.html` | Staff dashboard |
| `admin-dashboard.html` | Admin dashboard with extra stats |
| `manage-staff.html` | Admin staff CRUD |
| `dentists.html` | Admin dentist CRUD |
| `register.html` | Register appointment + check dentist availability |
| `view-appointment.html` | Search by number, name, and/or mobile |
| `bill.html` | Load/save bill with consultation fee and discounts |
| `appointments.html` | List all appointments |
| `help.html` | Help guide |

## API Routes

| Route | Auth | Description |
|-------|------|-------------|
| `POST /api/register` | Public | Staff self-registration |
| `POST /api/login` | Public | Login |
| `GET /api/session` | Public | Current session |
| `POST /api/logout` | Logged in | Logout |
| `GET /api/dashboard` | Logged in | Role-aware stats |
| `GET /api/meta` | Logged in | Active dentists + treatments |
| `GET /api/dentists` | Logged in | List dentists |
| `GET /api/dentists?available=true&date=&time=` | Logged in | Available dentists for slot |
| `POST/PUT/DELETE /api/dentists` | Admin | Dentist CRUD |
| `GET /api/staff` | Admin | List staff |
| `PUT /api/staff` | Admin | Update staff |
| `POST /api/staff` | Admin | Reset password / delete staff |
| `GET /api/appointments` | Logged in | List or search appointments |
| `POST /api/appointments` | Logged in | Register appointment |
| `GET /api/bill?number=` | Logged in | Load bill for appointment |
| `POST /api/bill` | Logged in | Save bill with fee/discount |

## Validation Rules

### Appointments
- All fields required (trimmed)
- Contact/mobile: exactly **10 digits** (`^[0-9]{10}$`)
- Date cannot be in the past
- Dentist and treatment must exist and be active
- Time must be within clinic hours (08:00–18:00) and dentist working hours
- Duplicate dentist/date/time slots are rejected

### Appointment Search
- At least one of: appointment number, patient name, mobile
- Number/mobile: exact match
- Patient name: case-insensitive partial match
- Returns a list of matching appointments

### Billing
- Treatment cost loaded from database
- Consultation fee editable per appointment
- Discount types: **NONE**, **PERCENT** (max 100%), **FIXED** (cannot exceed subtotal)
- Total = treatment + consultation − discount
- One saved bill per appointment (upsert)

## OOP Structure

```
model/      → User, Dentist, Treatment, Appointment, Bill, forms
dao/        → UserDao, DentistDao, TreatmentDao, AppointmentDao, BillDao
service/    → AuthService, UserService, DentistService, AppointmentService, BillService, DashboardService
servlet/    → Api*Servlet classes
filter/     → AuthFilter (session + role protection)
util/       → DatabaseInitializer, PasswordUtil, ValidationUtil, ServiceFactory, JsonUtil
```

## Features

1. Staff registration and secure login (BCrypt)
2. Admin staff management (edit, activate/deactivate, reset password, delete)
3. Admin dentist management with working hours
4. Dentist availability check by date/time
5. Strict appointment validation and multi-field search
6. Database-backed billing with discounts and saved receipts
7. Role-aware navigation and dashboards
8. Help section and logout
