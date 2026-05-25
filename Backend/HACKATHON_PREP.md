# Hackathon Evaluation Prep Guide - David's Branch (Restaurant, Menu, & Cart Modules)

This guide summarizes the architectural decisions, code structure, and business logic implemented on the `David` branch. Use this to prepare for your project presentation, Q&A, or live coding modifications.

---

## 1. High-Level Architecture Overview

We follow the standard **Controller-Service-Repository** pattern with clean **separation of concerns**:
*   **Controller Layer (API)**: Handles HTTP requests, input validation (`@Valid`), security context retrieval, and returns standardized JSON responses using the `ApiResponse<T>` wrapper. **No business logic lives here.**
*   **Service Layer (Business Logic)**: Contains the core rules (validation, data mapping, total calculations, cart updates, exception throwing).
*   **Data Access Layer (Repository)**: Utilizes Spring Data JPA interfaces already provided in the foundation.
*   **DTO Layer (Data Transfer Objects)**: Decouples the database entities from the REST API interface.
    *   *Why?* It prevents infinite recursion during JSON serialization (due to bidirectional JPA mappings) and hides internal database structures or sensitive fields (e.g. passwords, relationship links).

---

## 2. Walkthrough of Modules & Key Functions

### A. Restaurant Module (Public Browsing)
*   **DTO**: `RestaurantDTO` (exposes only basic details like `id`, `name`, `cuisine`, `address`, `imageUrl`, `rating`).
*   **Logic**:
    *   `getAllActiveRestaurants()`: Fetches only restaurants where `active = true` using `restaurantRepository.findByActiveTrue()`.
    *   `getRestaurantById(id)`: Looks up a restaurant. If not found, throws `ResourceNotFoundException("Restaurant not found: " + id)`.
*   **Endpoints** (Public/Unauthenticated):
    *   `GET /api/restaurants` (Get active list)
    *   `GET /api/restaurants/{id}` (Get restaurant detail)

### B. Menu Module (Public Menu View)
*   **DTO**: `MenuItemDTO` (exposes menu details and its availability status).
*   **Logic**:
    *   `getMenuByRestaurant(restaurantId)`: 
        1. First validates that the restaurant exists (throws `ResourceNotFoundException` if not).
        2. Retrieves only available items via `menuItemRepository.findByRestaurant_IdAndAvailableTrue(restaurantId)`.
*   **Endpoints** (Public/Unauthenticated):
    *   `GET /api/restaurants/{id}/menu`

### C. Cart Module (Authenticated Operations)
Requires the user to be logged in. The user ID is retrieved dynamically from the Spring Security context inside the controller and passed down to the service layer.
*   **DTOs**: 
    *   `CartItemRequest`: Represents request payload for adding items (`menuItemId`, `quantity` with `@Min(1)`).
    *   `CartItemDTO` / `CartDTO`: Represents the response payload including item totals and the grand total.
*   **Logic**:
    *   **Lazy Cart Creation**: `getOrCreateCart(userId)` checks if a cart already exists for the user. If not, it retrieves the `User` record, instantiates a new empty `Cart`, saves it, and returns it.
    *   **Add Item (`addItem`)**:
        1. Fetches the `MenuItem` and checks if it's available. If unavailable or non-existent, throws `ResourceNotFoundException`.
        2. Checks if the item is already present in the cart.
        3. If present: Increments the quantity.
        4. If new: Instantiates a new `CartItem` linked to the cart.
        5. Computes the line item total (`quantity * price`).
    *   **Remove Item (`removeItem`)**:
        1. Ensures the target `CartItem` actually exists in this user's cart (preventing cross-user deletion).
        2. Removes it and triggers orphan removal.
    *   **Clear Cart (`clearCart`)**:
        1. Clears the cart item collection.
    *   **Total Calculation**:
        *   `itemTotal` is calculated on the line item level using `CartItem.calculateItemTotal()`.
        *   `grandTotal` is calculated on the fly in `CartServiceImpl.calculateGrandTotal(cart)` by summing up all active line item totals.
*   **Endpoints** (Authenticated):
    *   `GET /api/cart` (Get user's cart)
    *   `POST /api/cart/add` (Add item/increase quantity)
    *   `DELETE /api/cart/remove/{cartItemId}` (Remove item)
    *   `DELETE /api/cart/clear` (Clear whole cart)

---

## 3. Anticipated Live Code Modification Scenarios

Judges/Evaluators often ask you to modify code on the fly to see how well you understand the project. Here are the most likely tasks they might request and how to implement them:

### Scenario 1: "Add a stock check or limit maximum item quantity in the cart"
*   **If they ask**: "Limit the maximum quantity of a single item in the cart to 10."
*   **How to modify**:
    In [CartServiceImpl.java](file:///c:/Users/vegir/Food_delivery_app/Backend/src/main/java/com/foodapp/service/CartServiceImpl.java), look inside `addItem(...)`:
    ```java
    int newQuantity = request.getQuantity();
    if (existingItem.isPresent()) {
        newQuantity += existingItem.get().getQuantity();
    }
    if (newQuantity > 10) {
        throw new IllegalArgumentException("Cannot add more than 10 of this item to your cart");
    }
    ```

### Scenario 2: "Implement a coupon/discount code discount on the Cart"
*   **If they ask**: "Apply a flat 10% discount to the grand total."
*   **How to modify**:
    In [CartServiceImpl.java](file:///c:/Users/vegir/Food_delivery_app/Backend/src/main/java/com/foodapp/service/CartServiceImpl.java), modify the mapper `toCartDTO(Cart cart)`:
    ```java
    Double subTotal = calculateGrandTotal(cart);
    Double grandTotal = subTotal * 0.90; // Apply 10% discount
    ```
    *(For production, you'd add a coupon code field to the Cart entity, validate the coupon, and calculate the discount based on that).*

### Scenario 3: "Change Restaurant Listing to order by rating descending"
*   **If they ask**: "Order the active restaurant list so that highly rated restaurants appear first."
*   **How to modify**:
    1. In [RestaurantRepository.java](file:///c:/Users/vegir/Food_delivery_app/Backend/src/main/java/com/foodapp/repository/RestaurantRepository.java), define:
       ```java
       List<Restaurant> findByActiveTrueOrderByRatingDesc();
       ```
    2. In [RestaurantServiceImpl.java](file:///c:/Users/vegir/Food_delivery_app/Backend/src/main/java/com/foodapp/service/RestaurantServiceImpl.java), call that method instead of `findByActiveTrue()`.

---

## 4. Key Java/Spring Boot Topics to Speak to

*   **JPA Bidirectional Mappings & Cascades**:
    *   Mention that `Cart` has a `@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)` relationship with `CartItem`.
    *   Explain that `orphanRemoval = true` ensures that when we remove a `CartItem` from the `items` list in Java, Hibernate automatically deletes the row from the database when the cart is saved.
*   **Transaction Management (`@Transactional`)**:
    *   Explain that read-only methods (e.g. browsing restaurants) use `@Transactional(readOnly = true)` for performance optimization (Spring skips dirty checking).
    *   Write methods (like adding/removing items) use `@Transactional` to ensure that if any DB query fails mid-operation, the whole transaction rolls back, keeping database state consistent.
*   **Spring Security Context**:
    *   Explain that we do not pass `userId` as a path variable or request body parameter in cart endpoints because that is a security vulnerability (allowing users to modify other users' carts). Instead, we pull the authenticated user principal directly from `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` safely.

---

## 5. What to Say if Asked: "What did you do on David's branch?" (1-Minute Pitch)

Use this script as a guide when the judges ask you to summarize your contribution:

> *"On David's branch, I implemented three core modules: **Restaurant Browsing**, **Menu Viewing**, and **Cart Management**.*
>
> *1. For the **Restaurant and Menu modules**, I built public endpoints that allow customers to view active restaurants and retrieve available menu items. These are optimized with read-only transactions.*
> *2. For the **Cart module**, I built the end-to-end cart management flow (retrieving, adding, removing, and clearing items) with dynamic subtotal and grand total calculations. This module is secure; it integrates directly with Spring Security to retrieve the logged-in user's ID from the session context, preventing ID tampering vulnerabilities.*
> *3. To build this clean and maintainable, I used the **Controller-Service-Repository pattern**, kept database structures isolated using **DTOs**, and leveraged JPA features like **orphan removal** to manage relational lifecycle automatically."*

