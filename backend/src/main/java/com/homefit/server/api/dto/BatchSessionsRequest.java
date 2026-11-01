package com.homefit.server.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lô buổi tập chưa đồng bộ. Từng phần tử được kiểm tra riêng trong service
 * để một bản ghi lỗi không chặn cả lô.
 */
public record BatchSessionsRequest(
        @NotEmpty(message = "Danh sách buổi tập không được rỗng")
        @Size(max = 100, message = "Mỗi lô tối đa 100 buổi tập")
        List<WorkoutSessionRequest> sessions) {
}
