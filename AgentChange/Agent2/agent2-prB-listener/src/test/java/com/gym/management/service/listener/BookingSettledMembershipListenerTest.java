package com.gym.management.service.listener;

// ⚠️ Cùng package giả định với listener — sửa theo đường dẫn thật của BookingSettledEvent.
import com.gym.management.event.BookingSettledEvent;
import com.gym.management.service.MembershipService;
import com.gym.management.service.MembershipService.ConsumeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingSettledMembershipListenerTest {

    @Mock
    private MembershipService membershipService;

    private BookingSettledMembershipListener listener;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        listener = new BookingSettledMembershipListener(membershipService);
        memberId = UUID.randomUUID();
    }

    private BookingSettledEvent eventWith(BookingSettledEvent.Outcome outcome) {
        return new BookingSettledEvent(UUID.randomUUID(), UUID.randomUUID(), memberId,
                outcome, true, LocalDateTime.now());
    }

    @Test
    void shouldConsumeOneSession_forEveryOutcome() {
        when(membershipService.tryConsumeSession(memberId)).thenReturn(ConsumeResult.CONSUMED);

        BookingSettledEvent.Outcome[] outcomes = BookingSettledEvent.Outcome.values();
        for (BookingSettledEvent.Outcome outcome : outcomes) {
            listener.onBookingSettled(eventWith(outcome));
        }

        // COMPLETED, MEMBER_NO_SHOW, LATE_CANCEL đều phải trừ đúng 1 buổi
        verify(membershipService, times(outcomes.length)).tryConsumeSession(memberId);
    }

    @Test
    void shouldNeverThrow_whenSessionCannotBeConsumed() {
        when(membershipService.tryConsumeSession(memberId))
                .thenReturn(ConsumeResult.NO_SESSION_LEFT, ConsumeResult.NO_ACTIVE_MEMBERSHIP, ConsumeResult.UNLIMITED);

        // Thiếu buổi/không có gói ACTIVE không được làm hỏng việc chốt booking của Agent 3
        assertThatCode(() -> {
            listener.onBookingSettled(eventWith(BookingSettledEvent.Outcome.COMPLETED));
            listener.onBookingSettled(eventWith(BookingSettledEvent.Outcome.MEMBER_NO_SHOW));
            listener.onBookingSettled(eventWith(BookingSettledEvent.Outcome.LATE_CANCEL));
        }).doesNotThrowAnyException();
    }
}
