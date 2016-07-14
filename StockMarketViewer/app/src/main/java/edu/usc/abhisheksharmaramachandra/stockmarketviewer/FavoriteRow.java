package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

public class FavoriteRow {
    String name;
    String symbol;
    String price;
    String change;
    String marketcap;

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getChange() {
        return change;
    }

    public String getMarketcap() {
        return marketcap;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public void setMarketcap(String marketcap) {
        this.marketcap = marketcap;
    }



}
