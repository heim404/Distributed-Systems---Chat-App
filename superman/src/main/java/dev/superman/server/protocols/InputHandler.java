package dev.superman.server.protocols;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCrypt;

import dev.superman.server.schema.UserProfile;
import dev.superman.server.schema.UserProfile.AccessLevel;
import dev.superman.server.udp.UDPConnection;
import dev.superman.server.*;
import dev.superman.server.protocols.Requests.RequestType;

/**
 * The InputHandler class is responsible for handling various input-related operations
 * such as user login verification, user registration, room joining, message handling,
 * and request processing. It interacts with the user database, chat room files, and
 * server requests to provide the necessary functionality.
 * 
 * <p>Key functionalities include:</p>
 * <ul>
 *   <li>Verifying user login credentials</li>
 *   <li>Registering new users</li>
 *   <li>Retrieving room names based on addresses</li>
 *   <li>Allowing users to join rooms based on their access levels</li>
 *   <li>Retrieving and adding messages to chat rooms</li>
 *   <li>Printing and accepting server requests</li>
 *   <li>Handling alert requests based on user permissions</li>
 * </ul>
 * 
 * <p>Private helper methods are used to check user existence, create files, and determine
 * access permissions for rooms and requests.</p>
 * 
 * <p>Dependencies:</p>
 * <ul>
 *   <li>Requests: Handles server requests</li>
 *   <li>Server: Manages server broadcasting</li>
 *   <li>BCrypt: Used for password hashing and verification</li>
 *   <li>UDPConnection: Provides multicast socket information</li>
 * </ul>
 * 
 * <p>Note: The class assumes the existence of certain directories and files for user and chat
 * room data. It creates these files if they do not exist.</p>
 */
public class InputHandler {
    private static final String DB = "db/";
    private static final String USERDB = DB + "users.txt";
    private static final String CHATSDBFOLDER = DB + "chats/";
    private Requests requests;
    private Server server;

    public InputHandler(Requests requests, Server server) {
        this.requests = requests;
        this.server = server;
        createFile(USERDB);
        createFile(CHATSDBFOLDER + "Alto.txt");
        createFile(CHATSDBFOLDER + "Medio.txt");
        createFile(CHATSDBFOLDER + "Baixo.txt");
        createFile(CHATSDBFOLDER + "Convidado.txt");
    }

    /**
     * Verifies the login credentials of a user by checking the provided username and password
     * against the stored user database.
     *
     * @param username the username provided by the user
     * @param password the password provided by the user
     * @return the user role if the credentials are valid, or null if the credentials are invalid
     */
    public String verifyLogin(String username, String password) {

        try (BufferedReader fileReader = Files.newBufferedReader(Paths.get(USERDB))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(username) && BCrypt.checkpw(password, parts[1])) 
                    return parts[2];
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a new user with the given username and password.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @return a message indicating whether the registration was successful or if the username already exists
     */
    public String registerUser(String username, String password) {
        if (userExists(username)) {
            return "Username already exists. Please choose another one.";
        }
        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String userEntry = username + "," + hashedPassword + "," + AccessLevel.CONVIDADO + "\n";
            Files.write(Paths.get(USERDB), userEntry.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Registration successful. You can now log in.";
    }

    /**
     * Retrieves the room name associated with a given address.
     *
     * @param address the address to look up
     * @return the room name associated with the given address, or an empty string if no match is found
     */
    public String getRoomName(String address) {
        for (Map.Entry<String,String> entry : UDPConnection.getMulticastsocketsInfo().entrySet()) {
            if (entry.getValue().equals(address)) return entry.getKey();
        }
        return "";
    }


    /**
     * Attempts to join a user to a specified room based on their access level.
     *
     * @param userProfile The profile of the user attempting to join the room.
     * @param room The name of the room the user is attempting to join.
     * @return The multicast socket information of the room if the user is allowed to enter,
     *         otherwise a message indicating that the user does not have permission or the room does not exist.
     */
    public String joinRoom(UserProfile userProfile, String room) {
        boolean result;
        switch (userProfile.getAccessLevel()) {
            case AccessLevel.ALTO:
                result = isAllowedToEnter(new AccessLevel[]{AccessLevel.ALTO, AccessLevel.MEDIO, AccessLevel.BAIXO, AccessLevel.CONVIDADO}, room);
                break;
            case AccessLevel.MEDIO:
                result = isAllowedToEnter(new AccessLevel[]{AccessLevel.MEDIO, AccessLevel.BAIXO, AccessLevel.CONVIDADO}, room);
                break;
            case AccessLevel.BAIXO:
                result = isAllowedToEnter(new AccessLevel[]{AccessLevel.BAIXO, AccessLevel.CONVIDADO}, room);
                break;
            default:
                result = isAllowedToEnter(new AccessLevel[]{AccessLevel.CONVIDADO}, room);
                break;
        }
        if (result) return UDPConnection.getMulticastsocketsInfo().get(room);
        
        else return "DENY";
    }


    /**
     * Retrieves the messages from the specified chat room.
     * 
     * @param roomName the name of the chat room
     * @return a list of messages from the chat room
     */
    public List<String> getMessages(String roomName) {
        try {
            return Files.readAllLines(Paths.get(CHATSDBFOLDER + roomName + ".txt"));
        } catch (IOException ignored) {}
        return new ArrayList<>();
    }



    /**
     * Adds a message to the specified chat room's log file.
     * If the file does not exist, it will be created.
     * 
     * @param roomName the name of the chat room
     * @param message the message to be added to the chat room's log file
     */
    public void addMessage(String roomName, String message) {
            try {
                Files.write(Paths.get(CHATSDBFOLDER + roomName + ".txt"), message.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException ignored) {}
        }
        
    

    /**
     * Prints the requests.
     *
     * @return A string representation of the printed requests.
     */
    public String printRequests() {
        return requests.printRequests(); 
    }

    /**
     * Accepts a request from the specified user profile and request type.
     *
     * @param userProfile the user profile making the request
     * @param requestType the type of request being made
     * @return a string indicating the result of the request removal
     */
    public String accept(UserProfile userProfile, String requestType) {
            return requests.removeRequest(userProfile, requestType);
    }

    /**
     * Notifies the server to broadcast a given message.
     *
     * @param message the message to be broadcasted by the server
     */
    public void notify(String message){
        server.serverBroadcast(message);
    }    

    /**
     * Handles the request for an alert based on the user profile and request type.
     *
     * @param userProfile the profile of the user making the request
     * @param requestType the type of request being made (e.g., "EVAC", "COMMS", "RES")
     * @return a message indicating the result of the request:
     *         - "Invalid request type" if the request type is not recognized
     *         - "You don't have permission to request this alert" if the user lacks the necessary permissions
     *         - "Evacuation request sent, wait for someone to accept it" if the request is successfully processed
     */
    public String requestAlert(UserProfile userProfile, String requestType) {
        Requests.RequestType type = null;
        switch (requestType.toString().toUpperCase()) {
            case "EVAC":
                type = Requests.RequestType.EVACUATION;
                break;
            case "COMMS":
                type = Requests.RequestType.COMMUNICATION;
                break;
            case "RES":    
                type = Requests.RequestType.RESOURCES;
                break;
            default:
                type = null;
                return "Invalid request type";    
        }
        if (type == null) {
            return "Invalid request type";   
        }
        if (!permissionToRequest(userProfile, type)) return "You dont have permission to request this alert";
        else requests.addRequest(type, userProfile);return type + " request sent, wait for someone to accept it";
}

    /**
     * Checks if a user exists in the database.
     *
     * @param username the username to check for existence
     * @return true if the user exists, false otherwise
     */
    private boolean userExists(String username) {
        try (BufferedReader fileReader = Files.newBufferedReader(Paths.get(USERDB))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Determines if the user has permission to make a specific type of request.
     *
     * @param userProfile the profile of the user making the request
     * @param requestType the type of request being made
     * @return true if the user has permission to make the request, false otherwise
     */
    private boolean permissionToRequest(UserProfile userProfile, Requests.RequestType requestType) {
        switch (requestType) {
            case EVACUATION:
                if(userProfile.getAccessLevel().equals(AccessLevel.ALTO) || userProfile.getAccessLevel().equals(AccessLevel.MEDIO))
                    return true;
                break;   
            case COMMUNICATION:
                if (userProfile.getAccessLevel().equals(AccessLevel.ALTO) || userProfile.getAccessLevel().equals(AccessLevel.MEDIO) || userProfile.getAccessLevel().equals(AccessLevel.BAIXO)) 
                    return true;
                break;
            case RESOURCES:
                return true; //todos podem pedir recursos  
            
        }
        return false;
    }

    

    /**
     * Creates a new file at the specified path if it does not already exist.
     * If the parent directories do not exist, they will be created.
     *
     * @param path the path where the file should be created
     */
    private void createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Checks if access to a specified room is allowed based on the provided access levels.
     *
     * @param accessLevels an array of AccessLevel objects representing the access levels of the user
     * @param roomName the name of the room to check access for
     * @return true if the user is allowed to enter the room, false otherwise
     */
    private boolean isAllowedToEnter(AccessLevel[] accessLevels, String roomName) {
        for (AccessLevel accessLevel : accessLevels) {
            if (accessLevel.toString().equals(roomName)) return true;
        }
        return false;
    }
}