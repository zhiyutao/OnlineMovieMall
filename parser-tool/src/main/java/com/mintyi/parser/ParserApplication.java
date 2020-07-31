package com.mintyi.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ParserApplication implements CommandLineRunner {

    final static String resourcePath = "stanford-movies/";
    final static String logPath = "log/";
    final static String actor = "actors63.xml", mains = "mains243.xml", cast = "casts124.xml";
    final static String bActor = "badActor.txt", bMovie = "badMain.txt", bCast="badCast.txt";

    public static void main(String[] args) {
        SpringApplication.run(ParserApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        long start = System.currentTimeMillis();
        if(args.length == 0 || args[0].equals("optim2")) {
            runOptim2();
        }
        else if(args[0].equals("optim1")) {
            runOptim1();
        }
        else if(args[0].equals("naive")){
            runNaiveVersion();
        }

        long end = System.currentTimeMillis();
        System.out.println(end - start + " ms");
        System.exit(0);
    }


    @Autowired
    DataInserter opt2StarInserter;
    @Autowired
    DataInserter opt2MovieInserter;
    @Autowired
    DataInserter opt2CastInserter;
    @Bean
    @Autowired
    public DataInserter opt2StarInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.optim2.StarParser(jdbcTemplate, 10000);
    }
    @Bean
    @Autowired
    public DataInserter opt2MovieInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.optim2.MovieParser(jdbcTemplate, 10000);
    }
    @Bean
    @Autowired
    public DataInserter opt2CastInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.optim2.CastParser(jdbcTemplate, 10000);
    }
    public void runOptim2() {
        opt2StarInserter.parseXML(resourcePath + actor);
        opt2StarInserter.insert(logPath + bActor); // 570ms
        opt2MovieInserter.parseXML(resourcePath+mains); // 1316 ms
        opt2MovieInserter.insert(logPath + bMovie);

        opt2CastInserter.parseXML(resourcePath + cast);
        opt2CastInserter.insert(logPath + bCast); // 312259 ms
    }

    @Autowired
    DataInserter opt1StarInserter;
    @Autowired
    DataInserter opt1MovieInserter;
    @Autowired
    DataInserter opt1CastInserter;
    @Bean
    @Autowired
    public DataInserter opt1StarInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.optim1.StarParser(jdbcTemplate, 10000);
    }
    @Bean
    @Autowired
    public DataInserter opt1MovieInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.optim1.MovieParser(jdbcTemplate, 10000);
    }
    @Bean
    @Autowired
    public DataInserter opt1CastInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.optim1.CastParser(jdbcTemplate, 10000);
    }
    public void runOptim1() {
        opt1StarInserter.parseXML(resourcePath + actor);
        opt1StarInserter.insert(logPath + bActor); // 552ms
        opt1MovieInserter.parseXML(resourcePath+mains); // 1422 ms
        opt1MovieInserter.insert(logPath + bMovie);
//        insert into stars_in_movies finish: 48802 success, 136 fail.
//        360343 ms
        opt1CastInserter.parseXML(resourcePath + cast);
        opt1CastInserter.insert(logPath + bCast);
    }


    @Autowired
    DataInserter naiveStarInserter;
    @Autowired
    DataInserter naiveMovieInserter;
    @Autowired
    DataInserter naiveCastInserter;

    @Bean
    @Autowired
    public DataInserter naiveStarInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.naive.StarParser(jdbcTemplate);
    }

    @Bean
    @Autowired
    public DataInserter naiveMovieInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.naive.MovieParser(jdbcTemplate);
    }

    @Bean
    @Autowired
    public DataInserter naiveCastInserter(JdbcTemplate jdbcTemplate) {
        return new com.mintyi.parser.naive.CastParser(jdbcTemplate);
    }
    public void runNaiveVersion() {
        /*
        insert into star finish: 6863 success, 0 fail. 23831 ms
         */
        naiveStarInserter.parseXML(resourcePath + actor);
        naiveStarInserter.insert(logPath + bActor);
        naiveMovieInserter.parseXML(resourcePath+mains);
        naiveMovieInserter.insert(logPath + bMovie);
        naiveCastInserter.parseXML(resourcePath + cast);
        naiveCastInserter.insert(logPath + bCast);
    }

}
