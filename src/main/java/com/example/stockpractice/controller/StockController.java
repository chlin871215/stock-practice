package com.example.stockpractice.controller;

import com.example.stockpractice.controller.response.GetResponse;
import com.example.stockpractice.model.entity.TransactionDetail;
import com.example.stockpractice.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/transactions")
    public GetResponse getAllTransactionDetails() {
        List<TransactionDetail> transactionDetails = stockService.getAllTransactionDetails();
        if (null==transactionDetails){
            return new GetResponse(null,null,null,"TransactionDetail is empty");
        }
        return new GetResponse(transactionDetails,null,null,null);
    }

    @GetMapping("/transaction")
    public GetResponse getTransactionByDocSeqAndTradeDate(@RequestParam String docSeq, @RequestParam String tradeDate) {
        List<TransactionDetail> transactionDetail = new ArrayList<>();
        transactionDetail.add(stockService.getTransactionByDocSeq(docSeq,tradeDate));
        if (null==transactionDetail){
            return new GetResponse(null,null,null,"Transaction doesn't exist");
        }
        return new GetResponse(transactionDetail,null,null,null);
    }


}
