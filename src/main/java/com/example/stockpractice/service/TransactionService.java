package com.example.stockpractice.service;

import com.example.stockpractice.controller.request.TransactionRequest;
import com.example.stockpractice.model.StockBalanceRepo;
import com.example.stockpractice.model.StockInfoRepo;
import com.example.stockpractice.model.TransactionRepo;
import com.example.stockpractice.model.entity.StockBalance;
import com.example.stockpractice.model.entity.TransactionDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

//交易
@Service
public class TransactionService {

    @Autowired
    StockInfoRepo stockInfoRepo;
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    StockBalanceRepo stockBalanceRepo;

    public String transaction(TransactionRequest transactionRequest) {
        //check:request資訊是否正確、股票餘額是否足夠
        {
            transactionRequest.setBsType(transactionRequest.getBsType().toUpperCase());//BsType轉大寫
            //check:stock 不得為空值或空白鍵
            if (transactionRequest.getStock().isBlank() || transactionRequest.getStock().isEmpty()) {
                return "Stock data wrong";
            }
            //check:stock是否存在
            if (null == stockInfoRepo.findByStock(transactionRequest.getStock())) {
                return "This Stock doesn't exist";
            }
            //check:qty不得為空或小於等於0
            if (transactionRequest.getQty() <= 0 || null == transactionRequest.getQty()) {
                return "Qty data wrong";
            }
            //check:bsType
            if (transactionRequest.getBsType().isEmpty() || transactionRequest.getBsType().isBlank()) {
                return "BsType data wrong";
            }
            if (!(transactionRequest.getBsType().equals("B") || transactionRequest.getBsType().equals("S"))) {
                return "BsType must be B or S";
            }
            //check:stockBalance
            if (transactionRequest.getBsType().equals("S")) {
                if (null == stockBalanceRepo.findByStock(transactionRequest.getStock()) || stockBalanceRepo.findByStock(transactionRequest.getStock()).getQty() - transactionRequest.getQty() < 0) {
                    return "Stock Balance doesn't enough";
                }
            }
        }
        //創建明細--------------------------------------------------------------------------------------------------
        TransactionDetail transactionDetail = new TransactionDetail();
        {
            transactionDetail.setTradeDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));//tradeDate
            transactionDetail.setBranchNo("F62S");//branchNo
            transactionDetail.setCustomerSeq("04");//customerSeq
            transactionDetail.setDocSeq(getNewDocSeq(transactionDetail.getTradeDate()));//docSeq
            transactionDetail.setStock(transactionRequest.getStock());//stock
            transactionDetail.setBsType(transactionRequest.getBsType());//bsType
            transactionDetail.setPrice(stockInfoRepo.findByStock(transactionRequest.getStock()).getCurPrice());//price
            transactionDetail.setQty(transactionRequest.getQty());//qty
            transactionDetail.setAmt(getAmt(transactionDetail.getPrice(), transactionDetail.getQty()));//單價*股數=amt
            transactionDetail.setFee(getFee(transactionDetail.getAmt()));//fee
            transactionDetail.setTax(getTax(transactionDetail.getAmt(), transactionDetail.getBsType()));//根據bsType決定tax
            transactionDetail.setTransferTax(0);//交易稅目前為0
            transactionDetail.setNetAmt(getNetAmt(transactionDetail.getAmt(), transactionDetail.getFee(), transactionDetail.getTax(), transactionDetail.getBsType()));//根據四項數據得到淨收付
            transactionDetail.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));//modDate
            transactionDetail.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));//modTime
            transactionDetail.setModUser("Berlin");//modUser
            transactionRepo.save(transactionDetail);//存進sql
        }
        //更新餘額--------------------------------------------------------------------------------------------------
        {
            StockBalance oldStockBalance;
            StockBalance newStockBalance = new StockBalance();
            if (null != stockBalanceRepo.findByStock(transactionRequest.getStock())) {//if stockBalance is existing
                oldStockBalance = stockBalanceRepo.findByStock(transactionRequest.getStock());//get oldData
                stockBalanceRepo.delete(oldStockBalance);//delete oldData in repo
                newStockBalance.setPrice(getBalancePrice(oldStockBalance.getPrice(), oldStockBalance.getQty(), transactionDetail.getPrice(), transactionDetail.getQty()));
                newStockBalance.setQty(getBalanceQty(oldStockBalance.getQty(), transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setRemainQty(getBalanceQty(oldStockBalance.getQty(), transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setFee(oldStockBalance.getFee() + transactionDetail.getFee());
                newStockBalance.setCost(getBalanceCost(oldStockBalance.getCost(), transactionDetail.getNetAmt(), transactionDetail.getBsType()));
            } else {
                newStockBalance.setPrice(getBalancePrice(0.0, 0, transactionDetail.getPrice(), transactionDetail.getQty()));
                newStockBalance.setQty(getBalanceQty(0, transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setRemainQty(getBalanceQty(0, transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setFee(transactionDetail.getFee());
                newStockBalance.setCost(getBalanceCost(0, transactionDetail.getNetAmt(), transactionDetail.getBsType()));
            }
            newStockBalance.setTradeDate(transactionDetail.getTradeDate());
            newStockBalance.setBranchNo(transactionDetail.getBranchNo());
            newStockBalance.setCustomerSeq(transactionDetail.getCustomerSeq());
            newStockBalance.setDocSeq(transactionDetail.getDocSeq());
            newStockBalance.setStock(transactionDetail.getStock());
            newStockBalance.setModDate(transactionDetail.getModDate());
            newStockBalance.setModTime(transactionDetail.getModTime());
            newStockBalance.setModUser(transactionDetail.getModUser());
            if (newStockBalance.getQty() > 0) {//only save when qty>0
                stockBalanceRepo.save(newStockBalance);
            } else {
                return "Transaction complete,qty is 0";
            }
        }

        return "Transaction successful";
    }

    //查詢未實現損益------------------------------------------------------------------------------------------------
    /*
    查詢剩餘股數表
    取得成本、剩餘股數
    查詢股票資訊表
    取得現價
    未實現損益＝現價*剩餘股數-成本-賣之手續費-賣之交易稅
    回傳字串
     */
    public String unrealizedGainsAndLosses(String stock) {
        //check
        {
            if (stock.isEmpty() || stock.isBlank()) {
                return "Stock data wrong";
            }
            if (null == stockInfoRepo.findByStock(stock)) {
                return "Stock doesn't exist";
            }
            if (null == stockBalanceRepo.findByStock(stock)) {
                return "Stock Balance doesn't enough";
            }
        }
        //process
        {
            int cost = stockBalanceRepo.findByStock(stock).getCost();
            int qty = stockBalanceRepo.findByStock(stock).getQty();
            Double curPrice = stockInfoRepo.findByStock(stock).getCurPrice();
            return Integer.toString((int) (curPrice * qty) - cost - getFee(getAmt(curPrice, qty)) - getTax(getAmt(curPrice, qty), "S"));
        }
    }

    //method-------------------------------------------------------------------------------------------------------

    private String getNewDocSeq(String tradeDate) {//流水單號
        String lastDocSeqEng = "AA";
        int lastDocSeqInt = 0;
        if (null != transactionRepo.getNewDocSeq(tradeDate)) {
            lastDocSeqEng = transactionRepo.getNewDocSeq(tradeDate).substring(0, 2);//取英文0~1
            lastDocSeqInt = Integer.parseInt(transactionRepo.getNewDocSeq(tradeDate).substring(2, 5));//取數字2~4
        }
        List<Integer> engToAscii = lastDocSeqEng.chars().boxed().collect(Collectors.toList());//英文轉ascii
        //數字+1
        lastDocSeqInt++;
        //進位處理--------------------------------------------------------------------------------------------------
        {
            if (lastDocSeqInt > 999) {//如果超過999則歸1且英文進位
                lastDocSeqInt = 1;//歸1
                engToAscii.set(1, engToAscii.get(1) + 1);//英文進位
                if (engToAscii.get(1) > 90) {//如果超過Z
                    engToAscii.set(1, 65);//歸A
                    engToAscii.set(0, engToAscii.get(0) + 1);//進位
                    if (engToAscii.get(0) > 90 && engToAscii.get(0) < 97) {//如果超過Z
                        engToAscii.set(0, 97);//超過預設數據最大量，若超過給臨時數據庫aA001~zA999
                    }
                }
            }
        }
        //數值轉字串之檢查---------------------------------------------------------------------------------------------
        {
            String newDocSeqInt = String.format("%03d", lastDocSeqInt);//數值轉字串
            String newDocSeqEng = "";
            for (int ascii : engToAscii) {
                newDocSeqEng += Character.toString(ascii);//list英文ascii轉字串
            }
            return newDocSeqEng + newDocSeqInt;
        }
    }

    private int getAmt(double price, int qty) {
        return (int) (price * qty);
    }

    private int getFee(int amt) {
        int fee = (int) Math.round(amt * 0.001425);
        return fee;
    }

    private int getTax(int amt, String bsType) {
        if (bsType.equals("S"))
            return (int) Math.round(amt * 0.003);
        return 0;
    }

    private int getNetAmt(int amt, int fee, int tax, String bsType) {
        if (bsType.equals("B")) {
            return -(amt + fee);
        }
        return amt - fee - tax;
    }

    private Double getBalancePrice(Double oldPrice, int oldQty, Double newPrice, int newQty) {//依比例計算價格
        return (oldPrice * oldQty + newPrice * newQty) / (oldQty + newQty);
    }

    private int getBalanceCost(int oldNetAmt, int newNetAmt, String bsType) {
        if (bsType.equals("B")) {
            return Math.abs(oldNetAmt) + Math.abs(newNetAmt);
        } else {
            return Math.abs(oldNetAmt - newNetAmt);
        }
    }

    private int getBalanceQty(int oldQty, int newQty, String bsType) {
        if (bsType.equals("B"))
            return oldQty + newQty;
        return oldQty - newQty;
    }

}
