package com.mintyi.fablix.dao;

import com.mintyi.fablix.domain.Creditcard;
import com.mintyi.fablix.domain.Customer;
import com.mintyi.fablix.domain.ShoppingCart;

import java.util.ArrayList;


public interface CustomDao {
    public Customer getCustomerByEmail(String email);
    public Creditcard getCreditcardById(String cardNumber);
    public Integer insertSale (ArrayList<ShoppingCart> shoppingCart);
}
