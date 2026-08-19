# Changelog

All notable changes to this project are documented here.

## [Unreleased]

### Changed
- Sitters go live as soon as their profile is complete (bio + real rate); slots no longer wait on admin approval. Admins keep verify/reject as a trust control.
- Admin panel now lists users in separate Parents and Sitters columns with safe deletion (own account, admin accounts and accounts with bookings are protected).

### Added
- `GET /api/admin/users` and `DELETE /api/admin/users/{id}` admin endpoints.

## [0.2.0] - 2026-09-21 - Day 41 (Review-II Full Product)

### Added
- Monitoring: public `GET /api/health` liveness probe; event logging for signup, login, booking lifecycle and errors
- Testing: new unit tests for babysitter, availability, payment, notification, admin and mail services; new list/query tests for booking and review services (59 tests total, 28/28 service methods covered, one test class per module)
- CI/CD: `.github/workflows/backend.yml` (Java 21 + `mvn -B clean verify` + Render deploy hook) and `.github/workflows/frontend.yml` (npm install + ESLint + build + Vercel deploy), both blocking on red builds
- Deployment readiness: `PORT`-driven server port, `VITE_API_BASE_URL` frontend override, `render.yaml` Render blueprint, `frontend/.env.example`
- Docs: JavaDoc on every public class and method (backend); README v2 with all 16 required sections; as-built design docs
- Config: H2 file-based default DB (persistent across restarts); `backend/data/` added to `.gitignore`; CORS/env templates extended

### Fixed
- Login failures now return 401 "Invalid email or password" instead of 500
- H2 console works under Spring Boot 4.x via explicit servlet registration
- Babysitter lazy-loading failure on profile reads

## [0.1.0] - 2026-08-11 - Day 11 (Review-I MVP)

### Added
- Problem statement, README, LICENSE, .gitignore, .env.example
- Architecture, ER, and class/module diagrams under `docs/diagrams/`
- Spring Boot backend:
  - JWT auth (signup, login, role-based access)
  - 7 entities: users, babysitters, availability_slots, bookings, payments, reviews, notifications
  - Booking engine with double-booking prevention and hourly-rate pricing
  - Review / rating aggregation
  - Sandbox payment record
  - In-app notifications + email (SMTP) notifications
  - Global exception handler with a consistent `{ success, data, message }` response
- Unit tests for auth, booking, and review services
- React frontend (Vite + Bootstrap):
  - Login / signup pages
  - Babysitter directory with slot booking
  - My bookings with status updates
  - Admin verification dashboard
