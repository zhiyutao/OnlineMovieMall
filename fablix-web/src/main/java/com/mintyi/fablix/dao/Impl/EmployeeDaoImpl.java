package com.mintyi.fablix.dao.Impl;

import com.mintyi.fablix.dao.EmployeeDao;
import com.mintyi.fablix.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeDaoImpl implements EmployeeDao {
    @Autowired
    JdbcTemplate readTemplate;

    @Override
    public Employee getEmployeeByEmail(String email) {
        List<Employee> employees = readTemplate.query("select * from employees where email = ?", new Object[]{email}, new BeanPropertyRowMapper<>(Employee.class));
        if(employees.isEmpty()){
            return null;
        }
        return employees.get(0);
    }
}
