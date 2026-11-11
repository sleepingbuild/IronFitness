package com.homefit.server.repository;

import com.homefit.server.domain.HomeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeSectionRepository extends JpaRepository<HomeSection, Long> {
    List<HomeSection> findAllByOrderBySortOrderAsc();
}
