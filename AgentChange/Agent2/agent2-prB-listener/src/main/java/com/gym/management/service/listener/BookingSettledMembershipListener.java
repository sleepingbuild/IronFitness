package com.gym.management.service.listener;

// ⚠️ XÁC NHẬN LẠI PACKAGE: đây là phỏng đoán, class BookingSettledEvent do Agent 3 tạo (chưa có trên
// master lúc viết file này). Kiểm tra bằng:  git grep -n "record BookingSettledEvent" origin/master
// rồi sửa dòng import cho khớp đường dẫn thật.
import com.gym.management.event.BookingSettledEvent;
import com.gym.management.service.MembershipService;
import com.gym.management.service.MembershipService.ConsumeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Agent 2 - Membership. Trừ 1 buổi của gói hội viên khi một booking được "chốt" — theo
 * CHANGE_PT_ATTENDANCE_AND_PAY.md (G2/G7), cả 3 kết cục đều trừ buổi:
 * COMPLETED, MEMBER_NO_SHOW (NO_SHOW), LATE_CANCEL (LATE_CANCELLED).
 *
 * Listener chạy ĐỒNG BỘ trong transaction của Agent 3, nên dùng tryConsumeSession (không ném lỗi
 * nghiệp vụ) — nếu để lỗi "hết buổi/không có gói ACTIVE" nổi lên, transaction của Agent 3 bị
 * rollback-only và PT không thể chốt buổi/nhận công vì vấn đề kế toán gói của hội viên. Trường hợp
 * không trừ được chỉ ghi log WARN để lễ tân xử lý.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSettledMembershipListener {

    private final MembershipService membershipService;

    @EventListener
    public void onBookingSettled(BookingSettledEvent event) {
        ConsumeResult result = membershipService.tryConsumeSession(event.memberId());

        if (result == ConsumeResult.NO_SESSION_LEFT || result == ConsumeResult.NO_ACTIVE_MEMBERSHIP) {
            log.warn("Không trừ được buổi cho booking {} (member {}, outcome {}): {}",
                    event.bookingId(), event.memberId(), event.outcome(), result);
        }
    }
}
