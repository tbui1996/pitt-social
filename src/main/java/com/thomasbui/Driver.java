package com.thomasbui;

import java.sql.SQLException;

public class Driver {
    public static void main(String[] args) throws SQLException, Exception {
        // RECREATE DB BEFORE RUNNING
        PittSocialJDBC jdbc = new PittSocialJDBC();

        // Create users
        jdbc.createUser("JDBC_TEST_1", "password1", "email1", "2019-01-01");
        jdbc.displayTable("profile");

        jdbc.createUser("JDBC_TEST_2", "password2", "email2", "2019-01-01");
        jdbc.displayTable("profile");

        // Login (unsuccessfully, then successfully) as test user 1
        jdbc.login("email1", "not_password1");
        jdbc.login("email1", "password1");

        // Create a group
        jdbc.createGroup("Tottenham", "Welcome", 1);
        jdbc.displayTable("groupInfo");

        // Send friend request to test user 2
        jdbc.initiateFriendship(2);
        jdbc.displayTable("pendingFriend");

        // Send a message to test user 2
        jdbc.sendMessageToUser(2);
        jdbc.displayTable("messageInfo");
        jdbc.displayTable("messageRecipient");

        // User 1 logs out
        jdbc.logout();
        jdbc.displayTable("profile");

        // User 2 logs in
        jdbc.login("email2", "password2");

        // User 2 is prompted to accept user 1's friend request
        jdbc.confirmRequests();
        jdbc.displayTable("friend");

        // User 2 checks messages
        jdbc.displayMessages();

        // User 2 requests to join user 1's group
        jdbc.initiateAddingGroup(1, "message");
        jdbc.displayTable("pendingGroupMember");

        // User 2 displays list of friends
        jdbc.displayFriends();

        // User 2 logs out
        jdbc.logout();
        jdbc.displayTable("profile");

        // User 1 logs back in
        jdbc.login("email1", "password1");

        // User 1 is prompted to accept user 2's group join request
        jdbc.confirmRequests();
        jdbc.displayTable("groupMember");

        // User 1 displays new messages received since last login
        jdbc.displayNewMessages();

        // User 1 sends a message to the group they created
        jdbc.sendMessageToGroup(1);
        jdbc.displayTable("messageInfo");
        jdbc.displayTable("messageRecipient");

        // User 1 performs a search for users with "2" in name or email
        jdbc.searchForUser("2");

        // User 1 performs a three-degrees search for user id 2
        jdbc.threeDegrees(2);

        // User 1 checks the top 10 most frequent users they communicated with over the last month
        jdbc.topMessages(1, 10);

        // User 1 deletes their account
        jdbc.dropUser();
        jdbc.displayTable("profile");

        // User 2 logs in
        jdbc.login("email2", "password2");

        // User 2 deletes their account
        jdbc.dropUser();
        jdbc.displayTable("profile");

        jdbc.exit();
    }
}
