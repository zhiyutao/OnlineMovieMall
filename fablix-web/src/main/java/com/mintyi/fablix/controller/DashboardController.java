package com.mintyi.fablix.controller;

import com.mintyi.fablix.dao.EmployeeDao;
import com.mintyi.fablix.dao.MetadataDao;
import com.mintyi.fablix.domain.Employee;
import com.mintyi.fablix.domain.Table;
import com.mintyi.fablix.support.RecaptchaVerifyUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {
    @Autowired
    EmployeeDao employeeDao;
    @Autowired
    MetadataDao metadataDao;
    @Autowired
    ServletContext servletContext;

    @GetMapping("/_dashboard")
    public ModelAndView getDashboard(){
        ModelAndView view = new ModelAndView("dashboard");
        List<Table> allTables = metadataDao.getAllTables();
        view.addObject("tableList", allTables);
        return view;
    }

    @GetMapping(value = "/_dashboard/table", params = {"name"})
    @ResponseBody
    public Table getTableColumns(@RequestParam String name) {
        Table table = new Table();
        table.setName(name);
        table.setColumns(metadataDao.getAllColumns(name));
        return table;
    }

    @PostMapping(value = "/_dashboard/login")
    @ResponseBody
    public Map<String, String> login(@RequestParam String email, @RequestParam String password, @RequestParam(value="g-recaptcha-response", required = false) String gRecaptchaResponse, HttpServletRequest request) {
        Map<String, String> a = new HashMap<>();
        try{
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        }catch (Exception e){
            a.put("data", "failure");
            a.put("reason", "recaptcha verification error:\n" + e.getMessage());
            return a;
        }
        Employee employee = employeeDao.getEmployeeByEmail(email);
        boolean success = false;
        if(employee != null)
            success = new StrongPasswordEncryptor().checkPassword(password, employee.getPassword());
        if(!success){
            a.put("data", "failure");
            a.put("reason", "User doesn't exist or password is wrong!");
        }
        else {
            request.getSession().setAttribute("employee", employee);
            a.put("data", "success");
        }
        return a;
    }

}
