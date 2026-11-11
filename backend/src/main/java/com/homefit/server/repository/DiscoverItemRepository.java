package com.homefit.server.repository;

import com.homefit.server.domain.DiscoverItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoverItemRepository extends JpaRepository<DiscoverItem, Long> {
    List<DiscoverItem> findAllByOrderBySortOrderAsc();
    List<DiscoverItem> findByActiveTrueOrderBySortOrderAsc();
}
