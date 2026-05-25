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
 * Seeds the database with initial data on application startup.
 * Uses per-name checks so new restaurants are added without dropping existing data.
 *
 * Seeds:
 * - 1 test user  (test@food.com / Test@123)
 * - 8 restaurants across 8 cuisines
 * - 5–6 menu items per restaurant (~45 items total)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository       userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository   menuItemRepository;

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
        userRepository.save(User.builder()
                .name("Test User")
                .email("test@food.com")
                .password(encoder.encode("Test@123"))
                .role("ROLE_USER")
                .build());
        log.info("[DataSeeder] Seeded test user: test@food.com");
    }

    // ── Restaurant & MenuItem Seeding ────────────────────────────────────────

    private void seedRestaurantsAndMenuItems() {

        // ── 1. Spice Garden — Indian ─────────────────────────────────────────
        Restaurant spiceGarden = seedRestaurant("Spice Garden", "Indian",
                "12 Curry Lane, Bengaluru, KA 560001",
                "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800", 4.5);
        if (spiceGarden != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Chicken Biryani",
                    "Aromatic basmati rice layered with spiced chicken, saffron, and caramelised onions.",
                    299.0, "Main Course", spiceGarden,
                    "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400"),
                menuItem("Paneer Butter Masala",
                    "Cottage cheese cubes in a rich tomato-cream sauce with fenugreek and spices.",
                    249.0, "Main Course", spiceGarden,
                    "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=400"),
                menuItem("Dal Makhani",
                    "Slow-cooked black lentils in a buttery tomato gravy, simmered overnight.",
                    199.0, "Main Course", spiceGarden,
                    "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=400"),
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
        }

        // ── 2. Pizza Republic — Italian ──────────────────────────────────────
        Restaurant pizzaRepublic = seedRestaurant("Pizza Republic", "Italian",
                "5 Olive Street, Mumbai, MH 400001",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800", 4.3);
        if (pizzaRepublic != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Margherita Pizza",
                    "Classic Neapolitan pizza with San Marzano tomato, fresh mozzarella, and basil.",
                    349.0, "Pizzas", pizzaRepublic,
                    "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400"),
                menuItem("BBQ Chicken Pizza",
                    "Smoky BBQ base with grilled chicken, red onion, and smoked cheddar.",
                    399.0, "Pizzas", pizzaRepublic,
                    "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400"),
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
        }

        // ── 3. Dragon Wok — Chinese ──────────────────────────────────────────
        Restaurant dragonWok = seedRestaurant("Dragon Wok", "Chinese",
                "88 Bamboo Road, Delhi, DL 110001",
                "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800", 4.1);
        if (dragonWok != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Kung Pao Chicken",
                    "Spicy stir-fried chicken with peanuts, dried chillies, and Sichuan pepper.",
                    289.0, "Main Course", dragonWok,
                    "https://images.unsplash.com/photo-1525755662778-989d0524087e?w=400"),
                menuItem("Dim Sum Basket",
                    "Assorted steamed dumplings — har gow, siu mai, and pork bao.",
                    199.0, "Starters", dragonWok,
                    "https://images.unsplash.com/photo-1563245369-86659ea2cc82?w=400"),
                menuItem("Egg Fried Rice",
                    "Wok-fried rice with scrambled egg, spring onion, and soy sauce.",
                    179.0, "Rice", dragonWok,
                    "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400"),
                menuItem("Hot & Sour Soup",
                    "Tangy broth with mushrooms, tofu, bamboo shoots, and white pepper.",
                    129.0, "Soups", dragonWok,
                    "https://images.unsplash.com/photo-1547592180-85f173990554?w=400"),
                menuItem("Mango Pudding",
                    "Silky smooth chilled mango pudding topped with fresh mango cubes.",
                    99.0, "Desserts", dragonWok,
                    "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400")
            ));
        }

        // ── 4. Burger Barn — American ────────────────────────────────────────
        Restaurant burgerBarn = seedRestaurant("Burger Barn", "American",
                "27 Route 66, Hyderabad, TS 500001",
                "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=800", 4.4);
        if (burgerBarn != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Classic Smash Burger",
                    "Double smashed beef patty, American cheese, pickles, and special sauce in a brioche bun.",
                    349.0, "Burgers", burgerBarn,
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"),
                menuItem("BBQ Bacon Burger",
                    "Beef patty with crispy bacon, cheddar, caramelised onions, and smoky BBQ sauce.",
                    399.0, "Burgers", burgerBarn,
                    "https://images.unsplash.com/photo-1553979459-d2229ba7433b?w=400"),
                menuItem("Loaded Cheese Fries",
                    "Crispy fries topped with cheddar sauce, jalapeños, and sour cream.",
                    179.0, "Sides", burgerBarn,
                    "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400"),
                menuItem("Chocolate Milkshake",
                    "Thick, creamy shake blended with premium chocolate ice cream.",
                    149.0, "Beverages", burgerBarn,
                    "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400"),
                menuItem("Crispy Chicken Wings",
                    "8 golden-fried wings tossed in buffalo, honey-garlic, or teriyaki sauce.",
                    259.0, "Starters", burgerBarn,
                    "https://images.unsplash.com/photo-1567620832903-9fc6debc209f?w=400")
            ));
        }

        // ── 5. Tokyo Bites — Japanese ────────────────────────────────────────
        Restaurant tokyoBites = seedRestaurant("Tokyo Bites", "Japanese",
                "14 Sakura Avenue, Chennai, TN 600001",
                "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800", 4.6);
        if (tokyoBites != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Salmon Sushi Platter",
                    "12-piece assorted nigiri and maki with pickled ginger, wasabi, and soy sauce.",
                    549.0, "Sushi", tokyoBites,
                    "https://images.unsplash.com/photo-1617196034183-421b4040ed20?w=400"),
                menuItem("Chicken Ramen",
                    "Rich tonkotsu broth with chashu chicken, soft-boiled egg, nori, and bamboo shoots.",
                    349.0, "Noodles", tokyoBites,
                    "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400"),
                menuItem("Vegetable Tempura",
                    "Lightly battered and fried seasonal vegetables with a dipping dashi broth.",
                    249.0, "Starters", tokyoBites,
                    "https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?w=400"),
                menuItem("Edamame",
                    "Steamed young soybeans tossed with sea salt and chilli flakes.",
                    99.0, "Starters", tokyoBites,
                    "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400"),
                menuItem("Matcha Ice Cream",
                    "Premium ceremonial-grade matcha ice cream with sweet red bean paste.",
                    149.0, "Desserts", tokyoBites,
                    "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400")
            ));
        }

        // ── 6. Taco Loco — Mexican ───────────────────────────────────────────
        Restaurant tacoLoco = seedRestaurant("Taco Loco", "Mexican",
                "3 Fiesta Road, Pune, MH 411001",
                "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800", 4.2);
        if (tacoLoco != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Chicken Street Tacos",
                    "Three soft corn tortillas with grilled chicken, salsa verde, onion, and cilantro.",
                    249.0, "Tacos", tacoLoco,
                    "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400"),
                menuItem("Beef Burrito",
                    "Flour tortilla loaded with slow-braised beef, black beans, rice, cheese, and guacamole.",
                    299.0, "Burritos", tacoLoco,
                    "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400"),
                menuItem("Nachos Supreme",
                    "Crispy tortilla chips with nacho cheese, jalapeños, pico de gallo, and sour cream.",
                    199.0, "Starters", tacoLoco,
                    "https://images.unsplash.com/photo-1582169296194-e4d644c48063?w=400"),
                menuItem("Churros",
                    "Golden fried dough sticks dusted with cinnamon sugar, served with chocolate dipping sauce.",
                    129.0, "Desserts", tacoLoco,
                    "https://images.unsplash.com/photo-1624371414361-e670edf4849e?w=400"),
                menuItem("Horchata",
                    "Chilled rice-and-cinnamon drink sweetened with vanilla, served over ice.",
                    89.0, "Beverages", tacoLoco,
                    "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400")
            ));
        }

        // ── 7. The Tandoor House — North Indian ──────────────────────────────
        Restaurant tandoorHouse = seedRestaurant("The Tandoor House", "North Indian",
                "9 Mughal Garden Road, Jaipur, RJ 302001",
                "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=800", 4.7);
        if (tandoorHouse != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Butter Chicken",
                    "Tender chicken in a velvety tomato-butter gravy, finished with fresh cream.",
                    319.0, "Main Course", tandoorHouse,
                    "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=400"),
                menuItem("Seekh Kebab",
                    "Minced lamb mixed with herbs and spices, skewered and grilled in a tandoor.",
                    299.0, "Starters", tandoorHouse,
                    "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=400"),
                menuItem("Shahi Paneer",
                    "Paneer cubes in a royal cashew and cream sauce perfumed with saffron.",
                    269.0, "Main Course", tandoorHouse,
                    "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=400"),
                menuItem("Rumali Roti",
                    "Paper-thin handkerchief bread cooked on an inverted tawa.",
                    39.0, "Breads", tandoorHouse,
                    "https://images.unsplash.com/photo-1604908177453-7462950a6a3b?w=400"),
                menuItem("Lassi",
                    "Chilled yoghurt drink — available sweet with rose water or salted with cumin.",
                    69.0, "Beverages", tandoorHouse,
                    "https://images.unsplash.com/photo-1571091655789-405eb7a3a3a8?w=400"),
                menuItem("Kheer",
                    "Creamy rice pudding simmered with milk, cardamom, saffron, and topped with pistachios.",
                    99.0, "Desserts", tandoorHouse,
                    "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=400")
            ));
        }

        // ── 8. Green Bowl — Healthy / Salads ─────────────────────────────────
        Restaurant greenBowl = seedRestaurant("Green Bowl", "Healthy",
                "22 Wellness Lane, Bengaluru, KA 560034",
                "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800", 4.0);
        if (greenBowl != null) {
            menuItemRepository.saveAll(List.of(
                menuItem("Quinoa Power Bowl",
                    "Quinoa, roasted chickpeas, avocado, cherry tomatoes, cucumber, and lemon-tahini dressing.",
                    279.0, "Bowls", greenBowl,
                    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400"),
                menuItem("Greek Salad",
                    "Cucumber, olives, cherry tomatoes, red onion, and feta cheese in a herb vinaigrette.",
                    219.0, "Salads", greenBowl,
                    "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=400"),
                menuItem("Avocado Toast",
                    "Sourdough toast topped with smashed avocado, poached egg, chilli flakes, and microgreens.",
                    199.0, "Breakfast", greenBowl,
                    "https://images.unsplash.com/photo-1541519227354-08fa5d50c820?w=400"),
                menuItem("Green Smoothie",
                    "Spinach, banana, mango, coconut water, and chia seeds blended smooth.",
                    149.0, "Beverages", greenBowl,
                    "https://images.unsplash.com/photo-1638176066666-ffb2f013c7dd?w=400"),
                menuItem("Acai Bowl",
                    "Thick acai blend topped with granola, banana, strawberries, and honey.",
                    229.0, "Bowls", greenBowl,
                    "https://images.unsplash.com/photo-1590301157890-4810ed352733?w=400")
            ));
        }

        log.info("[DataSeeder] Restaurant seeding complete.");
    }

    /**
     * Inserts a restaurant only if one with the same name does not already exist.
     * Returns the saved entity or null if it was skipped.
     */
    private Restaurant seedRestaurant(String name, String cuisine, String address,
                                      String imageUrl, double rating) {
        if (restaurantRepository.existsByName(name)) {
            log.info("[DataSeeder] '{}' already exists — skipping.", name);
            return null;
        }
        Restaurant r = restaurantRepository.save(Restaurant.builder()
                .name(name)
                .cuisine(cuisine)
                .address(address)
                .imageUrl(imageUrl)
                .rating(rating)
                .active(true)
                .build());
        log.info("[DataSeeder] Seeded restaurant: {}", name);
        return r;
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
