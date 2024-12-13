package dev.superman.server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

import dev.superman.ED.SynchronizedArrayList;
import dev.superman.server.loggerThread.LoggerThread;
import dev.superman.server.protocols.InputHandler;

/**
 * The UDPConnection class extends the Thread class and is responsible for managing
 * a UDP multicast connection. It handles sending and receiving messages over a 
 * multicast socket.
 * 
 * <p>This class includes inner classes for reading and writing messages, as well as 
 * managing the connection to the multicast group.</p>
 * 
 */
public class UDPConnection extends Thread {
    private static int BYTES = 1024;
    private static final Map<String, String> MULTICASTSOCKETS_INFO = Map.of(
        "CONVIDADO", "230.0.0.1:5000",
        "BAIXO", "230.0.0.1:5001",
        "MEDIO", "230.0.0.1:5002",
        "ALTO", "230.0.0.1:5003"
    );
    private String udpName;
    private String ip;
    private int port;
    private InputHandler inputHandler;
    private MulticastSocket multicastSocket;
    private SynchronizedArrayList<String> udpMessages;
    private ReaderThread readerThread;
    private WriterThread writerThread;
    private LoggerThread logger;

    /**
     * Establishes a UDP connection for a specified chat group.
     *
     * @param udpName The name of the UDP chat group.
     * @param inputHandler The handler for processing input messages.
     * @param logger The logger thread for logging messages.
     * 
     * @throws IOException If an I/O error occurs when creating the multicast socket or joining the group.
     */
    @SuppressWarnings("deprecation")
    public UDPConnection(String udpName, InputHandler inputHandler, LoggerThread logger) {
        this.udpName = udpName;
        String multicastSocketInfo = MULTICASTSOCKETS_INFO.get(udpName);
        String[] infoArray = multicastSocketInfo.split(":");
        this.ip = infoArray[0];
        this.port = Integer.valueOf(infoArray[1]);
        this.inputHandler = inputHandler;
        this.logger = logger;
        try {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(InetAddress.getByName(ip));
            udpMessages = new SynchronizedArrayList<>();
            readerThread = new ReaderThread();
            writerThread = new WriterThread();
            readerThread.start();
            writerThread.start();
            String message = "Chat group " + udpName + " started on " + ip + ":" + port;
            System.out.println(message);
            logger.info(message);
        } catch (IOException ignored) {
            logger.log("Server failed to create multicast group " + udpName);
        }
    }

    /**
     * Retrieves the name associated with the UDP connection.
     *
     * @return the name of the UDP connection.
     */
    public String getUdpName() {
        return udpName;
    }

    /**
     * Retrieves the list of UDP messages.
     *
     * @return a SynchronizedArrayList containing the UDP messages.
     */
    public SynchronizedArrayList<String> getUdpMessages() {
        return udpMessages;
    }

    /**
     * Retrieves information about multicast sockets.
     *
     * @return a map containing information about multicast sockets, where the key is the socket identifier and the value is the socket information.
     */
    public static Map<String, String> getMulticastsocketsInfo() {
        return MULTICASTSOCKETS_INFO;
    }

    /**
     * ReaderThread is a private inner class that extends Thread.
     * It continuously listens for incoming UDP packets on a multicast socket.
     * Upon receiving a packet, it converts the packet's data to a UTF-8 encoded string.
     * If the message does not start with "Sistema: ", it adds the message to the input handler.
     * This thread runs indefinitely, handling IOExceptions by continuing the loop.
     */
    private class ReaderThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] buffer = new byte[BYTES];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (!message.startsWith("Sistema: "))
                        inputHandler.addMessage(udpName, message + "\n");
                } catch (IOException e) {
                    logger.log("Multicast Socket Failed {" + MULTICASTSOCKETS_INFO.toString() + "} ! Error reciveing message on: " + udpName + "!");
                    continue;
                }
            }
        }
    }

    /**
     * WriterThread is a private inner class that extends Thread.
     * It continuously checks for messages in the udpMessages list.
     * If a message is found, it converts the message to a byte array,
     * creates a DatagramPacket, and sends it using the multicastSocket.
     * If an IOException occurs during packet creation or sending,
     * it catches the exception and continues the loop.
     */
    private class WriterThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!udpMessages.isEmpty()) {
                    String message = udpMessages.remove(0);
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = null;
                    try {
                        packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
                        multicastSocket.send(packet);
                    } catch (IOException e) {
                        logger.log("Multicast Socket Failed! {" + MULTICASTSOCKETS_INFO.toString() + "} Error sending message to: " + udpName + "!");
                        continue;
                    }
                }
            }
        }
    }
}
