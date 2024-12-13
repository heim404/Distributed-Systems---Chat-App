package dev.superman.server.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;

import dev.superman.ED.SynchronizedArrayList;
import dev.superman.server.loggerThread.LoggerThread;
import dev.superman.server.protocols.InputHandler;

/**
 * TCPHandler is a server-side class that handles incoming TCP connections.
 * It extends the Thread class to allow concurrent handling of multiple connections.
 * 
 * <p>This class initializes a server socket on a specified port and listens for incoming
 * connections. When a new connection is accepted, it creates a TCPConnection object,
 * adds it to a synchronized list of connections, and starts the connection in a new thread.</p>
 * 
 * <p>It also provides methods to get the input handler, the number of connected users,
 * a list of usernames of connected users, and to check if a user is already logged in.</p>
 * 
 * @see InputHandler
 * @see TCPConnection
 */
public class TCPHandler extends Thread {
    private InputHandler inputHandler;
    private SynchronizedArrayList<TCPConnection> tcpConnections;
    private static final int PORT = 7;
    private ServerSocket serverSocket;
    LoggerThread logger;

    public TCPHandler(InputHandler inputHandler, LoggerThread logger) {
        this.inputHandler = inputHandler;
        tcpConnections = new SynchronizedArrayList<>();
        this.logger = logger;
        try {
            serverSocket = new ServerSocket(PORT);
            String message = "Server started and listening on port " + PORT;
            System.out.println(message);
            logger.info(message);
        } catch (IOException e) {
            logger.log("Server failed to listen on port " + PORT);
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Continuously listens for incoming TCP connections and handles them.
     * Creates a new TCPConnection for each accepted connection and starts it.
     * Adds the new TCPConnection to the list of active connections.
     * 
     * This method runs indefinitely in a loop, accepting and handling new connections.
     * 
     * @throws IOException if an I/O error occurs when waiting for a connection.
     */
    @Override
    public void run() {
        while (true) {
            try {
                TCPConnection tcpConnection = new TCPConnection(serverSocket.accept(), this, logger);
                tcpConnections.add(tcpConnection);
                tcpConnection.start();
                logger.info("Creating new TCP Connection");
            } catch (IOException ignored) {
                logger.log("Failed Creating new TCP Connection");
            }
        }
    }

    /**
     * Retrieves the current instance of the InputHandler.
     *
     * @return the inputHandler instance.
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Returns the number of active TCP connections.
     *
     * @return the size of the tcpConnections list.
     */
    public int getUsersSize() {
        return tcpConnections.size();
    }

    /**
     * Retrieves a list of usernames from the current TCP connections.
     *
     * @return A JSON-like string representation of the usernames in the format: 
     *         ["username1","username2",...,"usernameN"]. The string will not have 
     *         a trailing comma.
     */
    public String getUsers() {
        String result = "[";
        Iterator<TCPConnection> it = tcpConnections.iterator();
        while (it.hasNext()) {
            result += it.next().getUsername() + ",";
        }
        return result.substring(0, result.length() - 1) + "]";
    }
    
    /**
     * Checks if a user with the given username is already logged in.
     *
     * @param username the username to check for
     * @return true if a user with the given username is already logged in, false otherwise
     */
    public boolean isAlreadyLoggedIn(String username) {
        Iterator<TCPConnection> it = tcpConnections.iterator();
        while (it.hasNext()) {
            if (it.next().getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
