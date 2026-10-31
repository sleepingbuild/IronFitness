package com.homefit.server.repository;

import com.homefit.server.domain.Category;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Danh mục đang hoạt động (lần đồng bộ đầu tiên). */
    List<Category> findByActiveTrueOrderBySortOrderAscIdAsc();

    /** Mọi bản ghi mới đổi sau mốc, kể cả bản vừa bị tắt (để app xóa khỏi Room). */
    List<Category> findByUpdatedAtAfterOrderBySortOrderAscIdAsc(Instant updatedAfter);
}
