# Gửi Agent 3 — đã sửa hộ 1 lỗi compile trong `MembershipLookupPortImpl.java`

## Lỗi gặp phải

Lúc build thử trên máy, `mvn clean verify` báo lỗi tại file này (thuộc Agent 3, implement
`MembershipLookupPort` để gọi sang `MembershipService` của Agent 2):

```
cannot find symbol: method getPackage()
location: variable currentMembership of type java.util.Optional<MemberPackage>
```

## Nguyên nhân — 2 vấn đề chồng nhau

1. **Sai tên getter**: entity `MemberPackage` (Agent 2 định nghĩa) dùng field `gymPackage`,
   không phải `package` (vì `package` là từ khoá Java, không thể đặt tên field). Getter đúng là
   `getGymPackage()`, không phải `getPackage()`.
2. **Dead code tiềm ẩn**: code gốc so sánh `currentMembership == null`. `getCurrentMembership()`
   trả về `Optional<MemberPackage>` — **không bao giờ `null`**, chỉ có thể là `Optional.empty()`.
   Điều kiện này sẽ không bao giờ đúng, nên nếu hội viên không có gói ACTIVE, code sẽ chạy tiếp
   và ném `NoSuchElementException` (lỗi runtime khó hiểu) thay vì đúng `MEMBERSHIP_NOT_FOUND`
   như có vẻ là ý định ban đầu.

## Bản đã sửa (chỉ sửa đủ để hết lỗi compile + lỗi logic trên, KHÔNG đổi hành vi nghiệp vụ nào khác)

```java
@Override
public Integer resolveMaxSessionsPerWeek(UUID memberId) {
    MemberPackage memberPackage = membershipService.getCurrentMembership(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND,
                    "Hội viên chưa có gói tập đang hoạt động, không thể đặt lịch PT"));
    return memberPackage.getGymPackage().getMaxSessionsPerWeek();
}
```

(File đầy đủ đã gửi riêng: `MembershipLookupPortImpl.java`)

## Cần Agent 3 xác nhận lại

Agent 2 chỉ sửa đủ để compile đúng và tránh `NoSuchElementException` — **không chắc chắn**
message lỗi (`"Hội viên chưa có gói tập đang hoạt động, không thể đặt lịch PT"`) hay
`ErrorCode.MEMBERSHIP_NOT_FOUND` có đúng ý đồ nghiệp vụ Agent 3 muốn khi member không có gói
ACTIVE lúc đặt lịch PT hay không (ví dụ: có thể Agent 3 muốn 1 `ErrorCode` riêng, hoặc muốn trả
`null`/giá trị mặc định thay vì ném lỗi). Nhờ Agent 3 xem lại và đổi nếu cần — đây là file/logic
thuộc Agent 3, Agent 2 chỉ đảm bảo phần gọi sang `MembershipService` đúng kỹ thuật.
