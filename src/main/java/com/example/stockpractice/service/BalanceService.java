package com.example.stockpractice.service;

import com.example.stockpractice.model.StockBalanceRepo;
import com.example.stockpractice.model.entity.StockBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BalanceService {

    @Autowired
    StockBalanceRepo stockBalanceRepo;


    public List<StockBalance> getAllStockBalance() {
        List<StockBalance> stockBalances = stockBalanceRepo.findAll();
        return stockBalances;
    }

//    public StockBalance getStockBalanceByStock(String stock) {
//        StockBalance stockBalance = stockBalanceRepo.findByStock(stock);
//        return stockBalance;
//    }


}
