package com.example.stockpractice.service;

import com.example.stockpractice.controller.request.TransactionRequest;
import com.example.stockpractice.controller.request.UnrealProfitRequest;
import com.example.stockpractice.controller.response.SumUnrealProfit;
import com.example.stockpractice.controller.response.UnrealProfitResult;
import com.example.stockpractice.model.StockBalanceRepo;
import com.example.stockpractice.model.StockInfoRepo;
import com.example.stockpractice.model.TransactionRepo;
import com.example.stockpractice.model.entity.StockBalance;
import com.example.stockpractice.model.entity.StockInfo;
import com.example.stockpractice.model.entity.TransactionDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
        if (null != check(transactionRequest)) return check(transactionRequest);
        //創建明細--------------------------------------------------------------------------------------------------
        getRandomPrice(transactionRequest.getStock());// 讓股票資訊價格隨機更動
        TransactionDetail transactionDetail = new TransactionDetail();
        {
            transactionDetail.setTradeDate(transactionRequest.getTradeDate());//tradeDate
            transactionDetail.setBranchNo(transactionRequest.getBranchNo());//branchNo
            transactionDetail.setCustomerSeq(transactionRequest.getCustSeq());//customerSeq
            transactionDetail.setDocSeq(transactionRequest.getDocSeq());//docSeq
            transactionDetail.setStock(transactionRequest.getStock());//stock
            transactionDetail.setBsType(transactionRequest.getBsType());//bsType
            transactionDetail.setPrice(transactionRequest.getPrice());//price
            transactionDetail.setQty(transactionRequest.getQty());//qty
            transactionDetail.setAmt(getAmt(transactionDetail.getPrice(), transactionDetail.getQty()));//單價*股數=amt
            transactionDetail.setFee(getFee(transactionDetail.getAmt()));//fee
            transactionDetail.setTax(getTax(transactionDetail.getAmt(), transactionDetail.getBsType()));//根據bsType決定tax
            transactionDetail.setTransferTax(0.0);//交易稅目前為0
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
            if (null != stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionDetail.getBranchNo(), transactionDetail.getCustomerSeq(), transactionDetail.getStock())) {//if stockBalance is existing
                oldStockBalance = stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionDetail.getBranchNo(), transactionDetail.getCustomerSeq(), transactionDetail.getStock());//get oldData
                stockBalanceRepo.delete(oldStockBalance);//delete oldData in repo
                newStockBalance.setPrice(getBalancePrice(oldStockBalance.getPrice(), oldStockBalance.getQty(), transactionDetail.getPrice(), transactionDetail.getQty()));
                newStockBalance.setQty(getBalanceQty(oldStockBalance.getQty(), transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setRemainQty(getBalanceQty(oldStockBalance.getRemainQty(), transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setFee(oldStockBalance.getFee() + transactionDetail.getFee());
                newStockBalance.setCost(getBalanceCost(oldStockBalance.getCost(), transactionDetail.getNetAmt(), transactionDetail.getBsType()));
            } else {
                newStockBalance.setPrice(getBalancePrice(0.0, 0.0, transactionDetail.getPrice(), transactionDetail.getQty()));
                newStockBalance.setQty(getBalanceQty(0.0, transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setRemainQty(getBalanceQty(0.0, transactionDetail.getQty(), transactionDetail.getBsType()));
                newStockBalance.setFee(transactionDetail.getFee());
                newStockBalance.setCost(getBalanceCost(0.0, transactionDetail.getNetAmt(), transactionDetail.getBsType()));
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
    public SumUnrealProfit unrealizedGainsAndLosses(UnrealProfitRequest unrealProfitRequest) {
        //check
        if ("001".equals(check(unrealProfitRequest))) return new SumUnrealProfit(null, "001", "查無符合資料");
        if (null != check(unrealProfitRequest)) return new SumUnrealProfit(null, "002", check(unrealProfitRequest));
        //process
        {
            List<String> stockList;
            if (unrealProfitRequest.getStock().isBlank()) {
                stockList = stockBalanceRepo.getAllStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq());
            } else {
                stockList = new ArrayList<>();
                stockList.add(unrealProfitRequest.getStock());
            }
            List<UnrealProfitResult> unrealProfitResults =new ArrayList<>();
            for (String stock : stockList) {
                getRandomPrice(stock);// 讓股票資訊價格隨機更動
                StockBalance stockBalance =stockBalanceRepo.findByBranchNoAndCustSeqAndStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq(),stock);
                StockInfo stockInfo =stockInfoRepo.findByStock(stock);

                UnrealProfitResult unrealProfitResult =new UnrealProfitResult();

                unrealProfitResult.setStock(stock);
                unrealProfitResult.setStockName(stockInfo.getStockName());
                unrealProfitResult.setNowPrice(stockInfo.getCurPrice());
                unrealProfitResult.setSumRemainQty(stockBalance.getRemainQty());
                unrealProfitResult.setSumFee(stockBalance.getFee());
                unrealProfitResult.setSumCost(stockBalance.getCost());
                unrealProfitResult.setSumMarketValue(unrealProfitResult.getNowPrice() * unrealProfitResult.getSumRemainQty());
                unrealProfitResult.setSumUnrealProfit(getUnreal(stock, unrealProfitResult.getSumCost(), unrealProfitResult.getSumRemainQty()));
                unrealProfitResults.add(unrealProfitResult);
            }
            return new SumUnrealProfit(unrealProfitResults,"000","");
        }
    }

    //method-------------------------------------------------------------------------------------------------------

    private Double getUnreal(String stock, Double cost, Double qty) {
        Double curPrice = stockInfoRepo.findByStock(stock).getCurPrice();
        return (double) Math.round((curPrice * qty) - cost - getFee(getAmt(curPrice, qty)) - getTax(getAmt(curPrice, qty), "S"));
    }

    private String check(TransactionRequest transactionRequest) {
        //check:request資訊是否正確、股票餘額是否足夠
        transactionRequest.setBsType(transactionRequest.getBsType().toUpperCase());
        //check:tradeDate
        if (transactionRequest.getTradeDate().isBlank()) return "TradeDate data wrong";
        //check:branchNo
        if (transactionRequest.getBranchNo().isBlank()) return "BranchNo data wrong";
        //check:custSeq
        if (transactionRequest.getCustSeq().isBlank()) return "CustSeq data wrong";
        //check:docSeq
        if (transactionRequest.getDocSeq().isBlank()) return "DocSeq data wrong";
        //check:stock
        if (transactionRequest.getStock().isBlank()) return "Stock data wrong";
        //check:price
        if (transactionRequest.getPrice() <= 0) return "Price data wrong";
        //check:docSeq是否存在
        if (null != transactionRepo.findByDocSeqAndTradeDate(transactionRequest.getDocSeq(), transactionRequest.getTradeDate()))
            return "This DocSeq already exist";
        //check:stock是否存在
        if (null == stockInfoRepo.findByStock(transactionRequest.getStock())) return "This Stock doesn't exist";
        //check:qty不得為空或小於等於0或含有小數
        if (transactionRequest.getQty() <= 0 || null == transactionRequest.getQty() || transactionRequest.getQty() % 1 != 0)
            return "Qty data wrong";
        //qty不得超過9位數
        if (transactionRequest.getQty() >= 1_000_000_000) return "Qty too much";
        //check:bsType不得為空或空白
        if (transactionRequest.getBsType().isEmpty() || transactionRequest.getBsType().isBlank()) {
            return "BsType data wrong";
        }
        if (!(transactionRequest.getBsType().equals("B") || transactionRequest.getBsType().equals("S"))) {
            return "BsType must be B or S";
        }
        //check:stockBalance
        if (transactionRequest.getBsType().equals("S")) {
            if (null == stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getStock()) || stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getStock()).getQty() - transactionRequest.getQty() < 0) {
                return "Stock Balance doesn't enough";
            }
        } else if (null != stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getStock()) && transactionRequest.getQty() + stockBalanceRepo.findByBranchNoAndCustSeqAndStock(transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getStock()).getRemainQty() >= 1_000_000_000) {//remainQty不得超過9位數
            return "RemainQty too much";
        }
        return null;
    }

    private String check(UnrealProfitRequest unrealProfitRequest) {
        if (unrealProfitRequest.getBranchNo().isBlank()) return "BranchNo data wrong";
        if (unrealProfitRequest.getCustSeq().isBlank()) return "CustSeq data wrong";
        if (unrealProfitRequest.getStock().isBlank()) {
            return null;
        } else if (null == stockBalanceRepo.findByBranchNoAndCustSeqAndStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq(), unrealProfitRequest.getStock())) return "Stock doesn't exist";
        if (null == stockBalanceRepo.findByBranchNoAndCustSeqAndStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq(), unrealProfitRequest.getStock()))
            return "001";
        return null;
    }

    private String getNewDocSeq(String tradeDate) {//流水單號
        String lastDocSeqEng = "AA";
        int lastDocSeqInt = 0;
        if (null != transactionRepo.getNewDocSeq(tradeDate)) {
            lastDocSeqEng = transactionRepo.getNewDocSeq(tradeDate).substring(0, 2);//取英文0~1
            lastDocSeqInt = Integer.parseInt(transactionRepo.getNewDocSeq(tradeDate).substring(2, 5));//取數字2~4
        }
        List<Integer> engToAscii = lastDocSeqEng.chars().boxed().collect(Collectors.toList());//英文轉ascii,box()之作用為將int轉為INTEGER
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
            String newDocSeqInt = String.format("%03d", lastDocSeqInt);//數值轉字串，%03d：表示補0到第3位
            String newDocSeqEng = "";
            for (int ascii : engToAscii) {
                newDocSeqEng += Character.toString(ascii);//list英文ascii轉字串
            }
            return newDocSeqEng + newDocSeqInt;
        }
    }

    private Double getAmt(Double price, Double qty) {
        return price * qty;
    }

    private Integer getFee(Double amt) {
        return (int) Math.round(amt * 0.001425);
    }

    private Integer getTax(Double amt, String bsType) {
        return (bsType.equals("S")) ? (int) Math.round(amt * 0.003) : 0;
    }

    private Double getNetAmt(Double amt, Integer fee, Integer tax, String bsType) {
        return (bsType.equals("B")) ? (-(amt + fee)) : (amt - fee - tax);
    }

    private Double getBalancePrice(Double oldPrice, Double oldQty, Double newPrice, Double newQty) {//依比例計算價格
        return (oldPrice * oldQty + newPrice * newQty) / (oldQty + newQty);
    }

    private Double getBalanceCost(Double oldNetAmt, Double newNetAmt, String bsType) {
        return (bsType.equals("B")) ? (Math.abs(oldNetAmt) + Math.abs(newNetAmt)) : (Math.abs(oldNetAmt - newNetAmt));
    }

    private Double getBalanceQty(Double oldQty, Double newQty, String bsType) {
        return (bsType.equals("B")) ? (oldQty + newQty) : (oldQty - newQty);
    }

    private void getRandomPrice(String stock) {
        StockInfo stockInfo = stockInfoRepo.findByStock(stock);
        Double oldPrice = stockInfo.getCurPrice();
        Double newPrice, r;
        do {
            r = Math.random() / 10;//0~9.99，最高漲跌幅9.99％
            newPrice = ((Math.random() * 10) < 5) ? (oldPrice * (1 + r)) : (oldPrice * (1 - r));//取二分之一機率
        } while (newPrice < 10);//股票最低價10
        stockInfo.setCurPrice(Math.round(newPrice * 10000.0) / 10000.0);//取小數點後第四位四捨五入
        stockInfoRepo.save(stockInfo);
    }

}
