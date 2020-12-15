# Pittsocial project, group 8

## When running demo-sample-data.sql:

If not present, add the following lines to the end of the file:

    ALTER SEQUENCE profile_userid_seq RESTART WITH 8;
    ALTER SEQUENCE groupinfo_gid_seq RESTART WITH 3;
    ALTER SEQUENCE messageinfo_msgid_seq RESTART WITH 5;

These are necessary because our database schema uses SERIAL auto-increment columns to handle profile.userID, groupInfo.gID, and messageInfo.msgID values.  The test data hardcodes these values to insert, so when further insertions try to use the auto-increment sequence (by using the DEFAULT value), they will get a primary key constraint violation since the sequence will try to start at 1.

## If you get an error about the postgres driver class not existing:

You may need to manually add the postgresql-42.2.5.jar file to the project dependencies on your machine, in whatever IDE you happen to be using (or on the command line).  We attempted to fix this by building our project with Maven to handle dependencies, but it still seems to be an issue when first downloading the project.