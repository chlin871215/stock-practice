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
    //private int customer;目前僅設customer為04
    private String stock;
    private String bsType;
    private Integer qty;
}
