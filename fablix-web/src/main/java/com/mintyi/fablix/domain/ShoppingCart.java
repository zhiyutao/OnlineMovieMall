package com.mintyi.fablix.domain;

public class ShoppingCart {
//    customerId INT NOT NULL,
//    movieId VARCHAR(10) NOT NULL,
//    count INT NOT NULL DEFAULT 1,
    private int  customerId;
    private String movieId;
    private int count;
    private String title;
    private String saleDate;

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private double singlePrice;
    public double getSinglePrice() {return singlePrice;}
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
        singlePrice = 0.99 + (Math.abs(movieId.hashCode()) % 12);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
