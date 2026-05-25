package com.foodapp.service;

import com.foodapp.dto.RestaurantDTO;
import com.foodapp.exception.ResourceNotFoundException;
import com.foodapp.model.Restaurant;
import com.foodapp.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RestaurantService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public List<RestaurantDTO> getAllActiveRestaurants() {
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
        return toDTO(restaurant);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private RestaurantDTO toDTO(Restaurant r) {
        return RestaurantDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .cuisine(r.getCuisine())
                .address(r.getAddress())
                .imageUrl(r.getImageUrl())
                .rating(r.getRating())
                .build();
    }
}
