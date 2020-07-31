package com.mintyi.parser.naive;

import com.mintyi.parser.DataInserter;
import com.mintyi.parser.entity.Star;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StarParser extends DefaultHandler implements DataInserter {
    protected JdbcTemplate jdbcTemplate;
    protected List<Star> myStar = new ArrayList<>();
    protected String tempVal;
    protected Star tempStar;
    protected BufferedWriter badFile;
    protected int successNum = 0, failNum = 0;

    public StarParser(JdbcTemplate j){
        super();
        jdbcTemplate = j;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            myStar.add(tempStar);
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            Pattern pattern = Pattern.compile("[0-9]{4}");
            if (tempVal.equals("") || !pattern.matcher(tempVal).matches()) tempStar.setBirthYear(null);
            else {
                tempStar.setBirthYear(Integer.parseInt(tempVal));
            }
        }
    }

    public void writeErr (Star s) {
        try {
            badFile.write(String.format("Bad RECORD: "+s.getName()+" "+s.getBirthYear() + "\n"));
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
        System.out.println("star size: " + myStar.size());
        initBadFile(badFileName);
        int i=0;
        Iterator<Star> it = myStar.iterator();
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate.getDataSource()).withProcedureName("insert_star");
        MapSqlParameterSource in = new MapSqlParameterSource();
        while (it.hasNext()) {
            Star si = it.next();
            try {
                in.addValue("insertname", si.getName()).addValue("insertyear", si.getBirthYear());
                Map<String, Object> out = simpleJdbcCall.execute(in);
                String insertId = (String) out.get("insertid");
                // System.out.println(insertId + " success - " + si.toString());
                successNum ++;
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                writeErr(si);
                failNum ++;
            }
            i++;
        }
        System.out.println("insert into star finish: " + successNum + " success, " + failNum + " fail.");
    }

    protected void initBadFile(String badFileName) {
        Path p = Paths.get(badFileName);
        try{
            badFile = Files.newBufferedWriter(p, Charset.forName("utf8"), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "StarParser{" +
                "myStar=" + myStar.stream().map(star -> {
                    return star.getName() + " " + star.getBirthYear() + "\n";
        }) + '}';
    }
}
