package com.mintyi.fablix.controller.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
@Aspect
public class PerformanceAspect {
    final static String logPath = "log/";
    final static String logName = "performance.txt";
    BufferedWriter logFile;
    public PerformanceAspect() {
        Path p = Paths.get(logPath, logName);
        try{
            logFile = Files.newBufferedWriter(p, Charset.forName("utf8"), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Pointcut("execution(public * com.mintyi.fablix.dao.Impl.*.*(..))")
    private void daoOps(){}

//    @Pointcut("execution(public void javax.servlet.http.HttpServlet.service(..))")
//    private void httpServletService() {}
    @Pointcut("execution(* com.mintyi.fablix.controller.MovieController.search*(..))")
    private void searchMethod() {}


    @Around("searchMethod()")
    public Object logTS(ProceedingJoinPoint pjp) throws Throwable {
        return writeToLog(pjp, "TS");
    }

    @Around("daoOps()")
    public Object logTJ(ProceedingJoinPoint pjp) throws Throwable {
        return writeToLog(pjp, "TJ");
    }


    private Object writeToLog(ProceedingJoinPoint pjp, String prefix) throws Throwable {
        long startTime = System.currentTimeMillis();
        String name = "-";
        String result = "Y";
        try {
            name = pjp.getSignature().toShortString();
            return pjp.proceed();
        } catch (Throwable t) {
            result = "N";
            throw t;
        } finally {
            long endTime = System.currentTimeMillis();
            try {
                synchronized (logFile) {
                    logFile.write(String.format("%s;%s;%s;%dms\n", prefix, name, result, endTime - startTime));
                    logFile.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
