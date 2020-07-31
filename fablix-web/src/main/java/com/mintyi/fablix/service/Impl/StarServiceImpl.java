package com.mintyi.fablix.service.Impl;

import com.mintyi.fablix.dao.StarDao;
import com.mintyi.fablix.domain.Movie;
import com.mintyi.fablix.domain.Star;
import com.mintyi.fablix.service.StarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StarServiceImpl implements StarService {
    @Autowired
    private StarDao starDao;

    @Override
    public Star queryStarById(String id){return starDao.queryStarById(id);}

    @Override
    public List<Movie> getStarInMovie(String id){return starDao.getStarInMovie(id);}

    @Override
    public String getMaxId(){return starDao.getMaxId();};

    @Override
    public String insertStar(String name, Integer year){return starDao.insertStar(name, year);};
}
