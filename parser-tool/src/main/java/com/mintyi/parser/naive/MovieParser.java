package com.mintyi.parser.naive;

import com.mintyi.parser.DataInserter;
import com.mintyi.parser.DomainRepo;
import com.mintyi.parser.entity.MovieInfo;
import org.springframework.dao.EmptyResultDataAccessException;
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
import java.util.regex.Pattern;


public class MovieParser extends DefaultHandler implements DataInserter {

    protected List<MovieInfo> myMovie = new ArrayList<>();
    protected String tempVal;
    protected String tempDir;
    protected MovieInfo tempMovie;
    protected HashMap<String, String> genTrans = DomainRepo.genTrans;
    protected JdbcTemplate jdbcTemplate;
    protected BufferedWriter badFile;
    protected String idPrefix;
    protected int currId, successNum = 0, failNum = 0;
    protected HashMap<String, String> genresId = new HashMap<>();


    public MovieParser (JdbcTemplate j) {
        jdbcTemplate = j;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new MovieInfo();
            tempMovie.setDirector(tempDir);
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("dirname")) {
            tempDir = tempVal;
        } else if (qName.equalsIgnoreCase("year")) {
            Pattern pattern = Pattern.compile("[0-9]{4}");
            if (tempVal.equals("") || !pattern.matcher(tempVal).matches()) tempMovie.setYear(null);
            else {
                tempMovie.setYear(Integer.parseInt(tempVal));
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            List<String> tmpG = tempMovie.getGenres();
            String[] split = tempVal.trim().split("\\W+");
            for (String v : split) {
                tmpG.add(v);
            }
            tempMovie.setGenres(tmpG);
        } else if (qName.equalsIgnoreCase("film")) {
            myMovie.add(tempMovie);
        } else if (qName.equalsIgnoreCase("t")) {
            tempVal = tempVal.equals("NKT")?null:tempVal;
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        }
    }

    public List<MovieInfo> getMovie () {
        return myMovie;
    }

    public void print () {
        System.out.println("#movies " + myMovie.size() + ".");
        for (int i =0;i<10;i++) {
            MovieInfo info = myMovie.get(i);
            int year = info.getYear()==null?0:info.getYear();
            String gs = "";
            List<String> genres = info.getGenres();
            if (genres != null) {
                for (String gi : genres) {
                    String rawG = gi.toLowerCase().replaceAll("[^a-zA-Z]","");
                    gs += genTrans.getOrDefault(rawG,gi)+" ";
                }
            }

            System.out.println(info.getId()+" "+ info.getTitle()+" "+year+" "+info.getDirector()+" | "+gs);
        }
    }

    public void writeErr (MovieInfo info) {
        try {
            badFile.write(String.format("Bad RECORD: "+" "+info.getId()+" "+info.getTitle()+" "+info.getYear()+" "+info.getDirector() + "\n"));
            badFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getGenId (String gen) {
        String rawGen = gen.toLowerCase().replaceAll("[^a-zA-Z]","");
        String genre = genTrans.getOrDefault(rawGen,gen);
        if (genresId.containsKey(genre)) return genresId.get(genre);
        else {
            String genId;
            try {
                genId = (String) jdbcTemplate.queryForObject("select id from genres where name like '"+genre+"'", String.class);
            } catch (EmptyResultDataAccessException e) {
                genId = null;
            }
            if (genId == null) {
                String sql = String.format("insert ignore into genres (name) values('%s')",genre);
                jdbcTemplate.update(sql);
                genId = (String) jdbcTemplate.queryForObject("select id from genres where name = '"+genre+"'", String.class);
            }
            genresId.put(genre, genId);
            return genId;
        }
    }

    @Override
    public DefaultHandler getHandler(){ return this; }

    protected void initBadFile(String badFileName) {
        Path p = Paths.get(badFileName);
        try{
            badFile = Files.newBufferedWriter(p, Charset.forName("utf8"), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void insert(String badFileName) {
        System.out.println("movie size: " + myMovie.size());
        initBadFile(badFileName);
        initId();
        int i=0;

        Iterator<MovieInfo> it = myMovie.iterator();
        while (it.hasNext()) {
            MovieInfo mi = it.next();
            try {
                if ( mi.getTitle()==null || mi.getYear()==null || mi.getDirector()==null) {
                    writeErr(mi);
                    failNum ++;
                    continue;
                }
                if (mi.getId()==null) {
                    String s = jdbcTemplate.queryForObject("select max(id) from movies", String.class);
                    mi.setId(idPrefix + currId);
                    currId ++;
                }
                String sql = String.format("insert ignore into movies value (\"%s\", \"%s\", %d, \"%s\")",mi.getId(), mi.getTitle(), mi.getYear(), mi.getDirector());
                jdbcTemplate.update(sql);
                List<String> genList = mi.getGenres();
                for (String gi : genList) {
                    String genId = getGenId(gi);
                    sql = String.format("insert ignore into genres_in_movies value ('%s', '%s')", genId, mi.getId());
                    jdbcTemplate.update(sql);
                }
                successNum ++;
            } catch (Exception e) {
                writeErr(mi);
                System.out.println(e.getMessage());
                failNum ++;
            }
            i++;
        }
        System.out.println("insert into movie finish: " + successNum + " success, " + failNum + " fail.");
    }

    protected void initId() {
        String s = jdbcTemplate.queryForObject("select max(id) from movies", String.class);
        idPrefix = s.replaceAll("\\d+", "");
        currId = Integer.parseInt(s.replaceAll("\\D+", "")) + 1;
        System.out.println("maxId: " + s);
    }
}
