package com.mintyi.fablix.service;

import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Star;

import java.util.List;

public interface StarService {
    public Star queryStarById(String id);
    public List<Movie> getStarInMovie(String id);
    public String getMaxId();
    public String insertStar(String name, Integer year);
}
