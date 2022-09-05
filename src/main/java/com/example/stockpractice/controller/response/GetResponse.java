package com.example.stockpractice.controller.response;

import com.example.stockpractice.model.entity.StockBalance;
import com.example.stockpractice.model.entity.StockInfo;
import com.example.stockpractice.model.entity.TransactionDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetResponse {
    private List<TransactionDetail> transactionDetails;
    private List<StockBalance> stockBalances;
    private List<StockInfo> stockInfos;
    private String response;
}
