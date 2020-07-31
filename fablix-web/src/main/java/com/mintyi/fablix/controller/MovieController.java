package com.mintyi.fablix.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mintyi.fablix.domain.Genre;
import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Rating;
import com.mintyi.fablix.domain.Star;
import com.mintyi.fablix.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/movie")
public class MovieController {

    final private MovieService movieService;
    private static final int DEFAULT_LIMIT = 25;
    @Autowired
    public MovieController(MovieService service) {
        movieService = service;
    }

    @GetMapping("/list")
    public ModelAndView getList(HttpServletRequest request) {
        String s = request.getRequestURL().toString();
        String query = request.getQueryString();
        if(!"".equals(query)) s=s+"?"+query;
        HttpSession session = request.getSession();
        session.setAttribute("returnAddr", s);
        System.out.println(s);
        List<Movie> movieList = movieService.getTopByRating(100);
        ModelAndView res = new ModelAndView("movielist");

        res.addObject("seqStartIdx", 0);
        res.addObject("limit", DEFAULT_LIMIT);
        res.addObject("maxPage", 1);
        res.addObject("movieList",  movieList);
        return res;
    }
    @GetMapping(value="/suggestion", params = {"title"})
    @ResponseBody
    public String getTitleSuggestion(@RequestParam String title) {
        List<Movie> movieList = movieService.getTitleSuggestion(title.toLowerCase());
        JsonArray jsonArray = new JsonArray();
        if (movieList.size() == 0) {
            return jsonArray.toString();
        }

        for (Movie i : movieList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("value", i.getTitle());
            JsonObject additionalDataJsonObject = new JsonObject();
            additionalDataJsonObject.addProperty("movieId", i.getId());
            jsonObject.add("data", additionalDataJsonObject);
            jsonArray.add(jsonObject);
        }
//        System.out.println(jsonArray.toString());
        return jsonArray.toString();
    }

    @GetMapping(value = "/list", params = {"genreId"})
    public ModelAndView getListByGenre(@RequestParam String genreId, @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset, @RequestParam(required = false) String order, HttpServletRequest request) {
        saveReturnAddr(request);
        if(offset == null) offset = 0;
        if(limit == null) limit = DEFAULT_LIMIT;
        if(order == null) order = "";
        List<Movie> movieList = movieService.getMovieByGenre(genreId, limit, offset, order);
        return addModelMapping(limit, offset, movieList);
    }

    @GetMapping(value = "/list", params = {"initial"})
    public ModelAndView getListByInitial(@RequestParam String initial, @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset, @RequestParam(required = false) String order, HttpServletRequest request) {
        saveReturnAddr(request);
        if(offset == null) offset = 0;
        if(limit == null) limit = DEFAULT_LIMIT;
        if(order == null) order = "";
        List<Movie> movieList = movieService.getMovieByInitial(initial, limit, offset, order);
        return addModelMapping(limit, offset, movieList);
    }

    @GetMapping(value = "/list", params = {"title","actor","director","year"})
    public ModelAndView searchMovie(@RequestParam String title, @RequestParam String actor, @RequestParam String director, @RequestParam(required = false) Integer year, @RequestParam(required = false) String order, @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset, HttpServletRequest request) {
        saveReturnAddr(request);
        if(offset == null) offset = 0;
        if(limit == null) limit = DEFAULT_LIMIT;
        if(order == null) order = "";
        if(year == null) year = 0;
        List<Movie> movieList = movieService.searchMovie(title,actor,director,year,order,limit,offset);
        return addModelMapping(limit, offset, movieList);
    }

    @GetMapping(value="/api/list", params = {"title"}, headers = {"Accept=application/json"})
    @ResponseBody
    public String searchByTitle(@RequestParam String title, @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset) {
        if(offset == null) offset = 0;
        if(limit == null) limit = DEFAULT_LIMIT;
        List<Movie> movieList = movieService.searchMovie(title, "", "", 0, "", limit, offset);
        JsonArray jsonArray = new JsonArray();
        for (Movie i : movieList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("title", i.getTitle());
            jsonObject.addProperty("id", i.getId());
            jsonObject.addProperty("year", i.getYear());
            jsonObject.addProperty("director", i.getDirector());
            List<Star> stars = i.getActors();
            for (int j=0;j<stars.size();j++) {
                jsonObject.addProperty("star"+j,stars.get(j).getName());
            }
            List<Genre> genres = i.getGenres();
            for (int j=0;j<genres.size();j++) {
                jsonObject.addProperty("genre"+j,genres.get(j).getName());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    @GetMapping("/singlemovie")
    public ModelAndView getSingleMovie(HttpServletRequest request,@RequestParam(value="id", required = true) String id) {
        HttpSession session = request.getSession();
        String referAddress = (String)session.getAttribute("returnAddr");

        Movie movie = movieService.queryMovieById(id);
        List<Star> starsList = movieService.getMovieStars(id);
        Rating rate = movieService.getMovieRating(id);
        List<Genre> genresList = movieService.getMovieGenre(id);
        ModelAndView res = new ModelAndView("singlemovie");
        res.addObject("singleMovie",movie);
        res.addObject("singleMovieStars",starsList);
        res.addObject("singleMovieRating",rate);
        res.addObject("singleMovieGenres",genresList);
        res.addObject("returnAddress",referAddress);
        return res;
    }

    private ModelAndView addModelMapping(Integer limit, Integer offset, List<Movie> movieList) {
        int num = 0;
        if(movieList.size() > 0) {
            num = movieList.get(0).getTotalNum();
        }

        int nowPage = (int) Math.ceil((offset + 1.0) / limit);
        int addPage = (int) Math.ceil((double)num / limit);
        // System.out.println(nowPage + ", " + addPage);
        ModelAndView res = new ModelAndView("movielist");
        res.addObject("movieList",  movieList);
        res.addObject("seqStartIdx", offset);
        res.addObject("limit", limit);
        res.addObject("maxPage", nowPage + addPage - 1);
        return res;
    }

    private void saveReturnAddr(HttpServletRequest request) {
        String s = request.getRequestURL().toString();
        String query = request.getQueryString();
        if (!query.equals("")) s = s + "?" + query;
        HttpSession session = request.getSession();
        session.setAttribute("returnAddr", s);
    }

    @PostMapping(value = "/singlemovie")
    @ResponseBody
    public Map<String, Object> addMovie(@RequestParam String movieTitle, @RequestParam Integer movieYear, @RequestParam String movieDirector, @RequestParam String starName, @RequestParam String genreName){

        Map<String, Object> a = movieService.insertMovie(movieTitle, movieYear, movieDirector, starName, genreName);
        if ((Integer) a.get("errcode") == -1) {
            a.put("data", "fail");
            a.put("type", "insertMovie: duplicate movie exists!");
        }
        else if((Integer) a.get("errcode") < 0){
            a.put("data", "fail");
            a.put("type", "insertMovie");
        }
        else {
            a.put("data", "success");
            a.put("type", "insertMovie");;
        }
        return a;
    }

    @GetMapping("/app/list")
    @ResponseBody
    public String getAppList(HttpServletRequest request) {
        List<Movie> movieList = movieService.getTopByRating(20);
        JsonArray jsonArray = new JsonArray();
        for (Movie i : movieList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("title", i.getTitle());
            jsonObject.addProperty("id", i.getId());
            jsonObject.addProperty("year", i.getYear());
            jsonObject.addProperty("director", i.getDirector());
            List<Star> stars = i.getActors();
            for (int j=0;j<stars.size();j++) {
                jsonObject.addProperty("star"+j,stars.get(j).getName());
            }
            List<Genre> genres = i.getGenres();
            for (int j=0;j<genres.size();j++) {
                jsonObject.addProperty("genre"+j,genres.get(j).getName());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    @GetMapping("/app/singlemovie")
    @ResponseBody
    public String getAppSingleMovie(HttpServletRequest request,@RequestParam(value="id", required = true) String id) {
        Movie movie = movieService.queryMovieById(id);
        List<Star> starsList = movieService.getMovieStars(id);
        Rating rate = movieService.getMovieRating(id);
        List<Genre> genresList = movieService.getMovieGenre(id);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", movie.getTitle());
        jsonObject.addProperty("id", movie.getId());
        jsonObject.addProperty("year", movie.getYear());
        jsonObject.addProperty("director", movie.getDirector());
        for (int j=0;j<starsList.size();j++) {
            jsonObject.addProperty("star"+j,starsList.get(j).getName());
        }
        for (int j=0;j<genresList.size();j++) {
            jsonObject.addProperty("genre"+j,genresList.get(j).getName());
        }
        return jsonObject.toString();
    }

}
