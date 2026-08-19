```mermaid
classDiagram
    class User {
        Long id
        String fullName
        String email
        String passwordHash
        String phone
        Role role
        boolean active
    }
    class Babysitter {
        Long id
        String bio
        int experienceYears
        BigDecimal hourlyRate
        boolean verified
        double avgRating
    }
    class AvailabilitySlot {
        Long id
        LocalDateTime startTime
        LocalDateTime endTime
        boolean booked
    }
    class Booking {
        Long id
        LocalDateTime startTime
        LocalDateTime endTime
        BookingStatus status
        BigDecimal totalAmount
        String notes
    }
    class Payment {
        Long id
        BigDecimal amount
        String method
        PaymentStatus status
        String transactionRef
    }
    class Review {
        Long id
        int rating
        String comment
    }
    class Notification {
        Long id
        String title
        String message
        boolean read
    }

    User "1" --> "0..1" Babysitter
    Babysitter "1" --> "*" AvailabilitySlot
    User "1" --> "*" Booking : parent
    Babysitter "1" --> "*" Booking
    Booking "1" --> "0..1" Payment
    Booking "1" --> "0..1" Review
    User "1" --> "*" Review : reviewer
    User "1" --> "*" Notification

    class UserRepository {
        <<interface>>
    }
    class BabysitterRepository {
        <<interface>>
    }
    class AvailabilitySlotRepository {
        <<interface>>
    }
    class BookingRepository {
        <<interface>>
    }
    class PaymentRepository {
        <<interface>>
    }
    class ReviewRepository {
        <<interface>>
    }
    class NotificationRepository {
        <<interface>>
    }

    class AuthService
    class BabysitterService
    class AvailabilityService
    class BookingService
    class ReviewService
    class PaymentService
    class NotificationService
    class MailService
    class AdminService
    class JwtService

    AuthService --> UserRepository
    BabysitterService --> BabysitterRepository
    BabysitterService --> UserRepository
    AvailabilityService --> AvailabilitySlotRepository
    BookingService --> BookingRepository
    BookingService --> BabysitterRepository
    BookingService --> AvailabilitySlotRepository
    BookingService --> NotificationService
    ReviewService --> ReviewRepository
    ReviewService --> BookingRepository
    ReviewService --> BabysitterRepository
    PaymentService --> PaymentRepository
    PaymentService --> BookingRepository
    NotificationService --> NotificationRepository
    NotificationService --> MailService
    AdminService --> BabysitterRepository
    AdminService --> BookingRepository
    AdminService --> UserRepository
```

Controller layer (`AuthController`, `BabysitterController`, `BookingController`,
`ReviewController`, `PaymentController`, `NotificationController`,
`AdminController`, `HealthController`) stays thin - it validates input, calls the matching service,
and wraps the result in `ApiResponse<{ success, data, message }>`.

As-built (Day 41): added `HealthController` (`GET /api/health` liveness probe),
`MailService` (best-effort SMTP behind notifications), `H2ConsoleConfig`
(explicit H2 console servlet registration) and `UserDto` (admin user lists).
Default database is an H2 file (`./data/babysitter_db`); production overrides
it with a managed MySQL URL via `DB_URL`.
