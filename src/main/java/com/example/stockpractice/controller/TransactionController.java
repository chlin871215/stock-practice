package com.example.stockpractice.controller;

import com.example.stockpractice.controller.request.TransactionRequest;
import com.example.stockpractice.controller.response.TransactionResponse;
import com.example.stockpractice.model.StockBalanceRepo;
import com.example.stockpractice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    TransactionService transactionService;
    @Autowired
    StockBalanceRepo stockBalanceRepo;

    //交易--------------------------------------------------------------------------------------------------------
    @PostMapping()
    public TransactionResponse transaction(@RequestBody TransactionRequest transactionRequest) {
        String response = transactionService.transaction(transactionRequest);
        if (response.equals("Transaction successful")){
            return new TransactionResponse(response,stockBalanceRepo.findByStock(transactionRequest.getStock()));
        }
        return new TransactionResponse(response,null);
    }


    //查詢未實現損益------------------------------------------------------------------------------------------------
    @GetMapping("/{stock}")
    public TransactionResponse unrealizedGainsAndLosses(@PathVariable String stock){
        String response = transactionService.unrealizedGainsAndLosses(stock);
        return new TransactionResponse(response,null);
    }
}
