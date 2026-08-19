# Problem Statement

## 1. Title

Babysitter Booking Platform

## 2. Domain

On-demand childcare / service booking

## 3. Who is the user? (2-3 user types, with roles)

1. **Parent** - A working parent who needs a verified babysitter for a few hours, either occasionally or on a regular schedule.
2. **Babysitter** - A care provider (often a college student or part-time worker) who lists their availability and hourly rate and takes on bookings.
3. **Admin** - The platform operator who verifies babysitter profiles, keeps the directory clean, and can intervene on problematic bookings.

## 4. What problem are we solving? (3-5 sentences, real-life example)

A working parent who needs to attend a late meeting or a Saturday event currently has to call around to friends or post on community WhatsApp groups and then manually confirm whether the person is available and how much it will cost. There is no structured way to check a babysitter's background or rating before trusting them with children. Double-booking is also common when the babysitter tracks availability in a spreadsheet or on paper. This platform fixes that by giving parents a searchable directory of verified babysitters with transparent hourly rates and ratings, and by letting both sides manage bookings through a shared calendar so a slot can never be booked twice.

## 5. Proposed Solution (what the application will do, feature-wise)

- **User accounts** - Signup and login for parents, babysitters, and admins with JWT-based authentication.
- **Babysitter profiles** - Babysitters create a profile with bio, experience, and hourly rate; a complete profile goes live in the public directory immediately, while admins keep a trust-marking verify/reject power and manage all users.
- **Availability management** - Babysitters create time slots; booked slots are locked automatically.
- **Booking engine** - Parents pick a verified babysitter and an open slot, the system calculates the total (hourly rate x hours), creates the booking, and notifies both parties.
- **Booking lifecycle** - Bookings move through Pending -> Confirmed -> Completed / Cancelled with proper permission checks.
- **Reviews & ratings** - A parent can rate a completed booking; the babysitter's average rating is recomputed automatically.
- **Payment record** - A sandbox payment flow that records Pending/Paid transactions (no real money).
- **Notifications** - In-app notifications plus email (SMTP) for booking confirmations.

## 6. Core Entities / Database Tables (list all, minimum 5)

1. `users` - account details, role, hashed password
2. `babysitters` - profile, verification flag, hourly rate, average rating
3. `availability_slots` - a babysitter's open time slots
4. `bookings` - a parent booking a babysitter for a slot
5. `payments` - payment record for a booking
6. `reviews` - rating and comment left on a completed booking
7. `notifications` - in-app messages for users

## 7. User Roles & Permissions (minimum 2 distinct roles, e.g. Admin & User)

- **PARENT**
  - Search and view verified babysitters
  - Create and cancel own bookings
  - Review completed bookings
  - View own bookings and notifications
- **BABYSITTER**
  - Manage own profile and availability slots
  - Accept / reject incoming booking requests
  - View own bookings, reviews, and notifications
- **ADMIN**
  - Verify / reject babysitter profiles
  - View all users and bookings
  - Moderate reviews and resolve disputes

## 8. Success Criteria (e.g. 'a user should be able to book an appointment in under 1 minute')

- A parent can find a verified babysitter and complete a booking in under 2 minutes.
- A slot can never be booked by two parents at the same time.
- A booking confirmation notification reaches both parties within seconds of confirmation.
- A babysitter's average rating updates immediately after a review is posted.
- The whole app runs locally by following only the README instructions.

## 9. Out of Scope (clearly list what you will NOT build, to avoid over-commitment)

- Real online payments (only a sandbox / mock payment record).
- In-app chat or video call between parent and babysitter.
- Geolocation / maps / distance-based search.
- Background verification of babysitters (police checks etc.) - verification is manual by the admin.
- Recurring weekly bookings / subscriptions.
- Mobile apps - web only for now.

## 10. Chosen Track: Java (Spring Boot) / Python (Django or FastAPI)

**Java (Spring Boot 3.x, Java 17)**

- Backend: Spring Boot 3.x + Spring Security + JWT (jjwt)
- ORM: Spring Data JPA + Hibernate
- Database: MySQL 8
- Build: Maven
- Testing: JUnit 5
- API docs: springdoc-openapi (Swagger UI)
- Frontend: React.js + Bootstrap + Axios
