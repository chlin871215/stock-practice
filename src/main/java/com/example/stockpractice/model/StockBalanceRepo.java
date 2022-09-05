package com.example.stockpractice.model;

import com.example.stockpractice.model.entity.StockBalance;
import com.example.stockpractice.model.entity.StockBalancePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface StockBalanceRepo extends JpaRepository<StockBalance, StockBalancePK> {

    StockBalance findByStock(String stock);

    /*@Modifying
    @Query(value = "replace INTO `test`.`tcnud` (`TradeDate`, `BranchNo`, `CustSeq`, `DocSeq`, `Stock`, `Price`, `Qty`, `RemainQty`, `Fee`, `Cost`, `ModDate`, `ModTime`, `ModUser`) VALUES (?1, ?2, ?3, ?4,?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13);",nativeQuery = true)
    @Transactional
    void replace(String tradeDate,String branchNo,String customerSeq,String docSeq,String stock,Double price,int qty,int remainQty,int fee,int cost,String modDate, String modTime,String modUser);
     */
}
