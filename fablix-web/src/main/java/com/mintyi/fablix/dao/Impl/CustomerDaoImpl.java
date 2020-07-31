package com.mintyi.fablix.dao.Impl;

import com.mintyi.fablix.dao.CustomDao;
import com.mintyi.fablix.domain.Creditcard;
import com.mintyi.fablix.domain.Customer;
import com.mintyi.fablix.domain.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerDaoImpl implements CustomDao {
    @Autowired
    JdbcTemplate readTemplate;
    @Autowired
    JdbcTemplate writeTemplate;
    @Autowired
    TransactionTemplate transactionTemplate;

    @Override
    public Customer getCustomerByEmail(String email) {
        List<Customer> customers = readTemplate.query("select * from customers where email = ?", new Object[]{email}, new BeanPropertyRowMapper<>(Customer.class));
        if(customers.isEmpty()){
            return null;
        }
        return customers.get(0);
    }

    @Override
    public Creditcard getCreditcardById(String cardNumber) {
        List<Creditcard> creditcards = readTemplate.query("select * from creditcards where id = ?", new Object[]{cardNumber}, new BeanPropertyRowMapper<>(Creditcard.class));
        if (creditcards.isEmpty()) {
            return null;
        }
        return creditcards.get(0);
    }


    @Override
    public Integer insertSale (ArrayList<ShoppingCart> shoppingCart){
        String sql = "insert into sales(customerId,movieId,saleDate,count) values (?, ?, ?, ?);";
        return transactionTemplate.execute(t->{
            try {
                int[] ints = writeTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setInt(1, shoppingCart.get(i).getCustomerId());
                        preparedStatement.setString(2, shoppingCart.get(i).getMovieId());
                        preparedStatement.setString(3, shoppingCart.get(i).getSaleDate());
                        preparedStatement.setInt(4, shoppingCart.get(i).getCount());
                    }

                    @Override
                    public int getBatchSize() {
                        return shoppingCart.size();
                    }
                });
                Integer orderN;
                orderN = (Integer) writeTemplate.queryForObject("select max(id) from sales", Class.forName("java.lang.Integer"));
                return orderN;
            } catch (Exception e) {
                t.setRollbackOnly();
                return null;
            }
        });
    }

}
