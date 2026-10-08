package com.spendsense.repository;

import com.spendsense.model.InsightCache;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightCacheRepository extends JpaRepository<InsightCache, Long> {

    Optional<InsightCache> findByUserIdAndPeriodStartAndPeriodEnd(
            Long userId,
            String periodStart,
            String periodEnd
    );

    void deleteByUserIdAndPeriodStartAndPeriodEnd(Long userId, String periodStart, String periodEnd);
}
