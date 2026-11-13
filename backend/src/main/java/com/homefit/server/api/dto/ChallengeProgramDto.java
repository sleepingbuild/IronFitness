package com.homefit.server.api.dto;

import com.homefit.server.domain.ChallengeProgram;

import java.util.List;

public record ChallengeProgramDto(
        Long id,
        String level,
        int version,
        String status,
        List<ChallengeSessionDto> sessions
) {
    public static ChallengeProgramDto from(ChallengeProgram p, List<ChallengeSessionDto> sessions) {
        return new ChallengeProgramDto(
                p.getId(),
                p.getLevel(),
                p.getVersion(),
                p.getStatus(),
                sessions
        );
    }
}
