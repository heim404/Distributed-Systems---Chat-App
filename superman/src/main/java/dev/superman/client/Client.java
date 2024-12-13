package dev.superman.client;

import java.io.IOException;
import java.net.UnknownHostException;

import dev.superman.ED.SynchronizedArrayList;
import dev.superman.client.Threads.InputThread;
import dev.superman.client.Threads.TCPHandlerThread;
import dev.superman.client.schema.User;

/**
 * The Client class initializes and starts the necessary threads for handling
 * TCP and UDP messages for a user. It creates instances of User, TCPHandlerThread,
 * SynchronizedArrayList for UDP messages, and InputThread.
 * 
 * <p>The Client class performs the following actions:
 * <ul>
 *   <li>Initializes a User object.</li>
 *   <li>Initializes a SynchronizedArrayList to store UDP messages.</li>
 *   <li>Creates and starts a TCPHandlerThread to handle TCP messages.</li>
 *   <li>Creates and starts an InputThread to handle input and UDP messages.</li>
 * </ul>
 * 
 * <p>The main method creates an instance of Client and handles any exceptions
 * that may occur during the initialization process.
 * 
 * @throws UnknownHostException if the IP address of the host could not be determined.
 * @throws IOException if an I/O error occurs when creating the Client.
 */
public class Client {
    private User user;
    private TCPHandlerThread tcpHandlerThread;
    private SynchronizedArrayList<String> udpMessages;
    private InputThread inputThread;

    public Client() throws UnknownHostException, IOException {
        user = new User();
        udpMessages = new SynchronizedArrayList<>();
        tcpHandlerThread = new TCPHandlerThread(user, udpMessages);
        inputThread = new InputThread(tcpHandlerThread.getTcpMessages(), udpMessages);
        tcpHandlerThread.start();
        inputThread.start();
    }

    public static void main(String[] args) {
        try {
            new Client();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
