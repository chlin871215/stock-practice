package com.example.stockpractice.controller.response;

import com.example.stockpractice.model.entity.StockBalance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String response;
    private StockBalance stockBalance;
}
