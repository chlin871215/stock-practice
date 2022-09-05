package com.example.stockpractice.controller;

import com.example.stockpractice.controller.response.GetResponse;
import com.example.stockpractice.model.entity.StockInfo;
import com.example.stockpractice.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stock")
public class InfoController {
    @Autowired
    private InfoService infoService;

    @GetMapping("/info")
    public GetResponse getAllStockInfo() {
        List<StockInfo> stockInfo = infoService.getAllStockInfo();
        if (null==stockInfo){
            return new GetResponse(null,null,null,"StockInfo is empty");
        }
        return new GetResponse(null,null,stockInfo,null);
    }

    @GetMapping("/info/{stock}")
    public GetResponse getStockInfoByStock(@PathVariable String stock) {
        List<StockInfo> stockInfo =new ArrayList<>();
        stockInfo.add(infoService.getStockInfoByStock(stock));
        if (null==stockInfo){
            return new GetResponse(null,null,null,"StockInfo doesn't exist");
        }
        return new GetResponse(null,null,stockInfo,null);

    }
}
