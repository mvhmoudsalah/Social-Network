package social_network.model.user;

import java.util.Date;
import social_network.model.enums.PostVisibility;

public class UserSettings {

    private int userId;
    private boolean isPrivate;
    private PostVisibility postVisibility;
    private String whoCanMessage;
    private boolean notifyEnabled;
    private String language;
    private String theme;
    private Date updatedAt;

    // Constructor
    public UserSettings(int userId, boolean isPrivate,
                        PostVisibility postVisibility,
                        String whoCanMessage, boolean notifyEnabled,
                        String language, String theme) {

        this.userId = userId;
        this.isPrivate = isPrivate;
        this.postVisibility = postVisibility;
        this.whoCanMessage = whoCanMessage;
        this.notifyEnabled = notifyEnabled;
        this.language = language;
        this.theme = theme;
        this.updatedAt = new Date();
    }

    // Update settings
    public void update(UserSettings newSettings) {
        this.isPrivate = newSettings.isPrivate;
        this.postVisibility = newSettings.postVisibility;
        this.whoCanMessage = newSettings.whoCanMessage;
        this.notifyEnabled = newSettings.notifyEnabled;
        this.language = newSettings.language;
        this.theme = newSettings.theme; //Light or dark
        this.updatedAt = new Date();
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public PostVisibility getPostVisibility() {
        return postVisibility;
    }

    public String getWhoCanMessage() {
        return whoCanMessage;
    }

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public String getTheme() {
        return theme;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}