package com.homefit.server.api;

import com.homefit.server.domain.DiscoverItem;
import com.homefit.server.repository.DiscoverItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discover")
public class DiscoverApiController {

    private final DiscoverItemRepository discoverItemRepository;

    public DiscoverApiController(DiscoverItemRepository discoverItemRepository) {
        this.discoverItemRepository = discoverItemRepository;
    }

    @GetMapping
    public List<DiscoverItem> getDiscoverItems() {
        return discoverItemRepository.findByActiveTrueOrderBySortOrderAsc();
    }
}
