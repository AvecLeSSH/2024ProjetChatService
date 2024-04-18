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

import java.sql.*;  // Import required packages

public class ConnexionBDD {
    private Connection cnx;

    public void connectToDatabase() {
        try {
            cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp?useSSL=false", "root", "");
        } catch (SQLException e) {
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
    public void insertMessages(List<Message> messages) {
        String query = "INSERT INTO messages (user_id, dest_id, content) VALUES (?, ?, ?)";

        try {
            PreparedStatement pstmt = cnx.prepareStatement(query);

            for (Message message : messages) {
                pstmt.setInt(1, message.getUserId());
                pstmt.setInt(2, message.getDestId());
                pstmt.setString(3, message.getContent());
                pstmt.addBatch(); // Ajouter l'insertion à un lot pour un traitement plus efficace
            }

            pstmt.executeBatch(); // Exécuter le lot d'insertions

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

}
