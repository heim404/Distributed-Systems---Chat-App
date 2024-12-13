package dev.superman.server.schema;

/**
 * Represents a user profile in the system.
 */
public class UserProfile {

    /**
     * The temporary name of the user.
     */
    private String temporaryName;

    /**
     * The actual name of the user.
     */
    private String name;

    /**
     * The access level of the user.
     */
    private AccessLevel accessLevel;

    /**
     * Indicates whether the user is logged in.
     */
    private boolean loggedIn;

    /**
     * The current room the user is in.
     */
    private String currentRoom;

    /**
     * Constructs a UserProfile with a temporary name.
     *
     * @param temporaryName the temporary name of the user
     */
    public UserProfile(String temporaryName) {
        this.temporaryName = temporaryName;
    }

    /**
     * Gets the temporary name of the user.
     *
     * @return the temporary name of the user
     */
    public String getTemporaryName() {
        return temporaryName;
    }

    /**
     * Gets the actual name of the user.
     *
     * @return the actual name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the actual name of the user.
     *
     * @param name the actual name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the access level of the user.
     *
     * @return the access level of the user
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the access level of the user.
     *
     * @param accessLevel the access level of the user
     */
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Checks if the user is logged in.
     *
     * @return true if the user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Sets the logged-in status of the user.
     *
     * @param loggedIn the logged-in status of the user
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Sets the current room of the user.
     *
     * @param room the current room of the user
     */
    public void setCurrentRoom(String room) {
        this.currentRoom = room;
    }

    /**
     * Gets the current room of the user.
     *
     * @return the current room of the user
     */
    public String getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Represents the access levels a user can have.
     */
    public enum AccessLevel {
        ALTO,
        MEDIO,
        BAIXO,
        CONVIDADO;

        /**
         * Returns the string representation of the access level.
         *
         * @return the string representation of the access level
         */
        @Override
        public String toString() {
            return switch (this) {
                case ALTO -> "ALTO";
                case MEDIO -> "MEDIO";
                case BAIXO -> "BAIXO";
                case CONVIDADO -> "CONVIDADO";
            };
        }
    }
}
