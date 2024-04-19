/*
 * Copyright (c) 2024.  Jerome David. Univ. Grenoble Alpes.
 * This file is part of DcissChatService.
 *
 * DcissChatService is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * DcissChatService is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.uga.miashs.dciss.chatservice.common;

//import jdk.javadoc.internal.doclint.Messages;
import fr.uga.miashs.dciss.chatservice.server.UserMsg;

import java.util.List;

import java.sql.*;  // Import required packages
import java.util.Map;

public class ConnexionBDD {
    private Connection cnx;

    public void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp?useSSL=false", "root", "");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    public void executeQueries() {
        String query1 = "SELECT * FROM users";
        String query2 = "SELECT * FROM messages";

        try {
            Statement statement = cnx.createStatement();
            ResultSet resQuery1 = statement.executeQuery(query1);
            ResultSet resQuery2 = statement.executeQuery(query2);

            // Code to process the results goes here
            // For example:
            while (resQuery1.next()) {
                String users = resQuery1.getString("username");
                System.out.println(users);

            }
            while (resQuery2.next()) {
                String messages = resQuery2.getString("content");
                System.out.println(messages);
            }

            // Close result sets and statement
            resQuery1.close();
            resQuery2.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    public void insertUser(Map<Integer, UserMsg> users) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";

        try {
            PreparedStatement pstmt = cnx.prepareStatement(query);
            for (Map.Entry<Integer, UserMsg> entry : users.entrySet()) {
                UserMsg u = entry.getValue();
                pstmt.setString(1, u.getName());
                // Assuming getPassword() method exists in UserMsg class
                pstmt.setInt(1, u.getId());
                pstmt.setString(2, u.getName());
                //pstmt.addBatch();
                pstmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    public void deleteUser(){
        String query = "TRUNCATE TABLE users";
        try {
            PreparedStatement pstmt = cnx.prepareStatement(query);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    public void insertMessages(List<Message> messages) {
        String query = "INSERT INTO messages (user_id, dest_id, content) VALUES (?, ?, ?)";

        try {
            PreparedStatement pstmt = cnx.prepareStatement(query);

            for (Message message : messages) {
                pstmt.setInt(1, message.getUserId());
                pstmt.setInt(2, message.getDestId());
                pstmt.setString(3, message.getContent());
                //pstmt.addBatch(); // Ajouter l'insertion à un lot pour un traitement plus efficace
                pstmt.execute();
            }

            pstmt.executeBatch(); // Exécuter le lot d'insertions

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    public void deleteMessages() {
        String query = "TRUNCATE TABLE messages";
        try {
            PreparedStatement pstmt = cnx.prepareStatement(query);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}
