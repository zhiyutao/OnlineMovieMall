package com.mintyi.fablix.controller;

import com.mintyi.fablix.domain.Genre;
import com.mintyi.fablix.service.MovieService;
import com.mintyi.fablix.service.StarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

@Controller
public class MainController {
    @Autowired
    MovieService movieService;
    @Autowired
    StarService starService;

    final static ArrayList<Character> ALPHABET;
    static {
        ArrayList<Character> a = new ArrayList<>();
        for(char i = '0'; i <= '9'; ++ i)
            a.add(i);
        for(char i = 'A'; i <= 'Z'; ++ i)
            a.add(i);
        a.add('*');
        ALPHABET = a;
    };

    public <T> ArrayList<ArrayList<T>> divideColumns(int colnum, ArrayList<T> originList) {
        int q = originList.size() / colnum, m = originList.size() % colnum;
        ArrayList<ArrayList<T>> res = new ArrayList<>(colnum);
        int j = 0;
        for(int i = 0; i < m; ++ i) {
            res.add(new ArrayList<>(originList.subList(j, j + q + 1)));
            j += (q + 1);
        }
        for(int i = m; i < colnum; ++ i) {
            res.add(new ArrayList<>(originList.subList(j, j + q)));
            j += q;
        }
        return res;
    }
    @GetMapping("/")
    public ModelAndView index() {
        // generate alphabet list
        ArrayList<ArrayList<Character>> chars = divideColumns(4, ALPHABET);
        // generate genres list
        ArrayList<Genre> g = new ArrayList<>(movieService.getAllGenre());
        ArrayList<ArrayList<Genre>> genres = divideColumns(4, g);
        ModelAndView res = new ModelAndView("index");
        res.addObject("chars", chars);
        res.addObject("genres", genres);
        return res;
    }
}
