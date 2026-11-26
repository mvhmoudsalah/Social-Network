package social_network.model.social;

import java.util.Date;
import social_network.model.enums.FriendRequestStatus;

public class FriendRequest {

    private int id;
    private int fromId;
    private int toId;
    private FriendRequestStatus status;
    private Date createdAt;
    private Date respondedAt;

    public FriendRequest(int id, int fromId, int toId) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.status = FriendRequestStatus.PENDING;
        this.createdAt = new Date();
    }

    public void accept() {
        status = FriendRequestStatus.ACCEPTED;
        respondedAt = new Date();
    }

    public void reject() {
        status = FriendRequestStatus.REJECTED;
        respondedAt = new Date();
    }

    public int getId() {
        return id;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getRespondedAt() {
        return respondedAt;
    }
}