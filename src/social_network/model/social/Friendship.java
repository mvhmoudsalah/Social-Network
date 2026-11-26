package social_network.model.social;

import java.util.Date;

public class Friendship {

    private int userA;
    private int userB;
    private Date since;

    // Constructor
    public Friendship(int userA, int userB) {
        this.userA = userA;
        this.userB = userB;
        this.since = new Date();
    }

    // Returns the other user in the friendship
    public int getOtherUser(int userId) {

        if (userId == userA) {
            return userB;
        }

        if (userId == userB) {
            return userA;
        }

        throw new IllegalArgumentException("User is not part of this friendship");
    }

    // Getters
    public int getUserA() {
        return userA;
    }

    public int getUserB() {
        return userB;
    }

    public Date getSince() {
        return since;
    }
}