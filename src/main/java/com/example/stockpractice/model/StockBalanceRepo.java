package com.example.stockpractice.model;

import com.example.stockpractice.model.entity.StockBalance;
import com.example.stockpractice.model.entity.StockBalancePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockBalanceRepo extends JpaRepository<StockBalance, StockBalancePK> {

    @Query(value = "select * from tcnud where BranchNo = ?1 and CustSeq = ?2 and stock = ?3 limit 1 ;",nativeQuery = true)
    StockBalance findByBranchNoAndCustSeqAndStock(String branchNo, String custSeq, String stock);

    @Query(value = "select distinct Stock from tcnud where BranchNo = ?1 and CustSeq = ?2 order by Stock  ;",nativeQuery = true)
    List<String> getAllStock(String branchNo, String custSeq);


}
