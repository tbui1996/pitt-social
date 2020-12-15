INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (1, 'Shenoda', 'shg@pitt.edu', 'shpwd', '1997-10-13', '2019-10-09 15:00:00');
INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (2, 'Lory', 'lra@pitt.edu', 'lpwd', '1996-03-08', '2019-10-09 16:00:00');
INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (3, 'Peter', 'pdj@pitt.edu', 'ppwd', '1994-01-09', '2019-10-10 15:00:00');
INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (4, 'Alexandrie', 'alx@pitt.edu', 'apwd', '1995-08-21', '2019-10-10 16:00:00');
INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (5, 'Panickos', 'pnk@pitt.edu', 'kpwd', '1997-09-08', '2019-10-11 15:00:00');
INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (6, 'Socratis', 'soc@pitt.edu', 'spwd', '1991-05-17', '2019-10-11 16:00:00');
INSERT INTO profile (userID, name, email, password, date_of_birth, lastlogin)
VALUES (7, 'Yaw', 'yaw@pitt.edu', 'ypwd', '1997-02-27', '2019-10-12 15:00:00');

INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (1, 2, '2019-01-06', 'Hey, it is me  Shenoda!');
INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (1, 5, '2019-01-15', 'Hey, it is me  Shenoda!');
INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (2, 3, '2019-08-23', 'Hey, it is me  Lory!');
INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (2, 4, '2019-02-17', 'Hey, it is me  Lory!');
INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (3, 4, '2019-09-16', 'Hey, it is me  Peter!');
INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (4, 6, '2019-10-06', 'Hey, it is me  Alexandrie!');
INSERT INTO friend (userID1, userID2, JDate, message)
VALUES (6, 7, '2019-09-13', 'Hey, it is me  Socratis!');

INSERT INTO pendingFriend (fromID, toID, message)
VALUES (7, 4, 'Hey, it is me Yaw');
INSERT INTO pendingFriend (fromID, toID, message)
VALUES (5, 2, 'Hey, it is me Panickos');
INSERT INTO pendingFriend (fromID, toID, message)
VALUES (2, 6, 'Hey, it is me Lory');

INSERT INTO groupInfo (gID, name, size, description)
VALUES (1, 'Grads at CS', 100, 'list of all graduate students');
INSERT INTO groupInfo (gID, name, size, description)
VALUES (2, 'DB Group', 10, 'members of the ADMT Lab');

INSERT INTO groupMember (gID, userID, role)
VALUES (1, 1, 'manager');
INSERT INTO groupMember (gID, userID, role)
VALUES (1, 2, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (1, 3, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (1, 4, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (1, 5, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (1, 6, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (1, 7, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (2, 1, 'manager');
INSERT INTO groupMember (gID, userID, role)
VALUES (2, 2, 'member');
INSERT INTO groupMember (gID, userID, role)
VALUES (2, 5, 'member');

INSERT INTO messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent)
VALUES (1, 1, 'Are we meeting tomorrow for the project?', 2, NULL, '2019-12-01 15:00:00');
INSERT INTO messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent)
VALUES (2, 1, 'Peters pub tomorrow?', 5, NULL, '2019-11-01 16:00:00');
INSERT INTO messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent)
VALUES (3, 2, 'Please join our DB Group forum tomorrow', NULL, 1, '2019-10-10 15:00:00');
INSERT INTO messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent)
VALUES (4, 5, 'Here is the paper I will present tomorrow', NULL, 2, '2019-10-10 16:00:00');

INSERT INTO messageRecipient (msgID, userID)
VALUES (1, 2);
INSERT INTO messageRecipient (msgID, userID)
VALUES (2, 5);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 1);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 2);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 3);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 4);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 5);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 6);
INSERT INTO messageRecipient (msgID, userID)
VALUES (3, 7);
INSERT INTO messageRecipient (msgID, userID)
VALUES (4, 1);
INSERT INTO messageRecipient (msgID, userID)
VALUES (4, 2);
INSERT INTO messageRecipient (msgID, userID)
VALUES (4, 5);

-- fix SERIAL sequences because of manually specified ID values in test data
ALTER SEQUENCE profile_userid_seq RESTART WITH 8;
ALTER SEQUENCE groupinfo_gid_seq RESTART WITH 3;
ALTER SEQUENCE messageinfo_msgid_seq RESTART WITH 5;