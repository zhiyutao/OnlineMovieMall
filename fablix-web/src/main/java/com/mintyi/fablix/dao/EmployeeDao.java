package com.mintyi.fablix.dao;

import com.mintyi.fablix.domain.Employee;


public interface EmployeeDao {
    public Employee getEmployeeByEmail(String email);
}
