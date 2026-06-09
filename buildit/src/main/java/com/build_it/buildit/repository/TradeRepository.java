package com.build_it.buildit.repository;
import com.build_it.buildit.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
