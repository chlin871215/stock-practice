package com.example.stockpractice.service;

import com.example.stockpractice.model.StockInfoRepo;
import com.example.stockpractice.model.entity.StockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfoService {

    @Autowired
    StockInfoRepo stockInfoRepo;

    public List<StockInfo> getAllStockInfo() {
        List<StockInfo> stockInfo = stockInfoRepo.findAll();
        return stockInfo;
    }

    public StockInfo getStockInfoByStock(String stock) {
        StockInfo stockInfo = stockInfoRepo.findByStock(stock);
        return stockInfo;
    }

}
