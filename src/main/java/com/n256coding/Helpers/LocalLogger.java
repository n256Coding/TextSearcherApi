package com.n256coding.Helpers;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LocalLogger {
    private Logger logger;
    private FileHandler fh;
    private static volatile LocalLogger localLogger = new LocalLogger(com.n256coding.Services.FileHandler.LOG_FILE_PATH);

    private LocalLogger(String loggerFileName) {
        File file = new File(loggerFileName);
        if(!file.exists()){
            try {
                file.createNewFile();
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
        fh.setFormatter(new SimpleFormatter());
        logger.setLevel(Level.ALL);
    }

    public static LocalLogger getInstance(){
        return localLogger;
    }
}
