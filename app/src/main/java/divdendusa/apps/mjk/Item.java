package divdendusa.apps.mjk;

public class Item {
    String seq;
    String stockSymbol;
    String companyName;
    String dividendYield;
    String closingPrice;
    String annualizedDividend;
    String exdivDate;
    String payDate;

    public String getSeq() {
        return seq;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }
    public String getCompanyName() {
        return companyName;
    }

    public String getDividendYield() {
        return dividendYield;
    }

    public String getClosingPrice() {
        return closingPrice;
    }

    public String getAnnualizedDividend() {
        return annualizedDividend;
    }

    public String getExdivDate() {
        return exdivDate;
    }

    public String getPayDate() {
        return payDate;
    }

       public Item(String seq,
                String stockSymbol,
                String companyName,
                String dividendYield,
                String closingPrice,
                String annualizedDividend,
                String exdivDate,
                String payDate
                ){
        this.seq=seq;
        this.stockSymbol=stockSymbol;
        this.companyName=companyName;
        this.companyName=companyName;
        this.dividendYield=dividendYield;
        this.closingPrice=closingPrice;
        this.annualizedDividend=annualizedDividend;
        this.exdivDate=exdivDate;
        this.payDate=payDate;
    }
}
