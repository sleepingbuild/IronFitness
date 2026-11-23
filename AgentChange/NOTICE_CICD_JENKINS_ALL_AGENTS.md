# Thông báo: bổ sung quy trình CI/CD (Jenkins) cho dự án

Gửi: Agent 1, 2, 3, 5, 6
Từ: Phi (Agent 4 là người triển khai)
Ngày: 2026-10-18
Phạm vi hiện tại: **chỉ Yêu cầu 1–3** của bài thực hành CI/CD. Yêu cầu 4 trở đi (triển khai thật, kiểm thử 3 tình huống) chưa làm.

## 1. Chuyện gì đang diễn ra

Bài thực hành môn học yêu cầu dựng quy trình tự động GitHub → Jenkins → Build → Test → Publish. Đề gốc dùng ASP.NET Core + IIS, nhóm quyết định áp dụng **trực tiếp cho dự án GMS này** (Spring Boot, Java 17, Maven) thay vì làm một app khác. Code nghiệp vụ không đổi.

## 2. Repo sẽ thay đổi gì

Thêm **một file mới `Jenkinsfile` ở thư mục gốc repo**, đưa vào qua PR riêng từ nhánh `ci/jenkins-pipeline` (không nằm trong nhánh `feature/agentN-...` nào). Đây là file dùng chung của cả repo nên **Agent 1 review** theo quy ước file SHARED.

Pipeline có 7 giai đoạn:

| Giai đoạn | Việc làm |
|---|---|
| Checkout | Lấy mã mới nhất từ `master` |
| Restore | `mvn dependency:resolve` |
| Build | `mvn clean compile` |
| Test | `mvn test`, ghi báo cáo JUnit |
| Publish | `mvn package -DskipTests`, lưu file JAR vào Jenkins |
| Deploy | **Tạm thời chỉ in thông báo** (chưa triển khai thật) |
| Verify | **Tạm thời chỉ kiểm tra file JAR tồn tại** |

Build hoặc Test lỗi thì pipeline dừng, các giai đoạn sau không chạy.

## 3. Ảnh hưởng và việc cần làm của từng người

**Tất cả agent**
1. Jenkins chỉ cài trên máy của Phi. Không ai phải cài Jenkins hay ngrok.
2. Sau khi chạy, mỗi lần có thay đổi vào `master`, Jenkins sẽ tự build. Build đỏ trên `master` cần được ưu tiên sửa.
3. Với thiết kế hiện tại (một Pipeline job chỉ build nhánh `master`), Jenkins **không** build nhánh `feature/...` hay PR. Vì vậy vẫn phải tự chạy `mvn clean verify` trước khi mở PR, như quy trình hiện nay.
4. **Test mặc định phải chạy được mà không cần Docker, SQL Server hay Redis.** Jenkins chạy `mvn test` trên máy không bật container. Test nào cần hạ tầng thật phải gắn `@Tag("integration")` (xem mục dành cho Agent 1).
5. Đề nghị **không xoá nhánh `feature/...` đã merge và không force-push** lên các nhánh đó, để giữ lịch sử nhánh và PR làm minh chứng cho báo cáo (Yêu cầu 1: mỗi người một nhánh riêng, hợp nhất qua PR). Các PR đã có (#42, #43...) là bằng chứng sẵn có.
6. Không commit secret, token hay mật khẩu vào repo (webhook có dùng secret, nhưng chỉ cấu hình trên GitHub và Jenkins).

**Agent 1**
1. Review PR `ci/jenkins-pipeline` khi Phi mở.
2. Khi thêm Testcontainers và Maven profile `integration` vào `pom.xml` (đã thông báo trước đó), giữ **profile mặc định không cần Docker**: `mvn test` và `mvn verify` không có `-P` phải bỏ qua test gắn `@Tag("integration")`. Nếu không, bước Test trên Jenkins sẽ đỏ.
3. Cùng Phi quyết định có bật bảo vệ nhánh `master` (bắt buộc đi qua PR) hay không. Việc này có lợi cho báo cáo nhưng ảnh hưởng cách mọi người merge, nên cần đồng ý trước.

**Agent 2, 3, 5, 6**
- Không cần sửa code. Chỉ cần tuân thủ các quy tắc ở mục "Tất cả agent". Agent 5 và 6 chưa có PR nên đặc biệt lưu ý quy tắc test không cần Docker ngay từ đầu.

## 4. Không thay đổi

- Thứ tự merge hiện tại, cách làm việc bằng worktree riêng (`gms-agentN`) và quy ước commit.
- Yêu cầu 4 (IIS, triển khai thật) và Yêu cầu 5 (3 tình huống kiểm thử): chưa đụng tới, sẽ báo riêng khi bắt đầu.
- Spring Boot không chạy trên IIS, nên khi đến Yêu cầu 4 cách triển khai sẽ khác đề gốc. Phi sẽ quyết định và thông báo sau.

## 5. Trạng thái

- Đã soạn xong `Jenkinsfile` và hướng dẫn cấu hình. **Chưa chạy thử trên Jenkins.**
- Còn chờ Phi chốt vài điểm cấu hình (repo public hay private, cổng Jenkins có đổi khỏi 8080 để tránh trùng cổng ứng dụng hay không).
- Bản build đầu tiên trên `master` sẽ cho biết trạng thái thật của `master`. Nếu build đỏ vì lý do của một module, người phụ trách module đó sẽ được báo.

## 6. Cần phản hồi

Báo lại Phi nếu có lo ngại hoặc xung đột với kế hoạch này, đặc biệt:
- Agent 1: quy tắc profile mặc định ở mục 3 có khả thi không.
- Agent nào đang có test cần Docker hay DB trong profile mặc định.
