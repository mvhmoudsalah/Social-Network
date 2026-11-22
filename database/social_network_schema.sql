-- Social Network Graph Analysis - database schema (v3, 21 tables)
-- MySQL 8 flavored. Change AUTO_INCREMENT, ENUM and BOOLEAN for PostgreSQL or SQLite.
-- Tables are in dependency order. `groups` is a reserved word in MySQL 8, so it is quoted.
--
-- NOTE: MySQL 8 refuses CHECK constraints on columns used in a foreign key with
-- ON DELETE CASCADE (error 3823). Those five rules are therefore enforced by
-- triggers at the end of this file. They are marked "-- CHECK (...)" below.

CREATE DATABASE IF NOT EXISTS social_network CHARACTER SET utf8mb4;
USE social_network;

CREATE TABLE users (
id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(30) NOT NULL UNIQUE,
email VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
display_name VARCHAR(60) NULL,
avatar_url VARCHAR(500) NULL,
bio TEXT NULL,
is_active BOOLEAN NOT NULL DEFAULT TRUE,
last_login_at DATETIME NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
content TEXT NOT NULL,
visibility ENUM('public', 'friends', 'private') NOT NULL DEFAULT 'public',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_posts_user ON posts (user_id, created_at);

CREATE TABLE comments (
id INT AUTO_INCREMENT PRIMARY KEY,
post_id INT NOT NULL,
user_id INT NOT NULL,
content TEXT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_comments_post ON comments (post_id, created_at);

CREATE TABLE likes (
user_id INT NOT NULL,
post_id INT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (user_id, post_id),
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
CREATE INDEX idx_likes_post ON likes (post_id);

CREATE TABLE saved_posts (
user_id INT NOT NULL,
post_id INT NOT NULL,
saved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (user_id, post_id),
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
CREATE INDEX idx_saved_user ON saved_posts (user_id, saved_at);

CREATE TABLE shares (
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
post_id INT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
CREATE INDEX idx_shares_post ON shares (post_id);
CREATE INDEX idx_shares_user ON shares (user_id, created_at);

CREATE TABLE post_media (
id INT AUTO_INCREMENT PRIMARY KEY,
post_id INT NOT NULL,
media_url VARCHAR(500) NOT NULL,
media_type ENUM('image', 'video') NOT NULL,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
CREATE INDEX idx_post_media_post ON post_media (post_id);

CREATE TABLE hashtags (
id INT AUTO_INCREMENT PRIMARY KEY,
tag VARCHAR(100) NOT NULL UNIQUE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_hashtags (
post_id INT NOT NULL,
hashtag_id INT NOT NULL,
PRIMARY KEY (post_id, hashtag_id),
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE
);
CREATE INDEX idx_post_hashtags_tag ON post_hashtags (hashtag_id);

CREATE TABLE friend_requests (
id INT AUTO_INCREMENT PRIMARY KEY,
from_id INT NOT NULL,
to_id INT NOT NULL,
status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
responded_at DATETIME NULL,
UNIQUE (from_id, to_id),
-- CHECK (from_id <> to_id)  -> trigger trg_friend_requests_*
FOREIGN KEY (from_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (to_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_requests_to ON friend_requests (to_id, status);

CREATE TABLE friendships (
user_a INT NOT NULL,
user_b INT NOT NULL,
since DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (user_a, user_b),
-- CHECK (user_a < user_b)  -> trigger trg_friendships_*
FOREIGN KEY (user_a) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (user_b) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_friendships_b ON friendships (user_b);

CREATE TABLE follows (
follower_id INT NOT NULL,
following_id INT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (follower_id, following_id),
-- CHECK (follower_id <> following_id)  -> trigger trg_follows_*
FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_follows_following ON follows (following_id);

CREATE TABLE blocked_users (
blocker_id INT NOT NULL,
blocked_id INT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (blocker_id, blocked_id),
-- CHECK (blocker_id <> blocked_id)  -> trigger trg_blocked_users_*
FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_blocked_blocked ON blocked_users (blocked_id);

CREATE TABLE conversations (
id INT AUTO_INCREMENT PRIMARY KEY,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conversation_members (
conversation_id INT NOT NULL,
user_id INT NOT NULL,
joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (conversation_id, user_id),
FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_conv_members_user ON conversation_members (user_id);

CREATE TABLE messages (
id INT AUTO_INCREMENT PRIMARY KEY,
conversation_id INT NOT NULL,
sender_id INT NOT NULL,
content TEXT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
is_read BOOLEAN NOT NULL DEFAULT FALSE,
FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_messages_conv ON messages (conversation_id, created_at);

CREATE TABLE `groups` (
id INT AUTO_INCREMENT PRIMARY KEY,
created_by INT NOT NULL,
name VARCHAR(100) NOT NULL,
description TEXT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE group_members (
group_id INT NOT NULL,
user_id INT NOT NULL,
role ENUM('owner', 'admin', 'member') NOT NULL DEFAULT 'member',
joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (group_id, user_id),
FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_group_members_user ON group_members (user_id);

CREATE TABLE notifications (
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
actor_id INT NOT NULL,
type ENUM('like', 'comment', 'friend_request', 'friend_accept', 'follow', 'share', 'message') NOT NULL,
post_id INT NULL,
comment_id INT NULL,
is_read BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);
CREATE INDEX idx_notif_user ON notifications (user_id, is_read, created_at);

CREATE TABLE reports (
id INT AUTO_INCREMENT PRIMARY KEY,
reporter_id INT NOT NULL,
post_id INT NULL,
reported_user_id INT NULL,
reason ENUM('spam', 'harassment', 'hate_speech', 'violence', 'nudity', 'other') NOT NULL,
details TEXT NULL,
status ENUM('open', 'reviewing', 'resolved', 'dismissed') NOT NULL DEFAULT 'open',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
resolved_at DATETIME NULL,
UNIQUE (reporter_id, post_id),
UNIQUE (reporter_id, reported_user_id),
-- CHECK ((post_id IS NULL) <> (reported_user_id IS NULL))  -> trigger trg_reports_*
FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_reports_status ON reports (status, created_at);
CREATE INDEX idx_reports_post ON reports (post_id);
CREATE INDEX idx_reports_user ON reports (reported_user_id);

CREATE TABLE user_settings (
user_id INT PRIMARY KEY,
is_private BOOLEAN NOT NULL DEFAULT FALSE,
post_visibility ENUM('public', 'friends', 'private') NOT NULL DEFAULT 'public',
who_can_message ENUM('everyone', 'friends', 'nobody') NOT NULL DEFAULT 'everyone',
notify_enabled BOOLEAN NOT NULL DEFAULT TRUE,
language VARCHAR(10) NOT NULL DEFAULT 'en',
theme ENUM('light', 'dark', 'system') NOT NULL DEFAULT 'system',
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Triggers that replace the CHECK constraints (see note at the top)
DELIMITER $$

CREATE TRIGGER trg_friendships_bi BEFORE INSERT ON friendships FOR EACH ROW
BEGIN
  IF NEW.user_a >= NEW.user_b THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'user_a must be less than user_b';
  END IF;
END$$

CREATE TRIGGER trg_friendships_bu BEFORE UPDATE ON friendships FOR EACH ROW
BEGIN
  IF NEW.user_a >= NEW.user_b THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'user_a must be less than user_b';
  END IF;
END$$

CREATE TRIGGER trg_follows_bi BEFORE INSERT ON follows FOR EACH ROW
BEGIN
  IF NEW.follower_id = NEW.following_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a user cannot follow themselves';
  END IF;
END$$

CREATE TRIGGER trg_follows_bu BEFORE UPDATE ON follows FOR EACH ROW
BEGIN
  IF NEW.follower_id = NEW.following_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a user cannot follow themselves';
  END IF;
END$$

CREATE TRIGGER trg_blocked_users_bi BEFORE INSERT ON blocked_users FOR EACH ROW
BEGIN
  IF NEW.blocker_id = NEW.blocked_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a user cannot block themselves';
  END IF;
END$$

CREATE TRIGGER trg_blocked_users_bu BEFORE UPDATE ON blocked_users FOR EACH ROW
BEGIN
  IF NEW.blocker_id = NEW.blocked_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a user cannot block themselves';
  END IF;
END$$

CREATE TRIGGER trg_friend_requests_bi BEFORE INSERT ON friend_requests FOR EACH ROW
BEGIN
  IF NEW.from_id = NEW.to_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a user cannot send a request to themselves';
  END IF;
END$$

CREATE TRIGGER trg_friend_requests_bu BEFORE UPDATE ON friend_requests FOR EACH ROW
BEGIN
  IF NEW.from_id = NEW.to_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a user cannot send a request to themselves';
  END IF;
END$$

CREATE TRIGGER trg_reports_bi BEFORE INSERT ON reports FOR EACH ROW
BEGIN
  IF (NEW.post_id IS NULL) = (NEW.reported_user_id IS NULL) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a report must target exactly one post or one user';
  END IF;
END$$

CREATE TRIGGER trg_reports_bu BEFORE UPDATE ON reports FOR EACH ROW
BEGIN
  IF (NEW.post_id IS NULL) = (NEW.reported_user_id IS NULL) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'a report must target exactly one post or one user';
  END IF;
END$$

DELIMITER ;
 