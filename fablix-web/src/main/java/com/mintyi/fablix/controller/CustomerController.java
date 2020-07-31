package com.mintyi.fablix.controller;

import com.mintyi.fablix.dao.CustomDao;
import com.mintyi.fablix.domain.Creditcard;
import com.mintyi.fablix.domain.Customer;
import com.mintyi.fablix.domain.ShoppingCart;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class CustomerController {
    @Autowired
    CustomDao customDao;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    ServletContext servletContext;

    private String renderConfirmedOrder(ArrayList<ShoppingCart> shoppingCarts, int lastOrderNumber, HttpServletRequest request, HttpServletResponse response) {
        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("shoppingCarts", shoppingCarts);
        context.setVariable("beginOrderNumber", lastOrderNumber - shoppingCarts.size() + 1);
        HashSet<String> set = new HashSet<>();
        set.add("order_refresh");
        return templateEngine.process("orderTemplate", set,context);
    }

    @GetMapping("/checkout")
    public ModelAndView goCheckout(HttpServletRequest request) {
        Map<String, ShoppingCart> allItem = getAllItem(request);
        ModelAndView view = new ModelAndView("checkout");
        if(allItem != null)
            view.addObject("allItem", allItem.values());
        else
            view.addObject("allItem", null);
        return view;
    }
    @PostMapping(value = "/checkout")
    @ResponseBody
    public Map<String, Object> checkout(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String cardNumber, @RequestParam String expirationDate, HttpServletRequest request, HttpServletResponse response) {
        Creditcard creditcard = customDao.getCreditcardById(cardNumber);
        Map<String, Object> a = new HashMap<String, Object>();
        if (creditcard == null) {
            a.put("data", "failure");
            a.put("reason", "Card number is wrong");
            return a;
        } else if (!creditcard.getFirstName().equals(firstName)) {
            a.put("data", "failure");
            a.put("reason", "First name is wrong");
            return a;
        } else if (!creditcard.getLastName().equals(lastName)) {
            a.put("data", "failure");
            a.put("reason", "Last name is wrong");
            return a;
        } else if (!creditcard.getExpiration().equals(expirationDate)) {
            a.put("data", "failure");
            a.put("reason", "Expiration date is wrong");
            return a;
        } else {
            HttpSession session = request.getSession();
            Map<String, ShoppingCart> shoppingCartHashMap = (Map<String, ShoppingCart>) session.getAttribute("cart");
            if (shoppingCartHashMap == null) {
                a.put("data", "failure");
                a.put("reason", "Nothing in your shopping cart");
                return a;
            } else {
                LocalDate date = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                shoppingCartHashMap.values().stream().forEach(s->{
                    s.setSaleDate(date.format(formatter));
                });
                ArrayList<ShoppingCart> shoppingCarts = new ArrayList<>(shoppingCartHashMap.values());
                Integer orderNumbers = customDao.insertSale(shoppingCarts);
                if(orderNumbers == null){
                    a.put("data", "failure");
                    a.put("reason", "Transaction fail!");
                } else {
                    session.removeAttribute("cart");
                    a.put("data", "success");
                    a.put("html", renderConfirmedOrder(shoppingCarts, orderNumbers, request, response));
                }
                return a;
            }
        }
    }

    @PostMapping("/cart/")
    @ResponseBody
    public Map<String, ShoppingCart> addItem(@RequestParam("id") String movieId, @RequestParam String title,
                                             HttpServletRequest request, HttpServletResponse response) {
        Customer customer = (Customer) request.getSession().getAttribute("user");
        HttpSession session = request.getSession();
        Map<String, ShoppingCart> shoppingCartHashMap;
        if(session.getAttribute("cart") == null){
            session.setAttribute("cart", new ConcurrentHashMap<String, ShoppingCart>());
        }
        shoppingCartHashMap = (Map<String, ShoppingCart>) session.getAttribute("cart");
        if(shoppingCartHashMap.containsKey(movieId)){
            ShoppingCart s = shoppingCartHashMap.get(movieId);
            s.setCount(s.getCount() + 1);
        } else {
            ShoppingCart s = new ShoppingCart();
            s.setCount(1); s.setCustomerId(customer.getId()); s.setMovieId(movieId);
            s.setTitle(title);

            shoppingCartHashMap.put(movieId, s);
        }
        return shoppingCartHashMap;
    }
    @PutMapping("/cart/")
    @ResponseBody
    public Map<String, ShoppingCart> updateItem(@RequestParam("id") String movieId, @RequestParam int count, HttpServletRequest request, HttpServletResponse response) {
        Customer customer = (Customer) request.getSession().getAttribute("user");
        HttpSession session = request.getSession();
        Map<String, ShoppingCart> shoppingCartHashMap;
        shoppingCartHashMap = (Map<String, ShoppingCart>) session.getAttribute("cart");
        ShoppingCart s = shoppingCartHashMap.get(movieId);
        s.setCount(count);
        return shoppingCartHashMap;
    }
    @DeleteMapping("/cart/")
    @ResponseBody
    public Map<String, ShoppingCart> deleteItem(@RequestParam("id") String movieId, HttpServletRequest request, HttpServletResponse response) {
        Customer customer = (Customer) request.getSession().getAttribute("user");
        HttpSession session = request.getSession();
        Map<String, ShoppingCart> shoppingCartHashMap;
        shoppingCartHashMap = (Map<String, ShoppingCart>) session.getAttribute("cart");
        shoppingCartHashMap.remove(movieId);
        return shoppingCartHashMap;
    }
    @GetMapping("/cart/")
    @ResponseBody
    public Map<String, ShoppingCart> getAllItem(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Map<String, ShoppingCart>) session.getAttribute("cart");
    }

    @GetMapping("/login")
    public String getLoginPage(HttpServletRequest request, HttpServletResponse response) {
        if(request.getSession().getAttribute("user") != null){
            try {
                response.sendRedirect("/");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "index"; // can be omitted
        }
        return "login";
    }

    @RequestMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession s = request.getSession(false);
            if(s != null)
                s.invalidate();
            response.sendRedirect("/login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public Map<String, String> login(@RequestParam String email, @RequestParam String password, @RequestParam(value="g-recaptcha-response", required = false) String gRecaptchaResponse, @RequestParam(value="zxc-android-request", required=false) String fromAndroid, HttpServletRequest request) {
        Map<String, String> a = new HashMap<String, String>();
//        if(fromAndroid == null) {
//            try {
//                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//            } catch (Exception e) {
//                a.put("data", "failure");
//                a.put("reason", "recaptcha verification error:\n" + e.getMessage());
//                return a;
//            }
//        }
        Customer customer = customDao.getCustomerByEmail(email);
        boolean success = false;
        if(customer != null)
            success = new StrongPasswordEncryptor().checkPassword(password, customer.getPassword());
        if(!success){
            a.put("data", "failure");
            a.put("reason", "User doesn't exist or password is wrong!");
        } else {
            request.getSession().setAttribute("user", customer);
            a.put("email", customer.getEmail());
            a.put("data", "success");
        }
        return a;
    }
}
