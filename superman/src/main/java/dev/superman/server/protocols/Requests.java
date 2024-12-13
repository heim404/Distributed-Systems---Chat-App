package dev.superman.server.protocols;

import dev.superman.ED.SynchronizedArrayList;
import dev.superman.server.schema.UserProfile;
import dev.superman.server.schema.UserProfile.AccessLevel;

import java.util.Iterator;

import dev.superman.server.Server;
import dev.superman.server.protocols.Requests;

/**
 * The Requests class manages and processes various types of requests made by users.
 * It maintains a synchronized list of requests and provides methods to add, remove, and print requests.
 * It also includes a nested requestScan class that continuously scans for requests and broadcasts them using the server.
 */
public class Requests {
    
    private SynchronizedArrayList<String> requests;
    private Server server;
    public Requests(Server server) {
        this.server = server;
        requests = new SynchronizedArrayList<>();
        new requestScan().start();
    }
    
    /**
     * Adds a request to the list of requests.
     *
     * @param requestType the type of the request
     * @param user the user profile making the request
     */
    public void addRequest(RequestType requestType,UserProfile user){
        requests.add(user.getName() +"," + requestType); //adiciona o pedido com autor,tipo de pedido
    }

    /**
     * Retrieves the list of requests.
     *
     * @return a SynchronizedArrayList containing the requests.
     */
    public SynchronizedArrayList<String> getRequests() {
        return requests;
    }

    /**
     * Removes a request of a specified type for a given user.
     *
     * @param user The user profile attempting to remove the request.
     * @param requestType The type of request to be removed (e.g., "EVAC", "COMMS", "RES").
     * @return A message indicating the result of the operation:
     *         - "Invalid request type" if the request type is not recognized.
     *         - "You cannot accept your own request" if the user is trying to accept their own request.
     *         - "You don't have permission to accept this alert" if the user lacks the necessary permissions.
     *         - "Alert ended" if the request was successfully removed.
     *         - "Request not found" if no matching request was found.
     */
    public String removeRequest(UserProfile user,String requestType){
        requestType = requestType.toUpperCase();
        RequestType type = null;
        switch (requestType) {
            case "EVAC":
                type = RequestType.EVACUATION;
                break;
            case "COMMS":
                type = RequestType.COMMUNICATION;
                break;
            case "RES":
                type = RequestType.RESOURCES;
                break;
            default:
                type = null;
                break;
        }
        if (type == null) {
            return "Invalid request type";
        }

        Iterator<String> it = requests.iterator();
        while (it.hasNext()) {
            String[] parts = it.next().split(",");
            if (parts[0].equals(user.getName()) && parts[1].equals(type.toString())) {
                return "You cannout accept your own request";
            }
            if (!permissionToAccept(user, type)) {
                return "You dont have permission to accept this alert";
            } 
            if (!parts[0].equals(user.getName()) && parts[1].equals(type.toString())) {
                it.remove();
                server.serverBroadcast("[System]: Alert " + type.toString() + " request accepted by " + user.getName());
                return "Alert ended";
            }
        }
        return "Request not found";
    }
    
    /**
     * Generates a formatted string of all requests in the system.
     * Each request is represented as a line in the format:
     * "[System]: Alert <alert> requested by <requester> needs to be accepted".
     *
     * @return A string containing all formatted requests.
     */
    public String printRequests() {
        String result = "";
        String parts[];
        Iterator<String> it = requests.iterator();
        while (it.hasNext()) {
            parts = it.next().split(",");
            result += "[System]: Alert " + parts[1] + " requested by "+parts[0] + " needs to be accepted\n";
        }
        return result;
    }

    /**
     * Determines if the user has permission to accept a specific request type.
     *
     * @param userProfile the profile of the user making the request
     * @param requestType the type of request being made
     * @return true if the user has permission to accept the request, false otherwise
     */
    private boolean permissionToAccept(UserProfile userProfile, Requests.RequestType requestType ) {
        System.out.println(requestType);
        switch (requestType) {
            case EVACUATION:
                if(!userProfile.getAccessLevel().equals(AccessLevel.ALTO)) //apenas o alto aprova
                    return false;
                break;   
            case COMMUNICATION:
                if ((userProfile.getAccessLevel().equals(AccessLevel.CONVIDADO))||(userProfile.getAccessLevel().equals(AccessLevel.BAIXO))) //apenas o alto e medio aprovam
                    return false;

                break;
            case RESOURCES:
                if (userProfile.getAccessLevel().equals(AccessLevel.CONVIDADO)) //todos aprovam menos o convidado
                    return false; 
                break;
        }
        return true;
    }
    
    /**
     * Enum representing the different types of requests that can be made.
     */
    public enum RequestType {
        EVACUATION,COMMUNICATION,RESOURCES
    }

    /**
     * The requestScan class extends the Thread class and continuously scans for requests.
     * If there are no requests, it sleeps for 10 seconds before checking again.
     * If there are requests, it broadcasts them using the server's broadcast method and then sleeps for 10 seconds.
     * 
     * The run method contains an infinite loop that performs the above actions.
     * 
     * If the thread is interrupted during sleep, it catches the InterruptedException and prints the stack trace.
     */
    public class requestScan extends Thread{
        
        public void run(){
            while(true){
                try {
                    if (requests.isEmpty()) {
                        Thread.sleep(10000);
                    }else{
                        server.serverBroadcast(printRequests());
                        Thread.sleep(10000);
                    }
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        }
    }
}



