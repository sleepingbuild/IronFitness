# Design tokens – HomeFit

Nguồn sự thật cho giao diện Android. Giá trị nằm ở `res/values/colors.xml`, `values-night/colors.xml`, `dimens.xml`, `styles.xml`, `themes.xml`. Task: W2-TV6-01. Người sở hữu: Agent 6 (frontend).

## 1. Nguyên tắc

1. Nền tảng là **Material 3** (`Theme.Material3.DayNight.NoActionBar`), tông **cam sắt** (năng động) trên nền **trung tính ấm**, điểm nhấn **thép xám xanh** và **xanh ngọc** cho trạng thái nghỉ.
2. Layout **không ghi mã màu, không ghi cỡ chữ cứng**: dùng `?attr/colorXxx`, `@dimen/...`, `@style/TextAppearance.HomeFit.*`.
3. Mọi màu có cặp sáng/tối cùng tên (`hf_*`). Trạng thái không chỉ truyền đạt bằng màu: luôn kèm chữ hoặc icon.
4. Chữ dùng `sp`, vùng chạm tối thiểu 48dp, nút cao 52dp (nút Player 64dp).

## 2. Màu (vai trò Material 3)

| Vai trò | Sáng | Tối | Dùng cho |
|---|---|---|---|
| primary / onPrimary | `#B83F00` / `#FFFFFF` | `#FFB596` / `#5A1B00` | Nút chính, tab đang chọn, nhấn mạnh |
| primaryContainer / on | `#FFDBCC` / `#380D00` | `#832B00` / `#FFDBCC` | Chip, vùng nhấn nhẹ |
| secondary / onSecondary | `#4E5F6B` / `#FFFFFF` | `#B6C9D8` / `#20333F` | Hành động phụ |
| secondaryContainer / on | `#D2E4F2` / `#0A1D28` | `#364956` / `#D2E4F2` | Nút tonal, chỉ báo tab |
| tertiary / onTertiary | `#00696B` / `#FFFFFF` | `#4CD9DB` / `#003738` | Nghỉ, hồi phục |
| error / onError | `#BA1A1A` / `#FFFFFF` | `#FFB4AB` / `#690005` | Lỗi, thao tác nguy hiểm |
| background = surface / on | `#FFFBF8` / `#201A17` | `#181210` / `#ECE0DA` | Nền màn hình |
| surfaceContainer | `#F8EDE6` | `#241C19` | Thẻ, ô nhóm |
| surfaceContainerHigh | `#F2E7E0` | `#2F2723` | Thanh điều hướng, nhấn nhẹ |
| onSurfaceVariant | `#52443C` | `#D8C2B8` | Chữ phụ, mô tả |
| outline / outlineVariant | `#85736B` / `#D8C2B8` | `#A08D84` / `#52443C` | Viền ô nhập / đường phân cách |

Các màu còn lại (container, surface dim/bright, inverse) xem `colors.xml`.

### Màu ngữ nghĩa

| Token | Sáng | Tối | Dùng cho |
|---|---|---|---|
| `hf_phase_exercise` (+ `hf_on_phase_exercise`) | `#B83F00` / `#FFFFFF` | `#FFB596` / `#5A1B00` | Player đang tập |
| `hf_phase_rest` (+ on) | `#00696B` / `#FFFFFF` | `#4CD9DB` / `#003738` | Player đang nghỉ |
| `hf_phase_paused` (+ on) | `#4E5F6B` / `#FFFFFF` | `#B6C9D8` / `#20333F` | Player tạm dừng |
| `hf_bmi_underweight` | `#1D5FA8` | `#A5C8FF` | Nhãn BMI "Gầy" |
| `hf_bmi_normal` | `#1B6D2F` | `#7DDB8B` | "Bình thường" |
| `hf_bmi_overweight` | `#8A5100` | `#FFB95C` | "Thừa cân" |
| `hf_bmi_obese` | `#BA1A1A` | `#FFB4AB` | "Béo phì" |

Màu BMI dùng làm **màu chữ trên nền surface** và luôn đi kèm nhãn chữ.

## 3. Tỉ lệ tương phản (WCAG, tính bằng script)

Yêu cầu: chữ thường ≥ 4,5:1; viền và biểu tượng ≥ 3:1.

| Cặp | Sáng | Tối |
|---|---|---|
| onPrimary / primary | 5,60 | 7,73 |
| onPrimaryContainer / primaryContainer | 13,26 | 7,04 |
| onSecondary / secondary | 6,62 | 7,68 |
| onTertiary / tertiary | 6,50 | 7,64 |
| onError / error | 6,46 | 7,72 |
| onSurface / surface | 16,71 | 14,34 |
| onSurface / surfaceContainer | 14,94 | 12,94 |
| onSurfaceVariant / surface | 9,07 | 10,88 |
| primary / surface (chữ nhấn) | 5,44 | 10,86 |
| primary / surfaceContainer | 4,87 | 9,80 |
| BMI (4 màu) / surface | ≥ 6,2 | ≥ 10,8 |
| outline / surface (viền, cần ≥ 3) | 4,38 | 5,86 |

Mọi cặp chữ đều đạt ≥ 4,5:1.

## 4. Khoảng cách (bội số 4dp)

`space_xs` 4 · `space_sm` 8 · `space_md` 12 · `space_lg` 16 · `space_xl` 24 · `space_xxl` 32 · `screen_padding` 16.

## 5. Bo góc

`corner_small` 12 (nút, ô nhập) · `corner_medium` 16 (thẻ) · `corner_large` 28 (khung lớn, bottom sheet).

## 6. Chữ

| Kiểu | Cỡ | Đậm | Dùng cho |
|---|---|---|---|
| `TextAppearance.HomeFit.Timer` | 72sp | Có (số cùng độ rộng) | Đếm ngược Player |
| `.Headline` | 28sp | Có | Tiêu đề màn hình |
| `.Title` | 20sp | Có | Tiêu đề thẻ, mục |
| `.Subtitle` | 16sp | Vừa | Phụ đề |
| `.Body` | 16sp | Không | Nội dung |
| `.BodySmall` | 14sp | Không | Mô tả phụ |
| `.Caption` | 12sp | Không | Nhãn nhỏ, tab |
| `.Button` | 16sp | Có | Nhãn nút |

Dùng phông hệ thống (Roboto), không thêm phông ngoài.

## 7. Thành phần

| Thành phần | Style | Ghi chú |
|---|---|---|
| Nút chính | mặc định `MaterialButton` (`Widget.HomeFit.Button`) | Cao 52dp, bo 12dp |
| Nút Player | `Widget.HomeFit.Button.Large` | Cao 64dp |
| Nút phụ | `Widget.HomeFit.Button.Tonal` | |
| Nút viền | `Widget.HomeFit.Button.Outlined` | |
| Nút chữ | `Widget.HomeFit.Button.Text` | Cao tối thiểu 48dp |
| Ô nhập | `TextInputLayout` mặc định (outlined, bo 12dp) | Cao tối thiểu 56dp |
| Thẻ | `MaterialCardView` mặc định (`Widget.HomeFit.Card`) | Nền surfaceContainer, bo 16dp |
| Thẻ viền | `Widget.HomeFit.Card.Outlined` | |
| Thanh điều hướng dưới | `Widget.HomeFit.BottomNavigationView` | Có nhãn chữ, 4 tab |

## 8. Biểu tượng

Vector 24dp, tô `#FF000000` trong drawable và để thành phần tự đổi màu. Hiện có: `ic_nav_workout`, `ic_nav_history`, `ic_nav_profile`, `ic_nav_settings`.

## 9. Quy tắc màn hình

1. Mỗi màn có đủ ba trạng thái: đang tải, rỗng, lỗi (nút "Thử lại").
2. Insets (thanh trạng thái, thanh điều hướng, bàn phím) do `MainActivity` xử lý; fragment không cộng thêm.
3. Mọi ảnh/icon có ý nghĩa cần `contentDescription`; ảnh trang trí đặt `importantForAccessibility="no"`.
4. Banner quảng cáo chỉ ở Danh mục, Danh sách bài, Lịch sử (xem `docs/ADS.md`); không có trong Player.
