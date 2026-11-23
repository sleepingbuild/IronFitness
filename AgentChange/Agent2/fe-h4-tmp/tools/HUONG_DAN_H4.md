# H4: chọn chi nhánh khi bán gói và đổi gói (Agent 2)

Dựa trên `GET /api/v1/branches` (Agent 1, trả `id`, `name`) và `branchId` tuỳ chọn ở `RegisterPackageRequest`, `UpgradeDowngradeRequest`.
Để trống `branchId`: bán gói = không gắn chi nhánh; đổi gói = giữ chi nhánh của gói hiện tại.

## 7 file trong `frontend/src/features/membership/`
| File | Thay đổi |
|---|---|
| `types.ts` | Thêm `Branch` |
| `api.ts` | Thêm `listBranches()` |
| `branchOptions.ts` (mới) | Tạo danh sách lựa chọn và `branchIdOrUndefined` |
| `branchOptions.test.ts` (mới) | 4 test cho hai hàm trên |
| `BranchSelect.tsx` (mới) | Ô chọn chi nhánh: ẩn khi không có chi nhánh, khoá và báo khi không tải được |
| `SellForMember.tsx` | Lễ tân bán gói hộ: thêm ô chi nhánh, gửi `branchId` |
| `ChangePlan.tsx` | Nâng, hạ cấp: ô chi nhánh hiện sau khi chọn gói mới, mặc định "Giữ chi nhánh hiện tại" |

## Chưa làm
Màn hội viên tự đăng ký (`PackageDetail.tsx`) chưa có chọn chi nhánh vì mình chưa đọc file đó.

## Giới hạn
Mình chưa chạy được `npm`; chỉ kiểm tra cú pháp bằng tsc (không có lỗi cú pháp). Cần `lint`, `typecheck`, `test`, `build` trên máy bạn.
