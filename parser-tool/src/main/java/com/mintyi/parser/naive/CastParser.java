package com.mintyi.parser.naive;

import com.mintyi.parser.DataInserter;
import com.mintyi.parser.entity.Cast;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CastParser extends DefaultHandler implements DataInserter {
    protected JdbcTemplate jdbcTemplate;
    protected List<Cast> myCast = new ArrayList<>();
    protected String tempVal;
    protected Cast tempCast;
    protected BufferedWriter badFile;
    protected int failNum = 0, successNum = 0;
    protected String starIdPrefix;
    protected int starCurrId;
    protected HashMap<String, String> starName2Id = new HashMap<>();

    public CastParser(JdbcTemplate j) {
        jdbcTemplate = j;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            tempCast = new Cast();
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("m")) {
            myCast.add(tempCast);
        } else if (qName.equalsIgnoreCase("f")) {
            tempCast.setMovieId(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
            if (tempVal.matches("s\\s*a")) tempCast.setStarName(null);
            else {
                tempCast.setStarName(tempVal);
            }
        }
    }

    public void writeErr(Cast c) {
        try {
            badFile.write(String.format("Bad RECORD: " + c.getStarName() + " " + c.getMovieId() + "\n"));
            badFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DefaultHandler getHandler() {
        return this;
    }

    @Override
    public void insert(String badFileName) {
        System.out.println("cast size: " + myCast.size());
        initBadFile(badFileName);
        initId();
        filterCast();
        int i = 0;
        Iterator<Cast> it = myCast.iterator();
        while (it.hasNext()) {
            Cast ci = it.next();
            try {
                System.out.println(ci.getStarName());
                String sql;
                String starId;
                if (starName2Id.containsKey(ci.getStarName())){
                    starId = starName2Id.get(ci.getStarName());
                } else {
                    try {
                        List<String> r = jdbcTemplate.queryForList("select id from stars where name = ?", new Object[]{ci.getStarName()}, String.class);
                        if(r.size() > 0)
                            starId = r.get(0);
                        else {
                            starId = (starIdPrefix + starCurrId);
                            jdbcTemplate.update("INSERT INTO stars (id, name) VALUES(?,?)", starId, ci.getStarName());
                            starCurrId ++;
                        }
                        starName2Id.put(ci.getStarName(), starId);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        writeErr(ci);
                        continue;
                    }
                }


                System.out.println(starId + " " + ci.getMovieId());
                sql = String.format("insert ignore into stars_in_movies values (\"%s\", \"%s\")", starId, ci.getMovieId());
                jdbcTemplate.update(sql);
                successNum++;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                writeErr(ci);
                failNum++;
            }
            i++;
        }
        System.out.println("insert into stars_in_movies finish: " + successNum + " success, " + failNum + " fail.");
    }

    protected void initBadFile(String badFileName) {
        Path p = Paths.get(badFileName);
        try{
            badFile = Files.newBufferedWriter(p, Charset.forName("utf8"), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void initId() {
        String s = jdbcTemplate.queryForObject("select max(id) from stars", String.class);
        starIdPrefix = s.substring(0, 2);
        starCurrId = Integer.parseInt(s.substring(2)) + 1;
        System.out.println("maxId: " + s);
    }

    protected void filterCast() {
        myCast = myCast.stream().filter(cast -> {
            if(cast.getStarName() == null || cast.getMovieId() == null){
                failNum ++; writeErr(cast);
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }
}