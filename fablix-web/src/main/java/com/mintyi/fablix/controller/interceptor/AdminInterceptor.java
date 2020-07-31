package com.mintyi.fablix.controller.interceptor;

import com.mintyi.fablix.controller.exception.RoleValidationException;
import com.mintyi.fablix.domain.Employee;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Employee employee = (Employee) (session.getAttribute("employee"));
        if(employee == null){
            throw new RoleValidationException("Please log in as an employee!");
        }
        return true;
    }
}
