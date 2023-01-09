package edu.yu.cs.com3800;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public interface LoggingServer {

    //It seems the first initializeLogger, is meant to either initialize a logger with certain name,
    // or you can also pass a Boolean to use the second signature of the method
    // in order to disable the printing of logger to console (hence the name disableParentHandlers)
    default Logger initializeLogging(String fileNamePreface) throws IOException {
        return initializeLogging(fileNamePreface,false);
    }
    //The second method is meant to actually initialize the rest of the logger construction hall else to my understanding.
    default Logger initializeLogging(String fileNamePreface, boolean disableParentHandlers) throws IOException {
        //.....
        String loggerName = "myLogger";
        return createLogger(fileNamePreface,fileNamePreface,disableParentHandlers);
    }

    static Logger createLogger(String loggerName, String fileNamePreface, boolean disableParentHandlers) throws IOException {
        //...........
        final Logger log = Logger.getLogger(loggerName);
        String path = "C:/stage5/";
        FileHandler fh;
        SimpleDateFormat formater= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        //Path p = Paths.get("src/test/java/edu/yu/cs/com3800/stage4/logFiles" + formater.format(date),fileNamePreface + ".log");
        final Path p = Paths.get("src/test/java/edu/yu/cs/com3800/stage5/logFiles",fileNamePreface + ".log");

        try {
            // This block configure the logger with handler and formatter
            if (!Files.exists(p.getParent())) {
                Files.createDirectory(p.getParent());
                Files.createFile(p);
            }
            fh = new FileHandler(p.toString(),0,1, true);
            log.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            log.setUseParentHandlers(!disableParentHandlers);
            log.setLevel(Level.FINE);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        return log;
    }
}
