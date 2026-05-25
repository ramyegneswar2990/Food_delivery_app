package com.foodapp.config;

import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;
import com.foodapp.model.User;
import com.foodapp.repository.MenuItemRepository;
import com.foodapp.repository.RestaurantRepository;
import com.foodapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with initial data on application startup if it is empty.
 *
 * Seeds:
 * - 1 test user (test@food.com / Test@123)
 * - 4 restaurants covering different cuisines
 * - 5 menu items per restaurant (20 items total)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public void run(String... args) {
        seedUsers();
        seedRestaurantsAndMenuItems();
    }

    // ── User Seeding ────────────────────────────────────────────────────────

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("[DataSeeder] Users already seeded — skipping.");
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User testUser = User.builder()
                .name("Test User")
                .email("test@food.com")
                .password(encoder.encode("Test@123"))
                .role("ROLE_USER")
                .build();

        userRepository.save(testUser);
        log.info("[DataSeeder] Seeded test user: test@food.com");
    }

    // ── Restaurant & MenuItem Seeding ────────────────────────────────────────

    private void seedRestaurantsAndMenuItems() {
        if (restaurantRepository.count() > 0) {
            log.info("[DataSeeder] Restaurants already seeded — skipping.");
            return;
        }

        // ── Restaurant 1: Indian ─────────────────────────────────────────────
        Restaurant spiceGarden = restaurantRepository.save(Restaurant.builder()
                .name("Spice Garden")
                .cuisine("Indian")
                .address("12 Curry Lane, Bengaluru, KA 560001")
                .imageUrl("https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800")
                .rating(4.5)
                .active(true)
                .build());

        menuItemRepository.saveAll(List.of(
                menuItem("Chicken Biryani",
                        "Aromatic basmati rice layered with spiced chicken, saffron, and caramelised onions.",
                        299.0, "Main Course", spiceGarden,
                        "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400"),
                menuItem("Paneer Butter Masala",
                        "Cottage cheese cubes in a rich tomato-cream sauce with fenugreek and spices.",
                        249.0, "Main Course", spiceGarden,
                        "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=400"),
                menuItem("Garlic Naan",
                        "Soft leavened flatbread baked in a tandoor, brushed with garlic butter.",
                        49.0, "Breads", spiceGarden,
                        "https://images.unsplash.com/photo-1604908177453-7462950a6a3b?w=400"),
                menuItem("Masala Chai",
                        "Spiced milk tea brewed with ginger, cardamom, and cloves.",
                        39.0, "Beverages", spiceGarden,
                        "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400"),
                menuItem("Gulab Jamun",
                        "Soft milk-solid dumplings soaked in rose-flavoured sugar syrup.",
                        89.0, "Desserts", spiceGarden,
                        "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=400")
        ));

        // ── Restaurant 2: Italian ────────────────────────────────────────────
        Restaurant pizzaRepublic = restaurantRepository.save(Restaurant.builder()
                .name("Pizza Republic")
                .cuisine("Italian")
                .address("5 Olive Street, Mumbai, MH 400001")
                .imageUrl("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800")
                .rating(4.3)
                .active(true)
                .build());

        menuItemRepository.saveAll(List.of(
                menuItem("Margherita Pizza",
                        "Classic Neapolitan pizza with San Marzano tomato, fresh mozzarella, and basil.",
                        349.0, "Pizzas", pizzaRepublic,
                        "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400"),
                menuItem("Pasta Arrabiata",
                        "Penne pasta tossed in a fiery tomato sauce with garlic, chilli, and parsley.",
                        279.0, "Pasta", pizzaRepublic,
                        "https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400"),
                menuItem("Caesar Salad",
                        "Romaine lettuce with Caesar dressing, croutons, Parmesan shavings, and anchovies.",
                        199.0, "Salads", pizzaRepublic,
                        "https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=400"),
                menuItem("Garlic Bread",
                        "Toasted ciabatta with herb-infused garlic butter, served with marinara dip.",
                        99.0, "Starters", pizzaRepublic,
                        "https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?w=400"),
                menuItem("Tiramisu",
                        "Classic Italian dessert with espresso-soaked ladyfingers and mascarpone cream.",
                        149.0, "Desserts", pizzaRepublic,
                        "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400")
        ));

        // ── Restaurant 3: Chinese ────────────────────────────────────────────
        Restaurant dragonWok = restaurantRepository.save(Restaurant.builder()
                .name("Dragon Wok")
                .cuisine("Chinese")
                .address("88 Bamboo Road, Delhi, DL 110001")
                .imageUrl("https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800")
                .rating(4.1)
                .active(true)
                .build());

        menuItemRepository.saveAll(List.of(
                menuItem("Kung Pao Chicken",
                        "Spicy stir-fried chicken with peanuts, dried chillies, and Sichuan pepper.",
                        289.0, "Main Course", dragonWok,
                        "https://images.unsplash.com/photo-1525755662778-989d0524087e?w=400"),
                menuItem("Dim Sum Basket",
                        "Assorted steamed dumplings — har gow, siu mai, and pork bao — served with dipping sauces.",
                        199.0, "Starters", dragonWok,
                        "https://images.unsplash.com/photo-1563245369-86659ea2cc82?w=400"),
                menuItem("Fried Rice",
                        "Wok-fried rice with egg, spring onion, soy sauce, and choice of chicken or vegetables.",
                        179.0, "Rice", dragonWok,
                        "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400"),
                menuItem("Hot & Sour Soup",
                        "Tangy broth with mushrooms, tofu, bamboo shoots, and white pepper.",
                        129.0, "Soups", dragonWok,
                        "https://images.unsplash.com/photo-1547592180-85f173990554?w=400"),
                menuItem("Mango Pudding",
                        "Silky smooth chilled mango pudding topped with fresh mango cubes and condensed milk.",
                        99.0, "Desserts", dragonWok,
                        "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400")
        ));

        // ── Restaurant 4: American ───────────────────────────────────────────
        Restaurant burgerBarn = restaurantRepository.save(Restaurant.builder()
                .name("Burger Barn")
                .cuisine("American")
                .address("27 Route 66, Hyderabad, TS 500001")
                .imageUrl("https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=800")
                .rating(4.4)
                .active(true)
                .build());

        menuItemRepository.saveAll(List.of(
                menuItem("Classic Smash Burger",
                        "Double smashed beef patty, American cheese, pickles, onions, and special sauce in a brioche bun.",
                        349.0, "Burgers", burgerBarn,
                        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"),
                menuItem("BBQ Bacon Burger",
                        "Beef patty with crispy bacon, cheddar, caramelised onions, and smoky BBQ sauce.",
                        399.0, "Burgers", burgerBarn,
                        "https://images.unsplash.com/photo-1553979459-d2229ba7433b?w=400"),
                menuItem("Loaded Cheese Fries",
                        "Crispy fries topped with cheddar sauce, jalapeños, crispy onions, and sour cream.",
                        179.0, "Sides", burgerBarn,
                        "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400"),
                menuItem("Chocolate Milkshake",
                        "Thick, creamy shake blended with premium chocolate ice cream and topped with whipped cream.",
                        149.0, "Beverages", burgerBarn,
                        "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400"),
                menuItem("Crispy Chicken Wings",
                        "8 golden-fried wings tossed in your choice of buffalo, honey-garlic, or teriyaki sauce.",
                        259.0, "Starters", burgerBarn,
                        "https://images.unsplash.com/photo-1567620832903-9fc6debc209f?w=400")
        ));

        log.info("[DataSeeder] Seeded 4 restaurants and 20 menu items successfully.");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private MenuItem menuItem(String name, String description, Double price,
                               String category, Restaurant restaurant, String imageUrl) {
        return MenuItem.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .restaurant(restaurant)
                .imageUrl(imageUrl)
                .available(true)
                .build();
    }
}
