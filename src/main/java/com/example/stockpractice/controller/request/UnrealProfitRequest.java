package com.example.stockpractice.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnrealProfitRequest {
    private String branchNo;
    private String custSeq;
    private String stock;
}
