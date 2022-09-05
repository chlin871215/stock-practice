package com.example.stockpractice.service;

import com.example.stockpractice.model.TransactionRepo;
import com.example.stockpractice.model.entity.TransactionDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    @Autowired
    private TransactionRepo transactionRepo;

    public List<TransactionDetail> getAllTransactionDetails() {
        List<TransactionDetail> transactionDetails = transactionRepo.findAll();
        return transactionDetails;
    }

    public TransactionDetail getTransactionByDocSeq(String docSeq,String tradeDate) {
        TransactionDetail transactionDetail = transactionRepo.findByDocSeqAndTradeDate(docSeq,tradeDate);
        return transactionDetail;
    }


}
