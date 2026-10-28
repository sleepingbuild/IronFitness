# API – HomeFit REST v1

Hợp đồng giữa Android và Backend. **Đổi API phải cập nhật file này trước** và báo TV2/TV3/TV4.

- Base URL: `/api/v1`
- Định dạng: JSON, UTF-8. Tên trường dạng `camelCase`.
- Thời gian: ISO-8601 UTC, ví dụ `2026-10-24T08:30:00Z`.
- Xác thực: header `Authorization: Bearer <accessToken>` (trừ nhóm Auth). Client **không gửi** header này tới `/auth/*`.

## Lỗi thống nhất

```json
{ "code": "VALIDATION_ERROR", "message": "Email không hợp lệ", "details": { "email": "invalid" } }
```

| HTTP | code | Khi nào |
|---|---|---|
| 400 | VALIDATION_ERROR | Dữ liệu sai |
| 401 | UNAUTHORIZED | Thiếu/hết hạn token |
| 403 | FORBIDDEN | Không đủ quyền |
| 404 | NOT_FOUND | Không tồn tại |
| 409 | CONFLICT | Trùng (email đã đăng ký) |
| 500 | INTERNAL_ERROR | Lỗi server |

## 1. Auth

### POST /auth/register
Validate: email hợp lệ; mật khẩu tối thiểu 8 ký tự; `displayName` 1–100 ký tự. Email đã tồn tại trả 409 `CONFLICT`.

```json
// request
{ "email": "a@mail.com", "password": "Min8chars", "displayName": "An" }
// 201
{ "accessToken": "...", "refreshToken": "...", "expiresIn": 3600, "user": { /* User, cùng cấu trúc với GET /me */ } }
```

### POST /auth/login
```json
{ "email": "a@mail.com", "password": "Min8chars" }
// 200: giống register
```

### POST /auth/google
```json
{ "idToken": "<Google ID token>" }
// 200: giống register (tạo user mới nếu chưa có)
```

### POST /auth/refresh
```json
{ "refreshToken": "..." }
// 200: { "accessToken": "...", "refreshToken": "...", "expiresIn": 3600 }
```

## 2. Hồ sơ cá nhân

### GET /me
```json
// 200 User
{ "id": 1, "email": "a@mail.com", "displayName": "An", "isPremium": false,
  "heightCm": 170.0, "weightKg": 65.0 }
```

### PUT /me/profile
```json
{ "displayName": "An", "heightCm": 170.0, "weightKg": 65.0 }
// 200 User
```
Validate: `heightCm` 50–250, `weightKg` 20–300.

## 3. Thư viện bài tập (đọc: user đăng nhập)

### GET /categories?updatedAfter=<ISO>
Trả danh mục đang `isActive`. Có `updatedAfter` thì chỉ trả bản ghi mới đổi (kể cả vừa bị tắt, đánh dấu `isActive:false` để app xóa khỏi Room).
```json
[ { "id": 1, "name": "Bụng", "slug": "abs", "iconUrl": null,
    "sortOrder": 1, "isActive": true, "updatedAt": "2026-10-24T08:00:00Z" } ]
```

### GET /exercises?categoryId=1&updatedAfter=<ISO>
```json
[ { "id": 10, "categoryId": 1, "name": "Plank", "description": "...",
    "mediaUrl": "https://...", "mediaType": "GIF",
    "durationSec": 30, "reps": null, "restSec": 15,
    "sortOrder": 1, "isActive": true, "updatedAt": "2026-10-24T08:00:00Z" } ]
```
Bỏ `categoryId` để lấy tất cả (app dùng khi đồng bộ lần đầu).

## 4. Lịch sử tập

### POST /workout-sessions
Idempotent theo `clientUuid`: gửi lại cùng uuid trả 200 với bản ghi cũ, không tạo trùng.
```json
{ "clientUuid": "7c1e...-uuid", "categoryId": 1,
  "startedAt": "2026-10-24T08:00:00Z", "completedAt": "2026-10-24T08:20:00Z",
  "totalSeconds": 1200, "exercisesDone": 8 }
// 201 (mới) hoặc 200 (đã có)
{ "id": 55, "clientUuid": "7c1e...-uuid" }
```

### POST /workout-sessions/batch
Đồng bộ nhiều bản ghi chưa sync.
```json
{ "sessions": [ { /* như trên */ } ] }
// 200
{ "syncedUuids": ["..."], "failed": [ { "clientUuid": "...", "reason": "..." } ] }
```

### GET /workout-sessions?from=<ISO>&to=<ISO>
Dùng khi đăng nhập máy mới để khôi phục lịch sử vào Room. Chỉ trả buổi tập của user trong token, lọc theo `completedAt`.
```json
// 200
[ { "id": 55, "clientUuid": "7c1e...-uuid", "categoryId": 1,
    "startedAt": "2026-10-24T08:00:00Z", "completedAt": "2026-10-24T08:20:00Z",
    "totalSeconds": 1200, "exercisesDone": 8 } ]
```

## 5. Admin API (role ADMIN, tùy chọn)

Admin Web dùng controller Thymeleaf riêng tại `/admin`. Nếu cần API cho công cụ khác:

| Method | Path | Mô tả |
|---|---|---|
| POST/PUT/DELETE | /admin/api/categories[/{id}] | CRUD danh mục |
| POST/PUT/DELETE | /admin/api/exercises[/{id}] | CRUD bài tập (DELETE = xóa mềm) |
| GET | /admin/api/stats | `{ "totalUsers": 120, "totalSessions": 840 }` |

## 6. Ghi chú cho client

- Đồng bộ danh mục/bài tập: lưu `lastSyncAt` (lấy từ `updatedAt` lớn nhất nhận được), lần sau gọi với `updatedAfter`.
- Timeout gợi ý: connect 10s, read 20s.
- Không retry tự động với lỗi 4xx (trừ 401 → refresh một lần).
- Lỗi mạng ở client dùng mã nội bộ `NETWORK_ERROR` (không có trong bảng lỗi server).
- Refresh token thất bại (401/403): xóa phiên và đưa người dùng về màn Đăng nhập.
