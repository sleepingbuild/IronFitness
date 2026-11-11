package com.homefit.server.repository;

import com.homefit.server.domain.ContentRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentReleaseRepository extends JpaRepository<ContentRelease, Long> {
    Optional<ContentRelease> findTopByOrderByVersionDesc();
}
