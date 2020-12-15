-- portable PL/pgSQL alternative to insert test data

ALTER SEQUENCE profile_userid_seq RESTART WITH 1;
ALTER SEQUENCE groupinfo_gid_seq RESTART WITH 1;
ALTER SEQUENCE messageinfo_msgid_seq RESTART WITH 1;

-- insert 100 users
CREATE OR REPLACE PROCEDURE insert_users() AS
$$
DECLARE
    i INT := 1;
BEGIN
    WHILE i <= 100
        LOOP
            INSERT INTO profile VALUES (DEFAULT, 'user' || i, 'user' || i || '@email.com', 'password' || i, now(), now());
            i := i + 1;
        END LOOP;
END ;
$$ LANGUAGE plpgsql;

CALL insert_users();

-- insert 10 groups
CREATE OR REPLACE PROCEDURE insert_groups() AS
$$
DECLARE
    i INT := 1;
BEGIN
    WHILE i <= 10
        LOOP
            INSERT INTO groupInfo VALUES (DEFAULT, 'group' || i, i * 10, 'this is group ' || i);
            i := i + 1;
        END LOOP;
END ;
$$ LANGUAGE plpgsql;

CALL insert_groups();

-- insert 200 friendships
CREATE OR REPLACE PROCEDURE insert_friends() AS
$$
DECLARE
    i INT := 1;
    j INT := 2;
BEGIN
    -- generates 99 friendships
    WHILE j <= 100
        LOOP
            INSERT INTO friend VALUES (i, j, now(), 'friendship between user ' || i || ' and user ' || j);
            j := j + 1;
        END LOOP;

    -- generates 98 more friendships
    i := 2;
    j := 3;
    WHILE j <= 100
        LOOP
            INSERT INTO friend VALUES (i, j, now(), 'friendship between user ' || i || ' and user ' || j);
            j := j + 1;
        END LOOP;

    -- generates the last 3 friendships to hit 200
    i := 3;
    j := 4;
    WHILE j <= 6
        LOOP
            INSERT INTO friend VALUES (i, j, now(), 'friendship between user ' || i || ' and user ' || j);
            j := j + 1;
        END LOOP;
END ;
$$ LANGUAGE plpgsql;

CALL insert_friends();

-- insert 300 messages
CREATE OR REPLACE PROCEDURE insert_messages() AS
$$
DECLARE
    i INT := 1;
    j INT := 2;
BEGIN
    -- 200 messages from user to user
    -- generates 99 messages
    WHILE j <= 100
        LOOP
            INSERT INTO messageInfo VALUES (DEFAULT, i, 'message from user ' || i || ' to user ' || j, j, NULL, now());
            j := j + 1;
        END LOOP;

    -- generates 98 more messages
    i := 2;
    j := 3;
    WHILE j <= 100
        LOOP
            INSERT INTO messageInfo VALUES (DEFAULT, i, 'message from user ' || i || ' to user ' || j, j, NULL, now());
            j := j + 1;
        END LOOP;

    -- generates the last 3 messages to hit 200
    i := 3;
    j := 4;
    WHILE j <= 6
        LOOP
            INSERT INTO messageInfo VALUES (DEFAULT, i, 'message from user ' || i || ' to user ' || j, j, NULL, now());
            j := j + 1;
        END LOOP;

    -- 100 messages from user to group
    i := 1;
    WHILE i <= 100
        LOOP
            INSERT INTO messageInfo VALUES (DEFAULT, i, 'message from user ' || i || ' to group 1', NULL, 1, now());
            i := i + 1;
        END LOOP;
END ;
$$ LANGUAGE plpgsql;

CALL insert_messages();