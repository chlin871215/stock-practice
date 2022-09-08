package com.example.stockpractice.controller;

import com.example.stockpractice.controller.request.TransactionRequest;
import com.example.stockpractice.controller.request.UnrealProfitRequest;
import com.example.stockpractice.controller.response.SumUnrealProfit;
import com.example.stockpractice.controller.response.TransactionResponse;
import com.example.stockpractice.model.StockBalanceRepo;
import com.example.stockpractice.model.TransactionRepo;
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
    @Autowired
    TransactionRepo transactionRepo;

    //交易--------------------------------------------------------------------------------------------------------
    @PostMapping()
    public TransactionResponse transaction(@RequestBody TransactionRequest transactionRequest) {
        String response = transactionService.transaction(transactionRequest);
        if (response.equals("Transaction successful") || response.equals("Transaction complete,qty is 0")) {
            return new TransactionResponse(response, transactionRepo.findByDocSeqAndTradeDate(transactionRequest.getDocSeq(), transactionRequest.getTradeDate()), stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getStock()));
        }
        return new TransactionResponse(response, null, null);
    }


    //查詢未實現損益------------------------------------------------------------------------------------------------
    @PostMapping("/unreal")
    public SumUnrealProfit unrealizedGainsAndLosses(@RequestBody UnrealProfitRequest unrealProfitRequest) {
        return transactionService.unrealizedGainsAndLosses(unrealProfitRequest);
    }
}
