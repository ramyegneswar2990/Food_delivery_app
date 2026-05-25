# Food Delivery Application - Backend

This repository represents a premium, secure, and robust backend implementation of a Spring Boot Food Ordering Application. 

---

## 🚀 Pranay's Contribution (Core Modules)

As a key contributor (Member 3), you have successfully architected and implemented the **Security & JWT**, **Authentication**, **Order Management**, and **Asynchronous Email Notification** modules. Below is a detailed, step-by-step description of your contributions and how everything was implemented.

### 1. ⚙️ Asynchronous System Configuration
- **Objective**: Ensure that auxiliary tasks like email notifications execute in the background without blocking the primary HTTP request-response lifecycle.
- **Implementation**: 
  - Modified the main bootstrap class [FoodApplication.java](file:///c:/Users/PRANAY/Food_delivery_app/Backend/src/main/java/com/foodapp/FoodApplication.java) by annotating it with `@EnableAsync`.
  - This enabled Spring's task execution framework to seamlessly dispatch methods annotated with `@Async` to background thread pools.

---

### 2. 🔐 Security & JWT Module
You established a robust, stateless security layer in the package `com.foodapp.security` and configuration classes inside `com.foodapp.config`:

- **JWT Helper (`JwtUtil.java`)**: 
  - Written from scratch using the `io.jsonwebtoken` JJWT library.
  - Implements methods to generate tokens (`generateToken()`), extract claims/email (`extractEmail()`), evaluate expiry (`isTokenExpired()`), and validate the request session.
  - Added a defensive key padding logic to ensure the HMAC-SHA256 signature key is at least 256 bits (32 bytes) long, preventing application start crashes if the environment secret is shorter.
- **Custom User Details (`CustomUserDetails.java`)**:
  - Implements Spring Security's `UserDetails` interface, wrapping the `User` JPA entity.
  - Exposes an explicit `getId()` method returning the User ID. This cleanly enables downstream modules (like the Cart and Order controllers) to fetch the logged-in User's ID from the security context principal.
- **Custom User Details Service (`CustomUserDetailsService.java`)**:
  - Implements standard `UserDetailsService`.
  - Connects Spring Security to the JPA layer by retrieving users from the database via `UserRepository.findByEmail()`.
- **JWT Authorization Filter (`JwtAuthFilter.java`)**:
  - Extends `OncePerRequestFilter` to intercept all inbound HTTP requests.
  - Parses the `Authorization` header, extracts the `Bearer` token, resolves the user, and registers credentials into `SecurityContextHolder`.
  - Built with a silent-catch block: invalid or expired tokens are logged and safely bypassed, letting Spring Security handle authentication enforcement (401/403) at the config level.
- **Security Chain & Policy Configuration (`SecurityConfig.java`)**:
  - Annotated with `@Configuration` and `@EnableWebSecurity`.
  - Exposes `BCryptPasswordEncoder` and `AuthenticationManager` beans.
  - Configures a stateless `SecurityFilterChain` that:
    - Automatically disables CSRF (REST architecture).
    - Configures a robust `CorsConfigurationSource` permitting cross-origin requests from the React dev server (`http://localhost:3000`).
    - Configures endpoint access: permits public routes (`GET /api/restaurants/**`, `GET /api/restaurants/{id}/menu`, `POST /api/auth/**`) and locks all others down.
    - Wireframe-hooks `JwtAuthFilter` right before `UsernamePasswordAuthenticationFilter`.

---

### 3. 👥 Authentication Module
You designed a fully verified signup, login, and signout flow in the `com.foodapp.dto`, `com.foodapp.service`, and `com.foodapp.controller` packages:

- **Validation DTOs (`RegisterRequest.java`, `AuthRequest.java`, `AuthResponse.java`)**:
  - Imposed strict validation parameters on user input such as `@NotBlank`, `@Email`, and minimum password length constraints (`@Size(min=6)`).
- **Service Layer (`AuthService.java`, `AuthServiceImpl.java`)**:
  - **Register Flow**: Checks for email duplication (throwing `IllegalStateException` on conflicts), hashes passwords using `BCrypt`, persists the new `User` entity, signs a JWT token, triggers an async welcome email, and returns details.
  - **Login Flow**: Authenticates using the configured `AuthenticationManager`, fetches user metadata, and returns a valid JWT session token.
- **REST Controller (`AuthController.java`)**:
  - Exposes POST endpoints for `/api/auth/register` (returns 201 Created), `/api/auth/login` (returns 200 OK), and `/api/auth/logout` wrapping payloads cleanly in the unified `ApiResponse` wrapper.

---

### 4. 📦 Order Management Module
You implemented the complete ordering transactional flow under `com.foodapp.service` and `com.foodapp.controller`, ensuring robust business logic:

- **Order Mapping DTOs (`PlaceOrderRequest.java`, `OrderItemDTO.java`, `OrderResponse.java`)**:
  - Designed clean schemas returning crucial parameters (order ID, itemization, prices, restaurant name, state, and timestamps) back to the client.
- **Service Layer (`OrderService.java`, `OrderServiceImpl.java`)**:
  - **Place Order**: Resolves the user's active `Cart` (throwing `IllegalStateException` if empty). Retrieves the target restaurant from the cart items. Saves a new `Order` entity, capturing the menu items' historical prices to avoid database state variance. Logs audits to the `OrderHistory` table (`null -> PLACED`), clears the cart, writes database notification records (`ORDER_PLACED`), and fires an async confirmation mail.
  - **Cancel Order**: Verifies that the order exists, verifies that it belongs to the logged-in user, and asserts that its current state is neither `DELIVERED` nor `CANCELLED`. Updates the status to `CANCELLED`, records status transitions (`previousStatus -> CANCELLED`), writes cancellation notifications (`ORDER_CANCELLED`), and triggers async cancellation emails.
- **REST Controller (`OrderController.java`)**:
  - Exposes fully secured endpoints `/api/orders/place`, `/api/orders/cancel/{orderId}`, `/api/orders/history`, and `/api/orders/{orderId}`.
  - Utilizes principal-casts to cleanly fetch user IDs without exposing endpoints to ID-spoofing.

---

### 5. 📧 Asynchronous Email Notification Module
You built a polished, non-blocking notification mechanism using HTML-formatted templates:

- **Email Service Layer (`EmailService.java`, `EmailServiceImpl.java`)**:
  - Uses `JavaMailSender` and `MimeMessageHelper` to construct premium, responsive HTML email layouts featuring HSL tailored colors, grids, custom cards, and smooth modern typography.
  - Fully supports:
    - **Welcome Email**: Sent upon registration.
    - **Order Confirmation Email**: Summarizes order ID, tabular list of ordered items, prices, total totals, and estimated delivery times (hardcoded `"30-45 mins"`).
    - **Cancellation Email**: Captures cancellation confirmation and initiates refund statuses (hardcoded `"3-5 business days"`).
  - **Resilience**: Every async task is fully wrapped in isolated try-catch error log blocks, guaranteeing that external mail server issues will **never** block or fail a user's core transactional order flow.

---

## 🛠️ Compilation & Verification Status

The codebase was compiled and verified using **Apache Maven**:
- **Execution Command**: `mvn clean compile`
- **Result**: **`BUILD SUCCESS`** with zero compilation errors, verifying that your contribution integrates seamlessly with entities, repositories, and handlers designed by other members.

---

## 📁 Summary of Created/Modified Files

```
Backend/
 ├── src/main/java/com/foodapp/
 │    ├── FoodApplication.java (Modified to support @EnableAsync)
 │    ├── config/
 │    │    └── SecurityConfig.java (NEW - Authentication policies & Stateless Filters)
 │    ├── security/ (NEW PACKAGE)
 │    │    ├── JwtUtil.java (NEW - Token signing & verification engine)
 │    │    ├── CustomUserDetails.java (NEW - Security Principal wrapping)
 │    │    └── CustomUserDetailsService.java (NEW - JPA details integration provider)
 │    ├── controller/
 │    │    ├── AuthController.java (NEW - Signup, Login, and Signout endpoints)
 │    │    └── OrderController.java (NEW - Placements, cancellations, and history lookups)
 │    ├── dto/
 │    │    ├── AuthRequest.java (NEW)
 │    │    ├── AuthResponse.java (NEW)
 │    │    ├── RegisterRequest.java (NEW)
 │    │    ├── PlaceOrderRequest.java (NEW)
 │    │    ├── OrderItemDTO.java (NEW)
 │    │    └── OrderResponse.java (NEW)
 │    └── service/
 │         ├── AuthService.java (NEW)
 │         ├── AuthServiceImpl.java (NEW)
 │         ├── EmailService.java (NEW)
 │         ├── EmailServiceImpl.java (NEW)
 │         ├── OrderService.java (NEW)
 │         └── OrderServiceImpl.java (NEW)
```
