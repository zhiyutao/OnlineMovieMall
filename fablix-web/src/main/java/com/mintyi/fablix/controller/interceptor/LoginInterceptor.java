package com.mintyi.fablix.controller.interceptor;

import com.mintyi.fablix.domain.Customer;
import com.mintyi.fablix.domain.Employee;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Customer user = (Customer) (session.getAttribute("user"));
        Employee employee = (Employee) (session.getAttribute("employee"));
        if(user == null && employee == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        try {
            Customer user = (Customer) request.getSession(false).getAttribute("user");
            if (user == null) {
                Employee employee = (Employee) request.getSession(false).getAttribute("employee");
                modelAndView.addObject("logged_email", employee.getEmail());
            } else
                modelAndView.addObject("logged_email", user.getEmail());
        } catch (Exception e) {
            // System.out.println("LoginInterceptor postHandle ERROR");
        }
    }
}
