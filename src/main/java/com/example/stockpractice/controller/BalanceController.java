package com.example.stockpractice.controller;

import com.example.stockpractice.controller.response.GetResponse;
import com.example.stockpractice.model.entity.StockBalance;
import com.example.stockpractice.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stock")
public class BalanceController {


    @Autowired
    private BalanceService balanceService;

    @GetMapping("/balances")
    public GetResponse getAllStockBalances() {
        List<StockBalance> stockBalances = balanceService.getAllStockBalance();
        if (null==stockBalances){
            return new GetResponse(null,null,null,"StockBalances is empty");
        }
        return new GetResponse(null,stockBalances,null,null);
    }

//    @GetMapping("/balances/{stock}")
//    public GetResponse getStockBalanceByDocSeq(@PathVariable String stock) {
//        List<StockBalance> stockBalances =new ArrayList<>();
//        stockBalances.add(balanceService.getStockBalanceByStock(stock));
//        if (null==stockBalances){
//            return new GetResponse(null,null,null,"StockBalance doesn't exist");
//        }
//        return new GetResponse(null,stockBalances,null,null);
//    }

}
