package social_network.model.group;

import java.util.Date;
import social_network.model.enums.GroupRole;

public class GroupMember {

    private int groupId;
    private int userId;
    private GroupRole role;
    private Date joinedAt;

    // Constructor
    public GroupMember(int groupId, int userId, GroupRole role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = new Date();
    }

    // Getters
    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

    public GroupRole getRole() {
        return role;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }
}