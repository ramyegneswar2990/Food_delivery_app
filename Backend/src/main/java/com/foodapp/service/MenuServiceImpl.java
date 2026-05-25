package com.foodapp.service;

import com.foodapp.dto.MenuItemDTO;
import com.foodapp.exception.ResourceNotFoundException;
import com.foodapp.model.MenuItem;
import com.foodapp.repository.MenuItemRepository;
import com.foodapp.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link MenuService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public List<MenuItemDTO> getMenuByRestaurant(Long restaurantId) {
        // Verify the restaurant exists first
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }

        return menuItemRepository.findByRestaurant_IdAndAvailableTrue(restaurantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private MenuItemDTO toDTO(MenuItem item) {
        return MenuItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .imageUrl(item.getImageUrl())
                .available(item.isAvailable())
                .build();
    }
}
