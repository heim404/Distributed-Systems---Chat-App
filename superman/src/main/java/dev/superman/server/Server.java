package dev.superman.server;

import java.util.Iterator;
import dev.superman.ED.SynchronizedArrayList;
import dev.superman.server.loggerThread.LoggerThread;
import dev.superman.server.protocols.InputHandler;
import dev.superman.server.protocols.Requests;
import dev.superman.server.tcp.TCPHandler;
import dev.superman.server.udp.UDPConnection;


/** 
 * The Server class contains the following inner class:
 * - PeriodicReport: A thread that generates periodic reports about the server's
 *   current state, including the number of connected users, a list of users,
 *   and the number of requests being processed. The report is broadcasted to
 *   all connected UDP clients.
 * 
 * Usage:
 * To start the server, simply create an instance of the Server class by calling
 * the main method.
 * 
 * Example:
 * public static void main(String[] args) {
 *     new Server();
 * }
 */
public class Server {
    private InputHandler inputHandler;
    private SynchronizedArrayList<UDPConnection> udpConnections;
    private Requests requests;
    private TCPHandler tcpHandler;
    private LoggerThread logger;

    /**
     * The Server class initializes and manages the server components including
     * request handling, input handling, UDP connections, logging, TCP handling,
     * and periodic reporting.
     * 
     * <p>This constructor performs the following actions:
     * <ul>
     *   <li>Initializes the Requests and InputHandler with the current server instance.</li>
     *   <li>Creates a synchronized list of UDP connections and starts a logger thread.</li>
     *   <li>Adds predefined UDP connections ("CONVIDADO", "BAIXO", "MEDIO", "ALTO") to the list and starts them.</li>
     *   <li>Initializes and starts the TCPHandler for handling TCP connections.</li>
     *   <li>Starts a periodic report thread for regular reporting.</li>
     * </ul>
     */
    public Server() {
        requests = new Requests(this);
        inputHandler = new InputHandler(requests, this);
        udpConnections = new SynchronizedArrayList<>();
        logger = new LoggerThread();
        logger.start();

        udpConnections.add(new UDPConnection("CONVIDADO", inputHandler, logger));
        udpConnections.add(new UDPConnection("BAIXO", inputHandler, logger));
        udpConnections.add(new UDPConnection("MEDIO", inputHandler, logger));
        udpConnections.add(new UDPConnection("ALTO", inputHandler, logger));

        Iterator<UDPConnection> udpConnectionsIterator = udpConnections.iterator();
        while (udpConnectionsIterator.hasNext())
            udpConnectionsIterator.next().start();
        tcpHandler = new TCPHandler(inputHandler, logger);
        tcpHandler.start();
        PeriodicReport periodicReport = new PeriodicReport();
        periodicReport.start();
    }

    public static void main(String[] args) {
        new Server();
    }

    /**
     * Broadcasts a message to all connected UDP clients.
     *
     * @param message The message to be broadcasted to all clients.
     */
    public void serverBroadcast(String message) {
        Iterator<UDPConnection> udpConnectionsIterator = udpConnections.iterator();
        while (udpConnectionsIterator.hasNext()){
                udpConnectionsIterator.next().getUdpMessages().add(message);
        }
    }

    /**
     * The PeriodicReport class extends the Thread class and is responsible for generating
     * periodic reports about the server's current state. It runs indefinitely in a loop,
     * sleeping for 60 seconds between each report generation.
     * 
     * The report includes:
     * - The number of users connected to the server.
     * - A list of users.
     * - The number of requests currently being processed.
     * 
     * The generated report is then broadcasted to the server.
     * 
     * If an exception occurs during the sleep or report generation, it is caught and
     * the stack trace is printed.
     */
    public class PeriodicReport extends Thread {
        public void run() {
            while (true) {
            try {
                Thread.sleep(60000); //a cada 60 segundos gera um relatório
                String result = "Sistema: -----------Server Data Report -----------\n";
                result += "Number of users: " + tcpHandler.getUsersSize() + "\n";
                result += "Users: "+ tcpHandler.getUsers() + "\n";
                result += "Number of requests at the moment: " + requests.getRequests().size() + "\n";
                result += "-------------------------------------------------";
                serverBroadcast(result);
                
        }catch (Exception e) {
            e.printStackTrace();
        }
        
    }}
}
}
