package com.example.stockpractice.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    private String tradeDate;
    private String branchNo;
    private String docSeq;
    private String custSeq;
    private String stock;
    private Double price;
    private String bsType;
    private Double qty;
}
