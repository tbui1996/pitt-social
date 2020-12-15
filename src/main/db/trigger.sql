-- inserting on message automatically inserts into messageRecipient
CREATE OR REPLACE FUNCTION fun_message() RETURNS TRIGGER AS
$$
BEGIN
    IF new.touserid IS NOT NULL THEN
        INSERT INTO messagerecipient VALUES (new.msgID, new.touserid);
    END IF;
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tri_message
    AFTER INSERT
    ON messageInfo
    FOR EACH ROW
EXECUTE PROCEDURE fun_message();

-- inserting into friend automatically deletes from pendingFriend
CREATE OR REPLACE FUNCTION fun_friend() RETURNS TRIGGER AS
$$
BEGIN
    DELETE FROM pendingfriend WHERE fromID = new.userID1 AND toID = new.userID2;
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tri_friend
    AFTER INSERT
    ON friend
    FOR EACH ROW
EXECUTE PROCEDURE fun_friend();

-- inserting into group automatically deletes from pendingGroupMember
CREATE OR REPLACE FUNCTION fun_groupmember() RETURNS TRIGGER AS
$$
BEGIN
    DELETE FROM pendinggroupmember WHERE gID = new.gID AND new.userID = userID;
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tri_groupmember
    AFTER INSERT
    ON groupmember
    FOR EACH ROW
EXECUTE PROCEDURE fun_groupmember();

-- sending a message to a group automatically sends that message to every group member other than the sender
CREATE OR REPLACE FUNCTION fun_message_group() RETURNS TRIGGER AS
$$
DECLARE
    user_rec RECORD;
BEGIN
    FOR user_rec IN SELECT userID FROM groupmember WHERE gID = new.toGroupID
        LOOP
            IF user_rec.userID IS NOT NULL AND user_rec.userID <> new.fromID THEN
                INSERT INTO messagerecipient VALUES (new.msgid, user_rec.userid);
            END IF;
        END LOOP;
    RETURN new;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER tri_message_group
    AFTER INSERT
    ON messageInfo
    FOR EACH ROW
    WHEN (new.toGroupID IS NOT NULL)
EXECUTE PROCEDURE fun_message_group();

-- delete group memberships when deleting a user
CREATE OR REPLACE FUNCTION fun_delete_group_membership() RETURNS TRIGGER AS
$$
BEGIN
    DELETE FROM groupmember WHERE userid = old.userid;
    DELETE FROM pendinggroupmember WHERE userid = old.userid;
    RETURN old;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tri_delete_group_membership
    BEFORE DELETE
    ON profile
    FOR EACH ROW
EXECUTE PROCEDURE fun_delete_group_membership();

-- when deleting a user, delete messages that are left with no sender and no recipients
CREATE OR REPLACE FUNCTION fun_delete_messages() RETURNS TRIGGER AS
$$
DECLARE
    message_rec RECORD;
BEGIN
    -- delete entries from messagerecipient, set relevant id fields to NULL
    DELETE FROM messagerecipient WHERE userid = old.userid;
    UPDATE messageInfo SET touserid = NULL WHERE touserid = old.userid;
    UPDATE messageInfo SET fromid = NULL WHERE fromid = old.userid;

    --delete messageInfo rows where fromid and touserid are both NULL
    FOR message_rec IN SELECT * FROM messageInfo WHERE fromid IS NULL AND touserid IS NULL
        LOOP
            DELETE FROM messagerecipient WHERE msgid = message_rec.msgid;
        END LOOP;
    DELETE FROM messageInfo WHERE fromid IS NULL AND touserid IS NULL;
    RETURN old;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tri_delete_messages
    BEFORE DELETE
    ON profile
    FOR EACH ROW
EXECUTE PROCEDURE fun_delete_messages();

-- delete friendships when deleting a user
CREATE OR REPLACE FUNCTION fun_delete_friendships() RETURNS TRIGGER AS
$$
BEGIN
    DELETE FROM friend WHERE userid1 = old.userid OR userid2 = old.userid;
    DELETE FROM pendingfriend WHERE fromid = old.userid OR toid = old.userid;
    RETURN old;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tri_delete_friendships
    BEFORE DELETE
    ON profile
    FOR EACH ROW
EXECUTE PROCEDURE fun_delete_friendships();