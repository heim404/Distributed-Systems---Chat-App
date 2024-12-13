package dev.superman.client.Threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import dev.superman.ED.SynchronizedArrayList;
import dev.superman.client.schema.User;

/**
 * UDPHandlerThread is a class that extends the Thread class and is responsible for handling
 * UDP communication using a multicast socket. It manages two inner threads: ReaderThread and
 * WriterThread, which handle reading and writing UDP messages, respectively.
 * 
 * The class provides functionality to:
 * - Initialize a multicast socket and join a multicast group.
 * - Start the reader and writer threads.
 * - Close the UDP connection and clear the message queue.
 * 
 */
public class UDPHandlerThread extends Thread {
    private static int BYTES = 1024;
    private String address;
    private int port;
    private MulticastSocket multicastSocket;
    private ReaderThread readerThread;
    private WriterThread writerThread;
    private SynchronizedArrayList<String> udpMessages;
    private User user;
    private boolean running;

    @SuppressWarnings("deprecation")
    public UDPHandlerThread(User user, String address, int port, SynchronizedArrayList<String> udpMessages) throws IOException {
        this.address = address;
        this.port = port;
        multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(InetAddress.getByName(address));
        this.udpMessages = udpMessages;
        readerThread = new ReaderThread();
        writerThread = new WriterThread();
        this.user = user;
        running = true;
        readerThread.start();
        writerThread.start();
    }

    /**
     * Closes the UDP connection by stopping the running thread, clearing the message queue,
     * and closing the multicast socket if it is not null.
     */
    public void closeConnection() {
        running = false;
        udpMessages.clear();
        if (multicastSocket != null) multicastSocket.close();
    }

    /**
     * ReaderThread is a private inner class that extends the Thread class.
     * It is responsible for continuously reading data from a multicast socket
     * and printing the received data to the console.
     * 
     * The thread runs in a loop as long as the 'running' flag is true.
     * It creates a buffer to store the incoming data and a DatagramPacket
     * to receive the data from the multicast socket.
     * 
     * If an IOException occurs during the receive operation, the exception
     * is caught and the loop continues to attempt to receive data.
     */
    private class ReaderThread extends Thread {
        @Override
        public void run() {
            while (running) {
                try {
                    byte[] buffer = new byte[BYTES];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    System.out.println(new String(packet.getData()));
                } catch (IOException e) {
                    continue;
                }
            }
        }
    }

    /**
     * WriterThread is a private inner class that extends Thread. It is responsible for
     * sending UDP messages from the udpMessages queue to a specified multicast address
     * and port. The messages are prefixed with the user's name.
     * 
     * The run method continuously checks if the thread is running and if there are any
     * messages in the udpMessages queue. If there are messages, it constructs a message
     * by combining the user's name and the message, converts it to a byte array, and
     * sends it as a DatagramPacket to the specified multicast address and port.
     * 
     * If an IOException occurs while sending the packet, the exception is caught and
     * the loop continues.
     */
    private class WriterThread extends Thread {
        @Override
        public void run() {
            while (running) {
                if (!udpMessages.isEmpty()) {
                    String message = user.getName() + ": " + udpMessages.remove(0);
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = null;
                    try {
                        packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(address), port);
                        multicastSocket.send(packet);
                    } catch (IOException e) {
                        continue;
                    }
                }
            }
        }
    }
}
