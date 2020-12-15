package com.thomasbui;

import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.*;

/**
 * Hello world!
 */
public class App {
    private static Scanner scan = new Scanner(System.in);
    private static PittSocialJDBC jdbc;

    public static void main(String[] args) throws SQLException, ClassNotFoundException, ParseException, Exception {
        /* Tested and functional: createUser, login, createGroup, initiateFriendship, initiateAddingGroup,
         * 						  logout, searchForUser, sendMessageToUser, sendMessageToGroup, dropUser
         * Tested and non-functional:
         * Needs to be tested:
         * Unfinished: everything else
         */

        jdbc = null;
        try {
            jdbc = new PittSocialJDBC();
            if (jdbc != null) {
                System.out.println("Connection established");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jdbc = new PittSocialJDBC();
            if (jdbc != null) {
                try {
                    jdbc.cleanup();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        userLoggedOut();
    }

    private static void userLoggedOut() throws SQLException, Exception {
        String password = "", name = "", email = "", dob = "";
        while (true) {
            try {
                System.out.println("\nWhat would you like to do?\n (Enter 0 TO EXIT)\n\n"
                        + "1 - LOGIN\n"
                        + "2 - CREATE USER\n");
                System.out.println("Enter number:");
                int x = scan.nextInt();
                scan.nextLine();

                switch (x) {
                    case 0:
                        System.out.println("Goodbye!");
                        jdbc.exit();
                        break;
                    case 1:
                        System.out.println("\n[LOGIN]");
                        email = getUserInputString("Please enter email: ");
                        password = getUserInputString("Please enter password: ");
                        if(jdbc.login(email, password)) {
                            userLoggedIn(email, password);
                        }
                        break;
                    case 2:
                        System.out.println("\n[CREATE USER]");
                        name = getUserInputString("Please enter name:");
                        password = getUserInputString("Please enter a password: ");
                        email = getUserInputString("Please enter a email address: ");
                        dob = getUserInputString("Please enter Date of Birth(YYYY-MM-DD): ");
                        jdbc.createUser(name, password, email, dob);
                        break;
                    default:
                        System.out.println("Invalid input option.");
                        break;
                }
            } catch (NumberFormatException | ParseException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }//end while
    }

    private static void userLoggedIn(String email, String password) throws SQLException, Exception {
        boolean loggedIn = true;
        while(loggedIn) {
            System.out.println("\nWhat would you like to do?\n (Enter 0 TO EXIT)\n\n"
                    + "1 - [Initiate Friendship]\n"
                    + "2 - [Create Group]\n"
                    + "3 - [Initiate Adding Group]\n"
                    + "4 - [Confirm Requests]\n"
                    + "5 - [Send Message To User]\n"
                    + "6 - [Send Message To Group]\n"
                    + "7 - [Display Messages]\n"
                    + "8 - [Display New Messages]\n"
                    + "9 - [Display Friends]\n"
                    + "10 - [Search For User]\n"
                    + "11 - [Three Degrees]\n"
                    + "12 - [Top Messages]\n"
                    + "13 - [Logout]\n"
                    + "14 - [Drop User]\n");

            System.out.println("Enter number:");
            int x = scan.nextInt();
            scan.nextLine();

            int userID2;
            String groupname, description;
            int memlimit, gID, sendToID, pastMonths, topKUsers;
            String groupmessage, searchForUser;

            switch (x) {
                case 0:
                    System.out.println("Goodbye!\n");
                    jdbc.logout();
                    jdbc.exit();
                    break;
                case 1:
                    System.out.println("[Initiate Friendship]\n");
                    System.out.println("Please enter the ID of the user you would like to befriend");
                    userID2 = scan.nextInt();
                    scan.nextLine();
                    jdbc.initiateFriendship(userID2);
                    break;
                case 2:
                    System.out.println("[Create Group]\n");
                    groupname = getUserInputString("Please enter a group name: ");
                    description = getUserInputString("Please enter a description: ");
                    memlimit = getUserNumber("Please enter a group limit: ");
                    jdbc.createGroup(groupname, description, memlimit);
                    break;
                case 3:
                    System.out.println("[Initiate Adding Group]\n");
                    gID = getUserNumber("Please enter a group ID: ");
                    groupmessage = getUserInputString("Please enter a a message: ");
                    jdbc.initiateAddingGroup(gID, groupmessage);
                    break;
                case 4:
                    System.out.println("[Confirm Requests]");
                    jdbc.confirmRequests();
                    break;
                case 5:
                    System.out.println("[Send Message To User]\n");
                    sendToID = getUserNumber("Please enter user ID to send message to: ");
                    jdbc.sendMessageToUser(sendToID);
                    break;
                case 6:
                    System.out.println("[Send Message To Group]\n");
                    gID = getUserNumber("Please enter group ID to send message to: ");
                    jdbc.sendMessageToGroup(gID);
                    break;

                case 7:
                    System.out.println("[Display Messages]\n");
                    jdbc.displayMessages();
                    break;
                case 8:
                    System.out.println("[Display New Messages]\n");
                    jdbc.displayNewMessages();
                    break;
                case 9:
                    System.out.println("[Display Friends]\n");
                    jdbc.displayFriends();
                    break;
                case 10:
                    System.out.println("[Search For User]\n");
                    searchForUser = getUserInputString("Search for user: ");
                    jdbc.searchForUser(searchForUser);
                    break;
                case 11:
                    System.out.println("[Three Degrees]\n");
                    userID2 = getUserNumber("Please enter the user ID to search for: ");
                    jdbc.threeDegrees(userID2);
                    break;
                case 12:
                    System.out.println("[Top Messages]\n");
                    pastMonths = getUserNumber("Please enter number of past months: ");
                    topKUsers = getUserNumber("Please enter the number of users to see: ");
                    jdbc.topMessages(pastMonths, topKUsers);
                    break;
                case 13:
                    System.out.println("[Logout]\n");
                    jdbc.logout();
                    loggedIn = false;
                    break;
                case 14:
                    System.out.println("[Drop User]\n");
                    jdbc.dropUser();
                    loggedIn = false;
                    break;
            }
        }
        userLoggedOut();
    }

    private static String getUserInputString(String input) {
        String inputString;
        do {
            System.out.println(input);
            inputString = scan.nextLine().trim();
        } while (inputString == null || inputString.equalsIgnoreCase(""));
        return inputString;
    }

    private static int getUserNumber(String message) {
        String userNumber;
        do {
            System.out.println(message);
            userNumber = scan.nextLine().trim();
        } while (!Pattern.matches("\\d+", userNumber));

        return Integer.parseInt(userNumber);
    }

}