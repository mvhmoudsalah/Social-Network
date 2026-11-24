
CREATE DATABASE IF NOT EXISTS social_network
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE social_network;

CREATE TABLE IF NOT EXISTS users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(30)  NOT NULL UNIQUE,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name  VARCHAR(60)  NULL,
  avatar_url    VARCHAR(500) NULL,
  bio           TEXT         NULL,
  is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
  last_login_at DATETIME     NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS posts (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT  NOT NULL,
  content    TEXT NOT NULL,
  visibility ENUM('public', 'friends', 'private') NOT NULL DEFAULT 'public',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_posts_user ON posts (user_id, created_at);

CREATE TABLE IF NOT EXISTS comments (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  post_id    INT  NOT NULL,
  user_id    INT  NOT NULL,
  content    TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_comments_post ON comments (post_id, created_at);

CREATE TABLE IF NOT EXISTS likes (
  user_id    INT NOT NULL,
  post_id    INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, post_id),
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_likes_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_likes_post ON likes (post_id);

CREATE TABLE IF NOT EXISTS saved_posts (
  user_id  INT NOT NULL,
  post_id  INT NOT NULL,
  saved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, post_id),
  CONSTRAINT fk_saved_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_saved_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_saved_user ON saved_posts (user_id, saved_at);

CREATE TABLE IF NOT EXISTS shares (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  post_id    INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_shares_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_shares_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_shares_post ON shares (post_id);
CREATE INDEX idx_shares_user ON shares (user_id, created_at);

CREATE TABLE IF NOT EXISTS post_media (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  post_id    INT NOT NULL,
  media_url  VARCHAR(500) NOT NULL,
  media_type ENUM('image', 'video') NOT NULL,
  CONSTRAINT fk_media_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_post_media_post ON post_media (post_id);

CREATE TABLE IF NOT EXISTS hashtags (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  tag        VARCHAR(100) NOT NULL UNIQUE,   -- store lowercase, without '#'
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS post_hashtags (
  post_id    INT NOT NULL,
  hashtag_id INT NOT NULL,
  PRIMARY KEY (post_id, hashtag_id),
  CONSTRAINT fk_ph_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_ph_hashtag FOREIGN KEY (hashtag_id)
    REFERENCES hashtags(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_post_hashtags_tag ON post_hashtags (hashtag_id);



CREATE TABLE IF NOT EXISTS friend_requests (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  from_id      INT NOT NULL,
  to_id        INT NOT NULL,
  status       ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  responded_at DATETIME NULL,
  UNIQUE (from_id, to_id),
  CONSTRAINT fk_fr_from FOREIGN KEY (from_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_fr_to FOREIGN KEY (to_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_requests_to ON friend_requests (to_id, status);

CREATE TABLE IF NOT EXISTS friendships (
  user_a INT NOT NULL,
  user_b INT NOT NULL,
  since  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_a, user_b),
  CONSTRAINT fk_fs_a FOREIGN KEY (user_a)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_fs_b FOREIGN KEY (user_b)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_friendships_b ON friendships (user_b);

CREATE TABLE IF NOT EXISTS follows (
  follower_id  INT NOT NULL,
  following_id INT NOT NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (follower_id, following_id),
  CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_follows_following FOREIGN KEY (following_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_follows_following ON follows (following_id);

CREATE TABLE IF NOT EXISTS blocked_users (
  blocker_id INT NOT NULL,
  blocked_id INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (blocker_id, blocked_id),
  CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_blocked_blocked ON blocked_users (blocked_id);

CREATE TABLE IF NOT EXISTS conversations (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS conversation_members (
  conversation_id INT NOT NULL,
  user_id         INT NOT NULL,
  joined_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (conversation_id, user_id),
  CONSTRAINT fk_cm_conv FOREIGN KEY (conversation_id)
    REFERENCES conversations(id) ON DELETE CASCADE,
  CONSTRAINT fk_cm_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_conv_members_user ON conversation_members (user_id);

CREATE TABLE IF NOT EXISTS messages (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  conversation_id INT  NOT NULL,
  sender_id       INT  NOT NULL,
  content         TEXT NOT NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_read         BOOLEAN  NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id)
    REFERENCES conversations(id) ON DELETE CASCADE,
  CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_messages_conv ON messages (conversation_id, created_at);

-- GROUPS is a reserved word in MySQL 8, so it is always quoted
CREATE TABLE IF NOT EXISTS `groups` (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  created_by  INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description TEXT         NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_groups_creator FOREIGN KEY (created_by)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_members (
  group_id  INT NOT NULL,
  user_id   INT NOT NULL,
  role      ENUM('owner', 'admin', 'member') NOT NULL DEFAULT 'member',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (group_id, user_id),
  CONSTRAINT fk_gm_group FOREIGN KEY (group_id)
    REFERENCES `groups`(id) ON DELETE CASCADE,
  CONSTRAINT fk_gm_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_group_members_user ON group_members (user_id);



CREATE TABLE IF NOT EXISTS notifications (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  actor_id   INT NOT NULL,
  type       ENUM('like', 'comment', 'friend_request', 'friend_accept',
                  'follow', 'share', 'message') NOT NULL,
  post_id    INT NULL,
  comment_id INT NULL,
  is_read    BOOLEAN  NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_notif_actor FOREIGN KEY (actor_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_notif_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_notif_comment FOREIGN KEY (comment_id)
    REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_notif_user ON notifications (user_id, is_read, created_at);

CREATE TABLE IF NOT EXISTS reports (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  reporter_id      INT NOT NULL,
  post_id          INT NULL,
  reported_user_id INT NULL,
  reason  ENUM('spam', 'harassment', 'hate_speech', 'violence', 'nudity', 'other') NOT NULL,
  details TEXT NULL,
  status  ENUM('open', 'reviewing', 'resolved', 'dismissed') NOT NULL DEFAULT 'open',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME NULL,
  UNIQUE (reporter_id, post_id),
  UNIQUE (reporter_id, reported_user_id),
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id)
    REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_post FOREIGN KEY (post_id)
    REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_user FOREIGN KEY (reported_user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_reports_status ON reports (status, created_at);
CREATE INDEX idx_reports_post   ON reports (post_id);
CREATE INDEX idx_reports_user   ON reports (reported_user_id);

CREATE TABLE IF NOT EXISTS user_settings (
  user_id         INT PRIMARY KEY,
  is_private      BOOLEAN NOT NULL DEFAULT FALSE,
  post_visibility ENUM('public', 'friends', 'private') NOT NULL DEFAULT 'public',
  who_can_message ENUM('everyone', 'friends', 'nobody') NOT NULL DEFAULT 'everyone',
  notify_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
  language        VARCHAR(10) NOT NULL DEFAULT 'en',
  theme           ENUM('light', 'dark', 'system') NOT NULL DEFAULT 'system',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_settings_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;


DELIMITER $$

-- friendships: user_a < user_b (edge stored once, no self-friendship)
CREATE TRIGGER trg_friendships_bi BEFORE INSERT ON friendships
FOR EACH ROW
BEGIN
  IF NEW.user_a >= NEW.user_b THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'friendships: user_a must be smaller than user_b';
  END IF;
END$$

CREATE TRIGGER trg_friendships_bu BEFORE UPDATE ON friendships
FOR EACH ROW
BEGIN
  IF NEW.user_a >= NEW.user_b THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'friendships: user_a must be smaller than user_b';
  END IF;
END$$

-- friend_requests: from_id <> to_id
CREATE TRIGGER trg_friend_requests_bi BEFORE INSERT ON friend_requests
FOR EACH ROW
BEGIN
  IF NEW.from_id = NEW.to_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'friend_requests: cannot send a request to yourself';
  END IF;
END$$

CREATE TRIGGER trg_friend_requests_bu BEFORE UPDATE ON friend_requests
FOR EACH ROW
BEGIN
  IF NEW.from_id = NEW.to_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'friend_requests: cannot send a request to yourself';
  END IF;
END$$

-- follows: follower_id <> following_id
CREATE TRIGGER trg_follows_bi BEFORE INSERT ON follows
FOR EACH ROW
BEGIN
  IF NEW.follower_id = NEW.following_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'follows: you cannot follow yourself';
  END IF;
END$$

CREATE TRIGGER trg_follows_bu BEFORE UPDATE ON follows
FOR EACH ROW
BEGIN
  IF NEW.follower_id = NEW.following_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'follows: you cannot follow yourself';
  END IF;
END$$

-- blocked_users: blocker_id <> blocked_id
CREATE TRIGGER trg_blocked_users_bi BEFORE INSERT ON blocked_users
FOR EACH ROW
BEGIN
  IF NEW.blocker_id = NEW.blocked_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'blocked_users: you cannot block yourself';
  END IF;
END$$

CREATE TRIGGER trg_blocked_users_bu BEFORE UPDATE ON blocked_users
FOR EACH ROW
BEGIN
  IF NEW.blocker_id = NEW.blocked_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'blocked_users: you cannot block yourself';
  END IF;
END$$

-- reports: exactly one target (post XOR user), and no self-report
CREATE TRIGGER trg_reports_bi BEFORE INSERT ON reports
FOR EACH ROW
BEGIN
  IF (NEW.post_id IS NULL) = (NEW.reported_user_id IS NULL) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'reports: a report must target exactly one post or one user';
  END IF;
  IF NEW.reported_user_id = NEW.reporter_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'reports: you cannot report yourself';
  END IF;
END$$

CREATE TRIGGER trg_reports_bu BEFORE UPDATE ON reports
FOR EACH ROW
BEGIN
  IF (NEW.post_id IS NULL) = (NEW.reported_user_id IS NULL) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'reports: a report must target exactly one post or one user';
  END IF;
  IF NEW.reported_user_id = NEW.reporter_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'reports: you cannot report yourself';
  END IF;
END$$

DELIMITER ;