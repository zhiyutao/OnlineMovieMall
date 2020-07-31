package com.mintyi.fablix.controller;

import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Star;
import com.mintyi.fablix.service.StarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/star")
public class StarController {
    @Autowired
    private StarService starService;

    @GetMapping("/singlestar")
    public ModelAndView getSingleStar(HttpServletRequest request, @RequestParam(value="id", required = true) String id) {
        HttpSession session = request.getSession();
        String referAddress = (String)session.getAttribute("returnAddr");
        Star starInfo = starService.queryStarById(id);
        List<Movie> starMovieList = starService.getStarInMovie(id);
        ModelAndView res = new ModelAndView("singlestar");
        res.addObject("starInfo",starInfo);
        res.addObject("starMovieList",starMovieList);
        res.addObject("returnAddress",referAddress);
        return res;
    }
    @PostMapping(value = "/singlestar")
    @ResponseBody
    public Map<String, Object> addStar(@RequestParam String name, @RequestParam(required = false) Integer year){
//        String maxId = starService.getMaxId();
//        String id = maxId.replaceAll("\\d+", "") + (Integer.parseInt(maxId.replaceAll("\\D+", ""))+1);
        String insertId = starService.insertStar(name, year);
        Map<String, Object> a = new HashMap<>();
        if (insertId == null) {
            a.put("data", "fail");
            a.put("type", "insertStar");
        }
        else {
            a.put("data", "success");
            a.put("type", "insertStar");
            a.put("insertId", insertId);
        }
        return a;
    }
}
