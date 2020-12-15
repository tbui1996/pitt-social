DROP TABLE IF EXISTS profile CASCADE;
DROP TABLE IF EXISTS friend CASCADE;
DROP TABLE IF EXISTS pendingFriend CASCADE;
DROP TABLE IF EXISTS messageInfo CASCADE;
DROP TABLE IF EXISTS messageRecipient CASCADE;
DROP TABLE IF EXISTS groupInfo CASCADE;
DROP TABLE IF EXISTS groupMember CASCADE;
DROP TABLE IF EXISTS pendingGroupMember CASCADE;

CREATE TABLE profile (
    userID        SERIAL,
    name          VARCHAR(50),
    email         VARCHAR(50) UNIQUE,
    password      VARCHAR(50),
    date_of_birth DATE,
    lastlogin     TIMESTAMP,

    CONSTRAINT profile_pk PRIMARY KEY (userID)
    );

CREATE TABLE groupInfo (
    gID         SERIAL PRIMARY KEY,
    name        VARCHAR(50),
    size        INT,
    description VARCHAR(200)

    );

CREATE TABLE friend (
    userID1 INT,
    userID2 INT,
    JDate   DATE,
    message VARCHAR(200),

    CONSTRAINT friend_pk PRIMARY KEY (userID1, userID2),
    CONSTRAINT friend_fk_1 FOREIGN KEY (userID1) REFERENCES PROFILE (userID),
    CONSTRAINT friend_fk_2 FOREIGN KEY (userID2) REFERENCES PROFILE (userID)
    );

CREATE TABLE pendingFriend (
    fromID  INT,
    toID    INT,
    message VARCHAR(200),

    CONSTRAINT pendingFriend_pk PRIMARY KEY (fromID, toID),
    CONSTRAINT pendingFriend_fk_1 FOREIGN KEY (fromID) REFERENCES profile (userID),
    CONSTRAINT pendingFriend_fk_2 FOREIGN KEY (toID) REFERENCES profile (userID)
    );

CREATE TABLE messageInfo (
    msgID     SERIAL PRIMARY KEY,
    fromID    INT,
    message   VARCHAR(200),
    toUserID  INT DEFAULT NULL,
    toGroupID INT DEFAULT NULL,
    timeSent  TIMESTAMP,

    CONSTRAINT message_fk_1 FOREIGN KEY (fromId) REFERENCES profile (userID),
    CONSTRAINT message_fk_2 FOREIGN KEY (toUserID) REFERENCES profile (userID),
    CONSTRAINT message_fk_3 FOREIGN KEY (toGroupID) REFERENCES groupInfo (gID)
    );

CREATE TABLE messageRecipient (
    msgID  INT,
    userID INT,

    CONSTRAINT messageRecipient_pk PRIMARY KEY (msgID, userID),
    CONSTRAINT messageRecipient_fk_1 FOREIGN KEY (msgID) REFERENCES messageInfo (msgID),
    CONSTRAINT messageRecipient_fk_2 FOREIGN KEY (userID) REFERENCES profile (userID)
    );

CREATE TABLE groupMember (
    gID    INT,
    userID INT,
    role   VARCHAR(200),

    CONSTRAINT groupMember_pk PRIMARY KEY (gID, userID),
    CONSTRAINT groupMember_fk_1 FOREIGN KEY (gID) REFERENCES groupInfo (gID),
    CONSTRAINT groupMember_fk_2 FOREIGN KEY (userID) REFERENCES profile (userID)
    );

CREATE TABLE pendingGroupMember (
    gID     INT,
    userID  INT,
    message VARCHAR(200),

    CONSTRAINT pendingGroupMember_pk PRIMARY KEY (gID, userID),
    CONSTRAINT pendingGroupMember_fk_1 FOREIGN KEY (gID) REFERENCES groupInfo (gID),
    CONSTRAINT pendingGroupMember_fk_2 FOREIGN KEY (userID) REFERENCES profile (userID)
    );