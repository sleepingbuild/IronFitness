package com.homefit.server.repository;

import com.homefit.server.domain.ChallengeProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeProgramRepository extends JpaRepository<ChallengeProgram, Long> {
    Optional<ChallengeProgram> findTopByLevelAndStatusOrderByVersionDesc(String level, String status);
}
