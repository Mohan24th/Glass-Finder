package com.glassfinder.repository;

import com.glassfinder.entity.GlassBox;
import com.glassfinder.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByBoxOrderByCreatedAtDesc(GlassBox box);

    @Query("""
        SELECT COALESCE(
            SUM(
                CASE
                    WHEN s.transactionType = 'ADD' THEN s.quantity
                    WHEN s.transactionType = 'SOLD' THEN -s.quantity
                    ELSE 0
                END
            ),
            0
        )
        FROM StockTransaction s
        WHERE s.box = :box
    """)
    Long calculateCurrentStock(GlassBox box);
}