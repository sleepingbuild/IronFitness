# ADS – Tích hợp quảng cáo AdMob (yêu cầu bài nhóm)

Task: **W1-TV1-08** (quyết định + đặc tả), **W2-TV3-ADS** (banner/interstitial), **W2-TV2-ADS** (cấu hình, ẩn quảng cáo cho Premium).

## 1. Quyết định (ADR #8)

Bài nhóm yêu cầu app đã tích hợp **AdMob hoặc In-App Purchase**. Chọn **AdMob**.

| Tiêu chí | AdMob | In-App Purchase |
|---|---|---|
| Cần tài khoản trả phí / Play Console | Không (dùng Test ID) | Có, và phải có bản phát hành thử |
| Chạy demo trên emulator | Được | Khó, cần Play Store + tài khoản tester |
| Ảnh hưởng schema/API | Không | Phải thêm xác thực biên lai phía server |
| Rủi ro tiến độ | Thấp | Cao |

Hệ quả:
- Đây là ngoại lệ có chủ đích cho quy tắc "không thêm thư viện ngoài danh sách": cho phép `com.google.android.gms:play-services-ads`.
- "Thanh toán" vẫn **ngoài MVP**. Cờ `isPremium` hiện có được dùng để **tắt quảng cáo** (không cần thanh toán thật).
- Có thể làm IAP ở giai đoạn sau nếu nhóm muốn, không ảnh hưởng thiết kế này.

## 2. Vị trí quảng cáo

| Vị trí | Loại | Quy tắc |
|---|---|---|
| Cuối màn Danh mục và Danh sách bài tập | Banner | Luôn hiện khi không Premium |
| Cuối màn Lịch sử / Thống kê | Banner | Như trên |
| Sau khi hoàn thành buổi tập (trạng thái FINISHED), trước màn tổng kết | Interstitial | Tối đa 1 lần mỗi 3 phút; không bao giờ hiện **trong lúc** tập |
| Workout Player đang chạy | Không có | Tránh làm gián đoạn đếm ngược |

Người dùng `isPremium = true` **không thấy quảng cáo nào**.

## 3. Cấu hình

### 3.1 Dependency (đã thêm vào `android/app/build.gradle`)
```groovy
implementation "com.google.android.gms:play-services-ads:25.5.0"
```
Phiên bản 25.5.0 lấy từ trang release của Google Play services; trước khi merge hãy đối chiếu với release notes AdMob và nâng nếu có bản mới. Yêu cầu minSdk của SDK thấp hơn minSdk 26 của dự án, nên không xung đột.

### 3.2 AndroidManifest (bắt buộc, thiếu là app crash khi khởi động)
Trong thẻ `<application>`:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713" />
```
Giá trị trên là **App ID thử nghiệm của Google**.

### 3.3 Khởi tạo
Đã tạo lớp `HomeFitApp` (`com.homefit.app.HomeFitApp`), gọi `AdsManager.init(this)` trong `onCreate()`, và đăng ký trong Manifest bằng `android:name=".HomeFitApp"`.

### 3.4 ID thử nghiệm và ID thật
- Trong dev, demo và nộp bài: **chỉ dùng ID thử nghiệm** (đã đặt sẵn trong `AdsManager`).
- Không bấm vào quảng cáo thật của chính mình (vi phạm chính sách AdMob, có thể bị khóa tài khoản).
- Nếu sau này dùng ID thật: tạo tài khoản AdMob, khai báo app, đặt ID qua `resValue`/`local.properties`, không hard-code vào Git.

## 4. API nội bộ (`com.homefit.app.ads.AdsManager`)

| Hàm | Dùng ở đâu |
|---|---|
| `init(Application)` | Khởi động app |
| `setAdsEnabled(boolean)` | Gọi với `!isPremium` sau khi có `UserProfileEntity` hoặc sau đăng nhập |
| `attachBanner(FrameLayout)` → `AdView` | Fragment có banner; **gọi `destroy()` ở `onDestroyView`** |
| `showInterstitialIfReady(Activity, Runnable)` → `boolean` | Sau FINISHED; `Runnable` luôn được gọi đúng một lần dù có hiện quảng cáo hay không; trả về `true` nếu quảng cáo được hiện |
| `isInterstitialReady()`, `isAdsEnabled()` | Chỉ để hiển thị trạng thái (màn demo) |
| `showInterstitial(Activity, Runnable, boolean ignoreInterval)` | Chỉ dùng cho màn demo, bỏ qua giới hạn 3 phút |

Quy tắc Android: Fragment không gọi thẳng SDK ngoài `AdsManager`; chỉ dùng các hàm trên.

## 5. Layout
Mỗi màn có banner thêm vào cuối layout một container:
```xml
<FrameLayout
    android:id="@+id/adContainer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
Đặt dưới RecyclerView/ConstraintLayout, không che nút điều hướng.

## 6. Kiểm tra (QA – Agent 5)
1. Chạy bản debug trên emulator có Google Play: banner hiện nhãn "Test Ad".
2. Hoàn thành một buổi tập: interstitial thử nghiệm hiện, đóng được, rồi sang màn tổng kết.
3. Tập liên tiếp hai buổi trong vòng 3 phút: lần hai **không** hiện interstitial.
4. Đặt `isPremium = true` (sửa DB hoặc qua Admin): không còn banner và interstitial.
5. Tắt mạng: app không crash, màn vẫn dùng bình thường (banner trống).
6. Màn Player: không có quảng cáo nào.

## 7. Nộp bài
Chuẩn bị ảnh chụp màn hình banner Test Ad, interstitial Test Ad và đoạn code `AdsManager` để minh chứng "đã tích hợp AdMob".

## 8. Màn demo tạm (`AdsDemoActivity`)

Để giảng viên thấy ngay AdMob hoạt động mà chưa cần các màn hình thật, có một màn tạm là màn khởi động của app (`com.homefit.app.ui.ads.AdsDemoActivity`):
- **Banner Test Ad** ở cuối màn hình.
- Nút **Hiện quảng cáo toàn màn hình**: mở interstitial thử nghiệm; đóng xong app tiếp tục bình thường.
- Nút **Giả lập Premium**: tắt quảng cáo (banner biến mất, nút interstitial bị khóa) và bật lại, minh họa quy tắc "Premium không thấy quảng cáo".
- Hai dòng trạng thái: quảng cáo bật/tắt, interstitial đã tải xong hay chưa.

Màn này **tạm thời**. Khi Agent 2 làm Splash và Agent 3 làm màn Danh mục, đổi launcher trong Manifest, gắn banner/interstitial vào màn thật theo mục 2, rồi xóa `AdsDemoActivity`, `activity_ads_demo.xml` và `strings_ads.xml`.

Kịch bản demo gợi ý (khoảng 1 phút): mở app thấy banner Test Ad, bấm nút Interstitial để hiện quảng cáo toàn màn hình và đóng lại, bấm Giả lập Premium để banner biến mất, bấm lại để banner quay về, rồi mở `AdsManager.java` và Manifest để chỉ chỗ tích hợp.

## 9. Hướng dẫn tạo tài khoản và ID trên AdMob (tùy chọn)

Chỉ cần làm nếu giảng viên muốn thấy cấu hình trong bảng điều khiển AdMob. Với bài nhóm, **ID thử nghiệm là đủ và an toàn hơn**. Giao diện AdMob có thể khác đôi chút so với mô tả dưới đây.

1. Vào `https://admob.google.com`, đăng nhập bằng tài khoản Google, chọn quốc gia, múi giờ, đơn vị tiền tệ và đồng ý điều khoản.
2. **Apps → Add app**: chọn nền tảng **Android**; ở câu hỏi "app đã có trên Google Play chưa" chọn **Không**; đặt tên `HomeFit`.
3. Sau khi tạo, ghi lại **App ID** dạng `ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy`.
4. Trong app vừa tạo chọn **Ad units → Add ad unit**:
   - **Banner**, tên `home_banner` → ghi lại **Ad unit ID** dạng `ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz`.
   - **Interstitial**, tên `workout_finished_interstitial` → ghi lại ID thứ hai.
5. Chụp màn hình trang Apps và Ad units để làm minh chứng nộp bài (che bớt mã nhà xuất bản nếu cần).

### Dùng ID thật trong app
1. Thay App ID trong `AndroidManifest.xml` (thẻ `meta-data` APPLICATION_ID).
2. Thay hai hằng `BANNER_UNIT_ID` và `INTERSTITIAL_UNIT_ID` trong `AdsManager.java`.
3. Trước khi chạy, thêm thiết bị của bạn vào danh sách thiết bị thử nghiệm (qua `RequestConfiguration`, lấy mã thiết bị từ dòng log "Use RequestConfiguration.Builder().setTestDeviceIds" trong Logcat) để không bị tính là bấm quảng cáo thật.

### Cảnh báo
- Đơn vị quảng cáo mới có thể **chưa hiện quảng cáo** trong vài giờ đầu hoặc báo "No fill". Đây là bình thường, không phải lỗi code.
- **Không bấm vào quảng cáo thật của chính mình.** Vi phạm chính sách AdMob có thể bị khóa tài khoản.
- Khi demo hoặc nộp bài, nên để lại ID thử nghiệm trong code để chắc chắn quảng cáo luôn hiện.
- Không dùng ID thật trong `main` nếu chưa thống nhất cả nhóm; mỗi thành viên có thể tự dùng ID riêng trên máy.
