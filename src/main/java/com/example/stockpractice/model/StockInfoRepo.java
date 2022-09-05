package com.example.stockpractice.model;

import com.example.stockpractice.model.entity.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockInfoRepo extends JpaRepository<StockInfo, String> {

    StockInfo findByStock(String stock);
}
