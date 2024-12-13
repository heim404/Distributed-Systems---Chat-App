package dev.superman.server.loggerThread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * LoggerThread is a custom thread that handles logging messages to a file.
 * It uses a BlockingQueue to manage log messages and ensures that log messages
 * are written to the log file in a thread-safe manner.
 * 
 * <p>This class supports logging messages with different severity levels:
 * SEVERE, INFO, and WARNING. It also creates the log file if it does not exist.
 * 
 * <p>Usage:
 * <pre>
 * LoggerThread loggerThread = new LoggerThread();
 * loggerThread.start();
 * loggerThread.log("This is a severe message");
 * loggerThread.info("This is an info message");
 * loggerThread.warning("This is a warning message");
 * </pre>
 * 
 * <p>Note: The log file is created in the "db/Logs/" directory with the name "log".
 * 
 * @see java.util.concurrent.BlockingQueue
 * @see java.util.logging.Logger
 * @see java.util.logging.Level
 * @see java.util.logging.FileHandler
 * @see java.util.logging.SimpleFormatter
 */
public class LoggerThread extends Thread {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final Logger logger = Logger.getLogger(LoggerThread.class.getName());

    private final String LOG_FILE_PATH;

    public LoggerThread() {
        LOG_FILE_PATH = "db/Logs/log";
    }

    public void log(String message) {
        logQueue.add(message + "&" + Level.SEVERE);
    }

    public void info(String message) {
        logQueue.add(message+ "&" + Level.INFO);
    }

    public void warning(String message) {
        logQueue.add(message+ "&" + Level.WARNING);
    }

    /**
     * Creates a log file at the specified path if it does not already exist.
     * Ensures that the necessary directories are created.
     * Adds a file handler to the logger with a simple formatter.
     *
     * @throws IOException if an I/O error occurs
     */

    public void createLogFile() throws IOException {
        Path logFilePath = Paths.get(LOG_FILE_PATH);
        Files.createDirectories(logFilePath.getParent());
        if (!Files.exists(logFilePath)) {
            Files.createFile(logFilePath);
        }
        FileHandler fileHandler = new FileHandler(logFilePath.toString(), true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
    }

    /**
     * The run method is the entry point for the LoggerThread.
     * It continuously takes log messages from the logQueue, splits the message to extract the log level and message content,
     * and logs the message using the logger.
     * 
     * The method handles IOException and InterruptedException by logging the error at SEVERE level and interrupting the thread.
     * 
     * @throws IOException if an I/O error occurs while creating the log file.
     * @throws InterruptedException if the thread is interrupted while waiting to take a message from the logQueue.
     */
    @Override
    public void run() {
        try {
            createLogFile();
            while (true) {
                String message = logQueue.take();
                String[] parts = message.split("&");
                logger.log(Level.parse(parts[1]), parts[0]);
                
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Logging error", e);
            Thread.currentThread().interrupt();
        }
    }
}