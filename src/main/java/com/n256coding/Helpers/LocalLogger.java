package com.n256coding.Helpers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LocalLogger {
    public Logger logger;
    private FileHandler fh;
    private static volatile LocalLogger localLogger = new LocalLogger(com.n256coding.Services.FileHandler.LOG_FILE_PATH);

    private LocalLogger(String loggerFileName) {
        File file = new File(loggerFileName);
        if(!file.exists()){
            try {
                FileUtils.touch(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fh = new FileHandler(loggerFileName, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger = Logger.getLogger("textAnalyserLogging");
        logger.addHandler(fh);
        logger.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
        fh.setFormatter(new SimpleFormatter());
        logger.setLevel(Level.ALL);
    }

    public static LocalLogger getInstance(){
        return localLogger;
    }
}
