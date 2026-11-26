package social_network.model.group;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import social_network.model.enums.GroupRole;

public class Group {

    private int id;
    private int createdBy;
    private String name;
    private String description;
    private Date createdAt;

    private List<GroupMember> members;

    // Constructor
    public Group(int id, int createdBy, String name, String description) {
        this.id = id;
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
        this.createdAt = new Date();

        this.members = new ArrayList<>();

        // Add the creator as the group owner
        members.add(new GroupMember(id, createdBy, GroupRole.OWNER));
    }

    // Add a member
    public void addMember(int userId) {
        members.add(new GroupMember(id, userId, GroupRole.MEMBER));
    }

    // Remove a member
    public void removeMember(int userId) {
        members.removeIf(member -> member.getUserId() == userId);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public List<GroupMember> getMembers() {
        return members;
    }
}