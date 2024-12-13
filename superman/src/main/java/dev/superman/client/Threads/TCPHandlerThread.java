package dev.superman.client.Threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import dev.superman.ED.SynchronizedArrayList;
import dev.superman.client.schema.User;

/**
 * TCPHandlerThread is a class that extends the Thread class and handles TCP communication.
 * It manages a client socket connection, reads incoming messages, and sends outgoing messages.
 * 
 * <p>Fields:</p>
 * <ul>
 *   <li>ADDRESS: The IP address of the server to connect to.</li>
 *   <li>PORT: The port number of the server to connect to.</li>
 *   <li>clientSocket: The socket used for the client connection.</li>
 *   <li>readerThread: The thread responsible for reading messages from the server.</li>
 *   <li>writerThread: The thread responsible for sending messages to the server.</li>
 *   <li>tcpMessages: A synchronized list of TCP messages to be sent.</li>
 *   <li>udpMessages: A synchronized list of UDP messages received.</li>
 *   <li>udpHandlerThread: The thread responsible for handling UDP communication.</li>
 *   <li>user: The user associated with this TCP connection.</li>
 * </ul>
 * 
 * <p>Constructor:</p>
 * <ul>
 *   <li>TCPHandlerThread(User user, SynchronizedArrayList<String> udpMessages): Initializes the client socket, 
 *       reader and writer threads, and starts the threads.</li>
 * </ul>
 * 
 */
public class TCPHandlerThread extends Thread {
    private static String ADDRESS = "127.0.0.1";
    private static int PORT = 7;
    private Socket clientSocket;
    private ReaderThread readerThread;
    private WriterThread writerThread;
    private SynchronizedArrayList<String> tcpMessages;
    private SynchronizedArrayList<String> udpMessages;
    private UDPHandlerThread udpHandlerThread;
    private User user;

    public TCPHandlerThread(User user, SynchronizedArrayList<String> udpMessages) throws UnknownHostException, IOException {
        clientSocket = new Socket(ADDRESS, PORT);
        readerThread = new ReaderThread();
        writerThread = new WriterThread();
        tcpMessages = new SynchronizedArrayList<>();
        this.udpMessages = udpMessages;
        udpHandlerThread = null;
        this.user = user;
        tcpMessages.add(user.getTemporaryName());
        readerThread.start();
        writerThread.start();
    }

    /**
     * Retrieves the list of TCP messages.
     *
     * @return a SynchronizedArrayList containing the TCP messages.
     */
    public SynchronizedArrayList<String> getTcpMessages() {
        return tcpMessages;
    }

    /**
     * ReaderThread is a private inner class that extends Thread.
     * It is responsible for reading input from a BufferedReader connected to a client socket.
     * The thread processes different types of input messages and performs corresponding actions.
     * 
     * Constructor:
     * - ReaderThread(): Initializes the BufferedReader to read from the client socket's input stream.
     * 
     * Methods:
     * - run(): The main execution method of the thread. It continuously reads input lines from the BufferedReader.
     *   - If the input starts with "userinfo", it updates the user's name and prints a welcome message.
     *   - If the input starts with "chat", it manages the UDPHandlerThread for chat communication.
     *   - For any other input, it simply prints the input.
     * 
     * Exception Handling:
     * - IOException: Any IOException encountered during reading is ignored.
     */
    private class ReaderThread extends Thread {
        private BufferedReader in;

        public ReaderThread() throws IOException {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        @Override
        public void run() {
            String input;
            try {
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("userinfo")) {
                        String[] inputArray = input.split(" ");
                        user.setName(inputArray[1]);
                        if (inputArray[2].equals("Welcome_to_the_chat_server_" + user.getName())) user.setTemporaryName(inputArray[1]);//guarda o nome temporário Guest-XXX
                        System.out.println(inputArray[2].replaceAll("_", " "));
                    } else if (input.startsWith("chat")) {
                        String[] inputArray = input.split(" ");
                        if (udpHandlerThread != null) udpHandlerThread.closeConnection();
                        if (!inputArray[1].equals("off")) 
                            udpHandlerThread = new UDPHandlerThread(user, inputArray[1], Integer.valueOf(inputArray[2]), udpMessages);
                    } else {
                        System.out.println(input);
                    }
                }
            } catch (IOException ignored) {}
        }
    }

    /**
     * WriterThread is a private inner class that extends the Thread class.
     * It is responsible for sending messages from the tcpMessages queue to the client socket's output stream.
     * 
     * <p>It continuously checks if there are any messages in the tcpMessages queue, and if so, 
     * it removes the first message from the arraylist and sends it to the client socket.</p>
     * 
     * <p>This class uses a PrintWriter to write messages to the client socket's output stream.</p>
     * 
     * @throws IOException if an I/O error occurs when creating the PrintWriter.
     */
    private class WriterThread extends Thread {
        private PrintWriter out;

        public WriterThread() throws IOException {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }

        @Override
        public void run() {
            while (true) {
                if (!tcpMessages.isEmpty()) {
                    out.println(tcpMessages.remove(0));
                }
            }
        }
    }
}
