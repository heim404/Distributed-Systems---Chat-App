package dev.superman.server.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import dev.superman.server.loggerThread.LoggerThread;
import dev.superman.server.protocols.InputHandler;
import dev.superman.server.schema.UserProfile;
import dev.superman.server.schema.UserProfile.AccessLevel;

/**
 * TCPConnection class handles the TCP connection for a client.
 * It extends the Thread class to handle client communication in a separate thread.
 * 
 * This class manages user login, registration, and various commands such as joining rooms,
 * viewing profile, logging out, and sending notifications.
 * 
 * Fields:
 * - tcpHandler: Handles TCP-related operations.
 * - inputHandler: Handles input-related operations.
 * - clientSocket: The socket for the client connection.
 * - in: BufferedReader for reading input from the client.
 * - out: PrintWriter for sending output to the client.
 * - userProfile: Stores user profile information.
 * 
 * Constructor:
 * - TCPConnection(Socket clientSocket, TCPHandler tcpHandler): Initializes the connection with the client socket and TCP handler.
 * 
 * Methods:
 * - run(): Handles the main communication loop with the client, processing various commands.
 * - getUsername(): Returns the username of the connected user.
 */
public class TCPConnection extends Thread {
    private TCPHandler tcpHandler;
    private InputHandler inputHandler;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private UserProfile userProfile;
    private LoggerThread logger;

    public TCPConnection(Socket clientSocket, TCPHandler tcpHandler, LoggerThread logger) throws IOException {
        this.tcpHandler = tcpHandler;
        this.inputHandler = tcpHandler.getInputHandler();
        this.clientSocket = clientSocket;
        this.logger = logger;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    /**
     * Handles the TCP connection for a user. This method is executed when the thread is started.
     * It reads input from the user, processes commands, and sends responses back to the user.
     * 
     * Commands available before login:
     * - .login <username> <password>: Logs in the user with the provided credentials.
     * - .register <username> <password>: Registers a new user with the provided credentials.
     * - .help: Displays the available commands.
     * 
     * Commands available after login:
     * - .profile: Displays the user's profile information.
     * - .logout: Logs out the user.
     * - .join <convidado/baixo/medio/alto>: Joins the specified chat room.
     * - .help: Displays the available commands.
     * - .online: Displays the list of online users.
     * - .request <evac/comms/res>: Sends a request for evacuation, communication, or resources.
     * - .accept <evac/comms/res>: Accepts a request for evacuation, communication, or resources.
     * - .notify <message>: Sends a notification message to all groups.
     * 
     * The method handles user authentication, command parsing, and interaction with the input handler
     * and TCP handler to manage user sessions and chat rooms.
     */
    @Override
    public void run() {
        try {
            userProfile = new UserProfile(in.readLine());
            userProfile.setName(userProfile.getTemporaryName());
            userProfile.setLoggedIn(false);
            System.out.println("Connection established with " + userProfile.getName() + " on " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
            out.println("Welcome to the chat server " + userProfile.getName() + "\nPlease .login, .register or .help");
            String input;
            while ((input = in.readLine()) != null) {
                String[] parts = input.split(" ");
                String command = parts[0].toLowerCase();
                if (!userProfile.isLoggedIn()) {
                    switch (command) {
                        case ".login":
                            if (parts.length != 3) { //numero de argumentos incorreto
                                out.println("Invalid login, use .login <username> <password>");
                                break;
                            }
                            if (tcpHandler.isAlreadyLoggedIn(parts[1])) { //verifica se o user já está logado
                                out.println("User already logged in");
                                break;
                            }
                            String result;
                            if ((result = inputHandler.verifyLogin(parts[1], parts[2])) != null) { //verifica se o login é válido
                                userProfile.setName(parts[1]);                                      //se for, atualiza o nome do user, o nível de acesso
                                switch (result) {
                                    case "CONVIDADO":
                                        userProfile.setAccessLevel(AccessLevel.CONVIDADO);
                                        break;
                                    case "BAIXO":
                                        userProfile.setAccessLevel(AccessLevel.BAIXO);
                                        break;
                                    case "MEDIO":
                                        userProfile.setAccessLevel(AccessLevel.MEDIO);
                                        break;
                                    case "ALTO":
                                        userProfile.setAccessLevel(AccessLevel.ALTO);
                                        break;
                                }
                                userProfile.setLoggedIn(true);
                                userProfile.setCurrentRoom("CONVIDADO");//inicializa a sala atual como CONVIDADO
                                out.println("userinfo " + parts[1] + " Login_successfull._You_can_now_send_messages");
                                out.println("Use .help to see available commands");
                                String chat = inputHandler.joinRoom(userProfile, "CONVIDADO");
                                
                                out.println("--------- Joined room CONVIDADO -----------");
                                String[] chatArray = chat.split(":");
                                out.println("chat " + chatArray[0] + " " + chatArray[1]);
                                loadMessages("CONVIDADO"); //carrega as ultimas 5 mensagens da sala
                                logger.info(parts[1] + " Joined CONVIDADO" );
                                
                            } else {
                                out.println("Login inválido");
                            }
                            break;
                        case ".register":
                            if (parts.length != 3) {
                                out.println("Invalid register, use .register <username> <password>");
                                break;
                            }
                            out.println(inputHandler.registerUser(parts[1], parts[2]));
                            logger.info("New User Registered: " + parts[1]);
                            break;
                        case ".help":
                            if (parts.length != 1) {
                                out.println("Invalid command, use .help");
                                break;   
                            }
                            out.println("Commands:\n.login <username> <password>\n.register <username> <password>\n.help");
                            break;
                        default:
                            out.println("Please login or register");
                            break;
                    }
                } else {
                    switch (command) {
                        case ".profile":
                            if (parts.length != 1) {
                                out.println("Invalid command, use .profile");
                                break; 
                            }
                            out.println("--------- User Profile -----------");
                            out.println("Username: " + userProfile.getName() + "\nAccess Level: " + userProfile.getAccessLevel());
                            break;
                        case ".logout":
                            if (parts.length != 1) {
                                out.println("Invalid command, use .logout");
                                break; 
                            }
                            userProfile.setName(userProfile.getTemporaryName()); //restaura o nome temporário gerado quando o user se conectou
                            userProfile.setAccessLevel(null);
                            userProfile.setCurrentRoom(null);
                            out.println("userinfo " + userProfile.getName() + " Logout_successful.");
                            userProfile.setLoggedIn(false);
                            out.println("chat off");
                            break;
                        case ".join":
                            if (parts.length != 2) {
                                out.println("Invalid room, use .join <convidado/baixo/medio/alto>");
                                break;                    
                            }
                            if (userProfile.getCurrentRoom().equals(parts[1].toUpperCase())) { //verifica se o user já está na sala
                                out.println("You are already in room " + parts[1].toUpperCase());
                                break;
                            }
                            String result;
                            String room = parts[1].toUpperCase();
                            if ((result = inputHandler.joinRoom(userProfile, room)) != null) {
                                if (result.equals("DENY")) { //user não tem permissão para entrar na sala
                                    out.println("Access denied to room " + room);
                                    logger.warning(userProfile.getName() + " tried to access " + room + ". UserProfile: " + userProfile);
                                } else {
                                    out.println("--------- Joined " +room+" -----------");
                                    userProfile.setCurrentRoom(room);
                                    String[] resultArray = result.split(":");
                                    out.println("chat " + resultArray[0] + " " + resultArray[1]);
                                    loadMessages(room);
                                    logger.info(userProfile.getName() + " joined " + room);
                                }
                            } else {
                                out.println("Failed to join room " + room);
                            }
                            break;
                        case ".help":
                            if (parts.length != 1) {
                                out.println("Invalid command, use .help");
                                break; 
                            }
                            out.println("--------- HELP MENU -----------");
                            out.println("Commands available:\n.profile\n.logout\n.join <name>\n.help\n.online\n.request <evac/comms/res>\n.accept <evac/comms/res>\n.notify <message>");	
                            break;
                        case ".online":
                            if (parts.length != 1) {
                                out.println("Invalid command, use .online");
                                break; 
                            }
                            out.println("Users(" + tcpHandler.getUsersSize() + "): " + tcpHandler.getUsers());
                            break;
                        case ".request":
                            if (parts.length < 2) {
                                out.println("Invalid request, use .request <evac/comms/res>");
                                break;
                            }
                            String alertRequest = inputHandler.requestAlert(userProfile, parts[1]);
                            out.println(alertRequest);
                            if (alertRequest.equals("You dont have permission to request this alert")) logger.warning(userProfile.getName() + " tried to request an alert without perms");
                            else if (alertRequest.equals("Evacuation request sent, wait for someone to accept it")) logger.info(userProfile.getName() + " requested " + parts[1]);
                            break;
                        case ".accept":
                            if (parts.length < 2) {
                                out.println("Invalid request, use .accept <evac/comms/res>");
                                break;
                            }
                            String alertAccept = inputHandler.accept(userProfile, parts[1]);
                            out.println(alertAccept);
                            if (alertAccept.equals("You cannout accept your own request")) logger.warning(userProfile.getName() + " tried to accept is own request");
                            else if (alertAccept.equals("Alert ended")) logger.info(userProfile.getName() + " accepted " + parts[1]);
                            else if (alertAccept.equals("You dont have permission to accept this alert")) logger.warning(userProfile.getName() + " tried do accecpt a request without perms");
                            break; 
                        case ".notify":
                            if (parts.length < 2) {
                                out.println("Invalid notify, use .notify <message>");
                                break;
                            }
                            String message = "[NOTIFICATION - "+userProfile.getName()+"]: ";
                            for (int i = 1; i < parts.length; i++) {
                                message += parts[i] + " ";
                            }
                            out.println("Notification sent for all groups");
                            inputHandler.notify(message);
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Retrieves the username from the user profile.
     *
     * @return the username as a String
     */
    public String getUsername() {
        return userProfile.getName();
    }

    
    /**
     * Loads and prints the last 10 messages from the specified room.
     * If the room has fewer than 10 messages, all messages are printed.
     *
     * @param room the name of the room from which to load messages
     */
    public void loadMessages(String room) {
        List<String> lines = inputHandler.getMessages(room);
        if (lines != null && !lines.isEmpty()) {
            int startIndex = lines.size() > 5 ? lines.size() - 5 : 0;
            List<String> lastMessages = lines.subList(startIndex, lines.size());
        for (String line : lastMessages) 
            out.println(line);
    }
    }
}
