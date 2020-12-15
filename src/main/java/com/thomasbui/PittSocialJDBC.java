package com.thomasbui;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.*;
import java.util.regex.Pattern;

class PittSocialJDBC {
    private String query;
    private PreparedStatement prepareStatement;
    private static Connection connection;
    private ResultSet resultSet;
    private Statement statement;
    private String ourEmail;
    private int ourID;
    private boolean loggedIn = false;
    private Scanner scanman = new Scanner(System.in);

    private Connection connectToDB() throws SQLException, ClassNotFoundException {
        String db_password = "postgres"; //update password to your actual postgres password
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", db_password);
        return DriverManager.getConnection(url, props);
    }

    void createUser(String name, String password, String email, String dob)
            throws SQLException, ClassNotFoundException, ParseException {
        connection = connectToDB();

        // convert dob string into SQL Date object
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date_of_birth = format.parse(dob);
        java.sql.Date sql_dob = new java.sql.Date(date_of_birth.getTime());

        query = "INSERT INTO profile VALUES (DEFAULT, ?, ?, ?, ?, ?)";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setString(1, name);
        prepareStatement.setString(2, email);
        prepareStatement.setString(3, password);
        prepareStatement.setDate(4, sql_dob);
        prepareStatement.setNull(5, java.sql.Types.TIMESTAMP);
        prepareStatement.executeUpdate();
    }

    boolean login(String email, String password) throws SQLException, ClassNotFoundException {
        if (loggedIn) {
            System.out.println("Error: already logged in");
            return true;
        }

        ourEmail = email;
        connection = connectToDB();
        try {
            query = "SELECT * FROM profile WHERE email = ? AND password = ?";
            prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            prepareStatement.setString(1, email);
            prepareStatement.setString(2, password);
            resultSet = prepareStatement.executeQuery();
            int rows_returned = 0;
            resultSet.beforeFirst();
            while (resultSet.next()) {
                rows_returned++;
            }
            if (rows_returned == 0) {
                System.out.println("Incorrect username and/or password.");
                loggedIn = false;
            } else if (rows_returned == 1) {
                System.out.println("Successfully logged in.");
                resultSet.last();
                ourID = resultSet.getInt("userid");
                loggedIn = true;
            } else {
                System.out.println("Multiple users with same email/password found, uh oh");
                loggedIn = false;
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        //closeSQL();
        return loggedIn;
    }

    void initiateFriendship(int friendID) throws SQLException, Exception {
        try {
            //Get yourself
            query = "SELECT userID FROM profile WHERE email = ?";
            prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            prepareStatement.setString(1, ourEmail);
            prepareStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println("SQL error in initiateFriendship() SELECT 1: " + e.getMessage());
        }

        try {
            //Display name of individual
            query = "SELECT name FROM profile WHERE userID = ?";
            prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            prepareStatement.setInt(1, friendID);
            resultSet = prepareStatement.executeQuery();
            resultSet.beforeFirst();
            if (resultSet.next()) {
                System.out.println("Name of user receiving friend request:" + resultSet.getString("Name"));
            } else {
                throw new Exception("User with specified friendID not found");
            }
        } catch (SQLException e) {
            System.out.println("SQL error in initiateFriendship() SELECT 2: " + e.getMessage());
        }
        try {
            //Prompt for message
            System.out.print("Enter friend request message: ");
            String message = scanman.nextLine();
            //Confirm you want to send request
            System.out.println("Are you sure you want to send request?(y/n)");
            String option = scanman.nextLine();
            if (option.equals("y")) {
                //Add request to pendingFriend relation
                query = "INSERT INTO pendingFriend VALUES(?, ?, ?)";
                prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                resultSet.beforeFirst();
                resultSet.next();
                prepareStatement.setInt(1, ourID);
                prepareStatement.setInt(2, friendID);
                prepareStatement.setString(3, message);
                prepareStatement.executeUpdate();
                //closeSQL();
            }
        } catch (SQLException e) {
            System.out.println("SQL error in initiateFriendship() INSERT: " + e.getMessage());
        }

        //Check if the tuple was added: (assuming we have an empty pendingF)
        resultSet.beforeFirst();
        int rows_returned = 0;
        while (resultSet.next()) {
            rows_returned++;
        }
        if (rows_returned == 0) {    //Tuple was not added
            System.out.println("Failure in adding request");
        } else if (rows_returned == 1) {   //Tuple added
            System.out.println("Successfully sent request");
        }
    }

    void createGroup(String name, String description, int size) throws SQLException, Exception {
        if (size <= 0) {
            throw new Exception("Group limit must be greater than 0");
        }
        try {
            query = "SELECT COUNT(*) FROM groupInfo WHERE NAME=?";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setString(1, name);
            resultSet = prepareStatement.executeQuery();
            //closeSQL();
        } catch (SQLException e) {
            System.out.println("SQL error in createGroup() SELECT: " + e.getMessage());
        }

        try {
            query = "INSERT INTO groupInfo (gID, name, size, description) VALUES (DEFAULT,?,?,?)";

            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setString(1, name);
            prepareStatement.setInt(2, size);
            prepareStatement.setString(3, description);
            prepareStatement.executeUpdate();

            //statement = connection.createStatement();
            //query = "SELECT name FROM profile where userID=?";
            //resultSet = statement.executeQuery(query);
            // prepareStatement = connection.prepareStatement(query);
            // prepareStatement.setString
        } catch (SQLException e) {
            System.out.println("SQL error in createGroup() INSERT: " + e.getMessage());
        }

        //add group creator as "manager" role
        query = "SELECT gID FROM groupInfo WHERE name = ?";
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        prepareStatement.setString(1, name);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        int gID = -1;
        while(resultSet.next()) {
            gID = resultSet.getInt(1);
        }

        query = "INSERT INTO groupMember VALUES (?, ?, ?)";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setInt(1, gID);
        prepareStatement.setInt(2, ourID);
        prepareStatement.setString(3, "manager");
        prepareStatement.executeUpdate();
    }

    void initiateAddingGroup(int gID, String message) throws SQLException, ClassNotFoundException {
        //Get yourself
        query = "SELECT userID FROM profile WHERE email = ?";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setString(1, ourEmail);
        resultSet = prepareStatement.executeQuery();

        //Add into pendingGroupMember
        query = "INSERT INTO pendingGroupMember VALUES(?,?,?)";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setInt(1, gID);
        resultSet.next();
        prepareStatement.setInt(2, ourID);
        prepareStatement.setString(3, message);
        prepareStatement.executeUpdate();
    }

    void confirmRequests() throws SQLException {
        // check friendship requests first
        System.out.println("Pending friend requests:");
        System.out.println("[userID] - [message]");
        query = "SELECT * FROM pendingFriend WHERE toID = ?";
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        prepareStatement.setInt(1, ourID);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        while(resultSet.next()) {
            System.out.println(resultSet.getInt("fromID") + " - " + resultSet.getString("message"));
            System.out.println("What would you like to do? (Enter 0 to EXIT)");
            System.out.println("1 - [Accept Request]\n" +
                               "2 - [Reject Request]\n" +
                               "3 - [View Next Request]");
            System.out.print("Enter number: ");
            int input = scanman.nextInt();
            switch(input) {
                case 0:
                    return;
                case 1:
                    // accept
                    query = "INSERT INTO friend VALUES (?, ?, ?, ?)";
                    prepareStatement = connection.prepareStatement(query);
                    prepareStatement.setInt(1, resultSet.getInt("fromID"));
                    prepareStatement.setInt(2, ourID);
                    prepareStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                    prepareStatement.setString(4, resultSet.getString("message"));
                    prepareStatement.executeUpdate();
                    break;
                case 2:
                    // reject
                    query = "DELETE FROM pendingFriend WHERE fromID = ? AND toID = ?";
                    prepareStatement = connection.prepareStatement(query);
                    prepareStatement.setInt(1, resultSet.getInt("fromID"));
                    prepareStatement.setInt(2, ourID);
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid input.");
                    break;
            }
        }

        // now check group requests - find all groups where current user is 'manager'
        System.out.println("Pending group requests:");
        System.out.println("[groupID] - [userID] - [message]");
        query = "SELECT gID FROM groupMember WHERE userID = ? AND role = 'manager'";
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        prepareStatement.setInt(1, ourID);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        while(resultSet.next()) {
            query = "SELECT * FROM pendingGroupMember WHERE gID = ?";
            int gID = resultSet.getInt(1);
            prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            prepareStatement.setInt(1, gID);
            ResultSet rs2 = prepareStatement.executeQuery();
            rs2.beforeFirst();
            while(rs2.next()) {
                System.out.println(gID + " - " + rs2.getInt("userID") + " - " + rs2.getString("message"));
                System.out.println("What would you like to do? (Enter 0 to EXIT)");
                System.out.println("1 - [Accept Request]\n" +
                        "2 - [Reject Request]\n" +
                        "3 - [View Next Request]");
                System.out.print("Enter number: ");
                int input = scanman.nextInt();
                switch(input) {
                    case 0:
                        return;
                    case 1:
                        // accept
                        query = "INSERT INTO groupMember VALUES (?, ?, 'member')";
                        prepareStatement = connection.prepareStatement(query);
                        prepareStatement.setInt(1, gID);
                        prepareStatement.setInt(2, rs2.getInt("userID"));
                        prepareStatement.executeUpdate();
                        break;
                    case 2:
                        // reject
                        query = "DELETE FROM pendingGroupMember WHERE gID = ? AND userID = ?";
                        prepareStatement = connection.prepareStatement(query);
                        prepareStatement.setInt(1, gID);
                        prepareStatement.setInt(2, rs2.getInt("userID"));
                        prepareStatement.executeUpdate();
                        break;
                    case 3:
                        break;
                    default:
                        System.out.println("Invalid input.");
                        break;
                }
            }
        }
    }

    void sendMessageToUser(int friendID) throws SQLException, ClassNotFoundException {
        //Display name of recipient
        query = "SELECT name FROM profile WHERE userID = ?";
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        prepareStatement.setInt(1, friendID);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        resultSet.next();
        System.out.println("Recipient" + resultSet.getString("Name"));

        //Prompt for message(can be multi-lined) and
        //Allow user to type message
        System.out.println("Message:");
        String message = scanman.nextLine();

        //add to messageInfo relation
        query = "INSERT INTO messageInfo VALUES(DEFAULT,?,?,?,DEFAULT,now())";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setInt(1, ourID);
        prepareStatement.setString(2, message);
        prepareStatement.setInt(3, friendID);
        prepareStatement.executeUpdate();
        //Use trigger to add to messengerecipient relation automatic
        //succ/failure feedback
        resultSet.beforeFirst();
        int rows_returned = 0;
        while (resultSet.next()) {
            rows_returned++;
        }
        if (rows_returned == 0) {    //Tuple was not added
            System.out.println("Failure in sending message");
        } else if (rows_returned == 1) {   //Tuple added
            System.out.println("Successfully sent message");
        }
    }
    /*
    In sendMessageToGroup, a message can only be sent to a group of
     which the logged-in user is a member.
     */

    void sendMessageToGroup(int groupId) throws SQLException, Exception {
        try {
            String groupName = "";
            query = "SELECT Name FROM groupInfo where gID = ?";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, groupId);
            resultSet = prepareStatement.executeQuery();

            if (!resultSet.next()) {
                throw new Exception("No group found");
            } else {
                groupName = resultSet.getString("name");
                //closeSQL();
            }
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter message:");
            String message = scanman.nextLine();

            query = "INSERT INTO messageInfo(msgID, fromID, message, toUserID, toGroupID, timeSent)" +
                    "VALUES(DEFAULT,?,?,?,?,now())";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, ourID);
            prepareStatement.setString(2, message);
            prepareStatement.setNull(3, java.sql.Types.INTEGER);
            prepareStatement.setInt(4, groupId);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL error in sendMessageToGroup(): " + e.getMessage());
        }
    }

    /*
    In displayMessages and displayNewMessages,
    group messages sent to the
    logged-in user should also be considered.
     */
    void displayMessages() throws SQLException, Exception {
        try {
            // don't need query to get own userID, it's already saved
            // closeSQL();

            query = "SELECT M.MSGID, M.FROMID, M.MESSAGE, M.TOGROUPID, M.TOUSERID,M.timesent\n"
                    + "FROM messageInfo M \n"
                    + "INNER JOIN MESSAGERECIPIENT MR ON MR.MSGID=M.MSGID\n"
                    + "WHERE MR.USERID=?";

            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, ourID);
            resultSet = prepareStatement.executeQuery();

            System.out.println("\nMessages:\n"
                    + "[RECORD#] [Name], [Message],[timeSent]");

            int counter = 1;
            while (resultSet.next()) {
                System.out.println("Record " + counter + ": "
                        + resultSet.getString(1) + ", "
                        + resultSet.getString(2) + ", "
                        + resultSet.getString(3));
                counter++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void displayNewMessages() throws SQLException, Exception {
        try {
            Timestamp lastlogin1 = null;

            query = "SELECT lastlogin from profile where userID=?";
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, ourID);
            resultSet = prepareStatement.executeQuery();

            if (!resultSet.next()) {
                throw new Exception("Error");
            } else {
                lastlogin1 = resultSet.getTimestamp(1);
                // closeSQL();
            }

            if (lastlogin1 == null) {
                query = "SELECT p.Name as Name,\n"
                        + " p.EMAIL as email,\n"
                        + " m.message as message,\n"
                        + " m.timeSent as timeSent\n"
                        + "from messageInfo m, profile p\n"
                        + "where toUserID=? \n"
                        + "and p.userId = m.fromID\n"
                        + "ORDER by timeSent";
            } else {
                query = "SELECT p.Name as Name,\n"
                        + " p.EMAIL as email,\n"
                        + " m.message as message,\n"
                        + " m.timeSent as timeSent\n"
                        + "from messageInfo m, profile p\n"
                        + "where toUserID=? \n"
                        + "and p.userId = m.fromID\n"
                        + "and m.timeSent > ? \n"
                        + "ORDER by timeSent";
            }
            prepareStatement = connection.prepareStatement(query);

            if (lastlogin1 == null) {
                prepareStatement.setInt(1, ourID);
                prepareStatement.setNull(2, Types.TIMESTAMP);
            } else {
                prepareStatement.setInt(1, ourID);
                prepareStatement.setTimestamp(2, lastlogin1);
            }

            resultSet = prepareStatement.executeQuery();
            System.out.println("\nMessages:\n"
                    + "[RECORD#] [Name], [Message],[timeSent]");

            int counter = 1;
            while (resultSet.next()) {
                System.out.println("Record " + counter + ": "
                        + resultSet.getString(1) + ", "
                        + resultSet.getString(2) + ", "
                        + resultSet.getString(3));
                counter++;
            }
        } finally {
            // closeSQL();
        }
    }

    /*
    In sendMessageToGroup, a message can only be sent
    to a group of which the logged-in user is a member.
     */
    void displayFriends() throws SQLException, ClassNotFoundException {
        //Print out all friends names and user ids
        query = "SELECT name,userID2 FROM friend JOIN profile ON (userID1 = userID OR userID2 = userID) WHERE userID = ?";
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        prepareStatement.setInt(1, ourID);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        while (resultSet.next()) {
            System.out.println("Friend Name: " + resultSet.getString("name"));
            System.out.println("Friend userID" + resultSet.getString("userID2") + "\n");
        }
        //Access friends profile by inputting uid
        //or
        //go to main menu with 0
        boolean flag = true;
        while (flag) {
            System.out.println("Enter userID to view profile. (Use 0 to exit)");
            int input = scanman.nextInt();
            //Print friends profile and ask for another uid
            if (input != 0) {
                query = "SELECT * FROM profile WHERE userID = ?";
                prepareStatement = connection.prepareStatement(query);
                prepareStatement.setInt(1, input);
                resultSet = prepareStatement.executeQuery();
                while (resultSet.next()) {
                    System.out.println("UserID: " + resultSet.getInt("userID"));
                    System.out.println("Name: " + resultSet.getString("name"));
                    System.out.println("Email: " + resultSet.getString("email"));
                    System.out.println("Date of Birth: " + resultSet.getDate("date_of_birth"));
                }
            } else {
                flag = false;
                //Return to main menu
            }
        }
    }

    void searchForUser(String searcher) throws SQLException, Exception {
        try {
            int userID = 0;

            String[] search = searcher.split("\\s+");
            System.out.println("Search length = " + search.length);

            StringBuilder builder = new StringBuilder();
            builder.append("SELECT userID, name, email FROM PROFILE WHERE ");
            for (int x = 0; x < search.length; x++) {
                if (x == 0) {
                    builder.append("(");
                } else {
                    builder.append("Or (");
                }
                builder.append("name LIKE '%").append(search[x]).append("%'");
                builder.append(" OR email LIKE '%").append(search[x]).append("%'");
                builder.append(") ");
            }
            builder.append(";");
            System.out.println(builder.toString());
            prepareStatement = connection.prepareStatement(builder.toString());
            resultSet = prepareStatement.executeQuery();

            System.out.println("\nSearch results:\n" + "[RECORD#]: [ID], [NAME], [EMAIL]");
            int count = 1;

            while (resultSet.next()) {
                System.out.println("Record " + count + ": "
                        + resultSet.getString(1) + ", "
                        + resultSet.getString(2) + ", "
                        + resultSet.getString(3));
                count++;
            }
        } finally {
            //closeSQL();
        }
    }

    void topMessages(int x, int k) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -(x));
        java.util.Date currentDate = cal.getTime();
        java.sql.Date nextDate = new java.sql.Date(currentDate.getTime());
        try {
            query = "SELECT *\n" +
                    "FROM (\n" +
                    "         SELECT CASE\n" +
                    "                    WHEN sCount.touserID IS NULL\n" +
                    "                        THEN rCount.fromid\n" +
                    "                    ELSE sCount.touserid\n" +
                    "                    END\n" +
                    "                 ,\n" +
                    "                (\n" +
                    "                        (\n" +
                    "                            CASE\n" +
                    "                                WHEN sCount.countSent IS NULL\n" +
                    "                                    THEN 0\n" +
                    "                                ELSE sCount.countSent\n" +
                    "                                END\n" +
                    "                            )\n" +
                    "                        + (\n" +
                    "                            CASE\n" +
                    "                                WHEN rCount.countReceived IS NULL\n" +
                    "                                    THEN 0\n" +
                    "                                ELSE rCount.countReceived\n" +
                    "                                END\n" +
                    "                            )\n" +
                    "                    ) AS RScount\n" +
                    "         FROM (\n" +
                    "                  SELECT m.fromid, count(mr.MSGID) AS countReceived\n" +
                    "                  FROM messagerecipient mr\n" +
                    "                           INNER JOIN messageinfo m ON mr.MSGID = m.MSGID\n" +
                    "                  WHERE m.timesent > now() - INTERVAL '1 MONTH' * ?\n" +
                    "                    AND mr.userid = ?\n" +
                    "                  GROUP BY m.fromid\n" +
                    "              ) rCount\n" +
                    "                  FULL OUTER JOIN\n" +
                    "              (\n" +
                    "                  SELECT m.touserid, count(m.MSGID) AS countSent\n" +
                    "                  FROM messageInfo m\n" +
                    "                  WHERE m.timesent > now() - INTERVAL '1 MONTH' * ?\n" +
                    "                    AND m.fromid = ?\n" +
                    "                    AND touserid IS NOT NULL\n" +
                    "                  GROUP BY m.touserid\n" +
                    "              ) sCount\n" +
                    "              ON rCount.fromid = sCount.touserid\n" +
                    "         ORDER BY RScount DESC\n" +
                    "     ) t1\n" +
                    "LIMIT ?";
            statement = connection.createStatement();
            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setInt(1, x);
            prepareStatement.setInt(2, ourID);
            prepareStatement.setInt(3, x);
            prepareStatement.setInt(4, ourID);
            prepareStatement.setInt(5, k);
            resultSet = prepareStatement.executeQuery();

            System.out.println("[userid] - [total message count]");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("touserid") + " - "
                        + resultSet.getInt("rscount"));
            }
        } catch (SQLException e) {
            System.out.println("Error." + e.toString());
        }
    }

    void logout() throws SQLException, ClassNotFoundException {

        Connection connection = connectToDB();
        if (!loggedIn) {
            System.out.println("Error: not logged in");
            return;
        }
        loggedIn = false;
        query = "UPDATE profile SET lastlogin = now() WHERE userid = ?";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setInt(1, ourID);
        prepareStatement.executeUpdate();

        System.out.println("Successfully logged out.");
    }

    //NOTE: unsure on this, I'm assuming dropUser is supposed to delete the currently logged in user
    void dropUser() throws SQLException, ClassNotFoundException {
        connection = connectToDB();
        logout();
        query = "DELETE FROM profile WHERE userid = ?";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setInt(1, ourID);
        prepareStatement.executeUpdate();
    }

    void exit() {
        System.exit(0);
    }

    void cleanup() throws SQLException, ClassNotFoundException {
        connection = connectToDB();
        query = "DELETE FROM profile WHERE name = ?";
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setString(1, "JDBC_TEST");
        prepareStatement.executeUpdate();
    }

    void threeDegrees(int searchID) throws Exception, SQLException {
        //Get yourself is unnecessary, we already have our own ID saved from login

        Vector<Integer> hopsID = new Vector<Integer>();
        Vector<Integer> path = new Vector<Integer>();
        path.add(ourID);       //Add yourself to the path

        // first hop - get all friendships with self
        query = "SELECT userID1, userID2 FROM friend WHERE userID1 = ? OR userID2 = ?";
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        prepareStatement.setInt(1, ourID);
        prepareStatement.setInt(2, ourID);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        while(resultSet.next()) {
            //check if searchID found to exit early
            if(resultSet.getInt("userID1") == searchID || resultSet.getInt("userID2") == searchID) {
                path.add(searchID);
                System.out.println("Found path within 3 hops");
                for (int i = 0; i < path.size(); i++) {
                    System.out.println(path.elementAt(i));
                }
                return;
            }

            //otherwise, add friendship to vector
            if(resultSet.getInt("userID1") == ourID) {
                hopsID.add(resultSet.getInt("userID2"));
            } else {
                hopsID.add(resultSet.getInt("userID1"));
            }
        }

        // second hop - find friendships from friends in first hop
        HashMap<Integer, Integer> hopsID2 = new HashMap<Integer, Integer>();
        query = "SELECT userID1, userID2 FROM friend WHERE userID1 = ? OR userID2 = ?";
        for(int friendID : hopsID) {
            prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            prepareStatement.setInt(1, friendID);
            prepareStatement.setInt(2, friendID);
            resultSet = prepareStatement.executeQuery();
            resultSet.beforeFirst();
            while(resultSet.next()) {
                //check if searchID found to exit early
                if(resultSet.getInt("userID1") == searchID || resultSet.getInt("userID2") == searchID) {
                    path.add(friendID);
                    path.add(searchID);
                    System.out.println("Found path within 3 hops");
                    for (int i = 0; i < path.size(); i++) {
                        System.out.println(path.elementAt(i));
                    }
                    return;
                }

                //otherwise, add friendship to vector
                if(resultSet.getInt("userID1") == friendID) {
                    hopsID2.put(resultSet.getInt("userID2"), friendID);
                } else {
                    hopsID2.put(resultSet.getInt("userID1"), friendID);
                }
            }
        }

        //third hop - find friendships from second-hop users
        query = "SELECT userID1, userID2 FROM friend WHERE userID1 = ? OR userID2 = ?";
        for(int friendID : hopsID2.keySet()) {
            prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            prepareStatement.setInt(1, friendID);
            prepareStatement.setInt(2, friendID);
            resultSet = prepareStatement.executeQuery();
            resultSet.beforeFirst();
            while(resultSet.next()) {
                //check if searchID found to exit early
                if(resultSet.getInt("userID1") == searchID || resultSet.getInt("userID2") == searchID) {
                    path.add(hopsID2.get(friendID));
                    path.add(friendID);
                    path.add(searchID);
                    System.out.println("Found path within 3 hops");
                    for (int i = 0; i < path.size(); i++) {
                        System.out.println(path.elementAt(i));
                    }
                    return;
                }
                // no need to add to a third vector here, since we won't be saving anything for another hop
            }
        }

        //if we reach here, no path was found in 3 hops
        System.out.println("No relationship exists within 3 hops.");
    }

    void displayTable(String tableName) throws SQLException {
        /* technically vulnerable to SQL injection, but the table name can't be parameterized
         * by PreparedStatement and we're only performing selection here for the driver program
         */
        System.out.println("\nCurrent contents of " + tableName + ":");
        query = "SELECT * FROM " + tableName;
        prepareStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        resultSet = prepareStatement.executeQuery();
        resultSet.beforeFirst();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int cols = rsmd.getColumnCount();
        for(int i=1; i<=cols; i++) {
            System.out.print(rsmd.getColumnName(i) + " ");
        }
        System.out.println();
        while(resultSet.next()) {
            for(int j=1; j<=cols; j++) {
                System.out.print(resultSet.getObject(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
