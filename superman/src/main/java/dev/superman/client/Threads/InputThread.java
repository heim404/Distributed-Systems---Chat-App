package dev.superman.client.Threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dev.superman.ED.SynchronizedArrayList;

/**
 * The InputThread class extends the Thread class and is responsible for reading input from the standard input stream.
 * It categorizes the input into TCP or UDP messages based on the content of the input.
 * 
 * <p>If the input starts with a period ('.'), it is considered a TCP message and added to the tcpMessages list.
 * Otherwise, it is considered a UDP message and added to the udpMessages list.</p>
 * 
 * <p>This class continuously reads input in a loop until the input stream is closed or an IOException occurs.</p>
 */
public class InputThread extends Thread {
    private BufferedReader stdInput;
    private SynchronizedArrayList<String> tcpMessages;
    private SynchronizedArrayList<String> udpMessages;

    public InputThread(SynchronizedArrayList<String> tcpMessages, SynchronizedArrayList<String> udpMessages) {
        stdInput = new BufferedReader(new InputStreamReader(System.in));
        this.tcpMessages = tcpMessages;
        this.udpMessages = udpMessages;
    }

    /**
     * Continuously reads input from the standard input stream and categorizes it into TCP or UDP messages.
     * If the input starts with a period ('.'), it is added to the TCP messages list.
     * Otherwise, it is added to the UDP messages list.
     * 
     * This method runs in a loop until the input stream is closed or an IOException occurs.
     */
    @Override
    public void run() {
        String input;
        try {
            while ((input = stdInput.readLine()) != null) {
                if (input.startsWith(".")) tcpMessages.add(input);
                else udpMessages.add(input);
            }
        } catch (IOException ignored) {}
    }
}
