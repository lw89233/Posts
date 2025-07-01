package pl.edu.uws.lw89233;

import pl.edu.uws.lw89233.managers.DatabaseManager;
import pl.edu.uws.lw89233.managers.EnvManager;
import pl.edu.uws.lw89233.managers.MessageManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Posts {

    private final int PORT = Integer.parseInt(EnvManager.getEnvVariable("POSTS_MICROSERVICE_PORT"));
    private final String DB_HOST = EnvManager.getEnvVariable("DB_HOST");
    private final String DB_PORT = EnvManager.getEnvVariable("DB_PORT");
    private final String DB_NAME = EnvManager.getEnvVariable("DB_NAME");
    private final String DB_USER = EnvManager.getEnvVariable("DB_USER");
    private final String DB_PASSWORD = EnvManager.getEnvVariable("DB_PASSWORD");
    private final DatabaseManager dbManager = new DatabaseManager(DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD);

    public void startService() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Posts microservice is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting Posts microservice: " + e.getMessage());
        }
    }

    private class ClientHandler extends Thread {

        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();
                if (request != null && request.contains("send_post_request")) {
                    String response = handleSendPost(request);
                    out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Error handling client request: " + e.getMessage());
            }
        }

        private String handleSendPost(String request) {
            MessageManager responseManager = new MessageManager(request);
            String content = responseManager.getAttribute("post");
            String login = responseManager.getAttribute("login");
            String message_id = responseManager.getAttribute("message_id");
            String response = "type:send_post_response#message_id:" + message_id + "#";

            if (content == null || content.isBlank() || login == null || login.isBlank()) {
                response += "status:400#";
                return response;
            }

            try {
                String getUserSql = "SELECT id FROM users WHERE login = ?";
                try (PreparedStatement getUserStmt = dbManager.getConnection().prepareStatement(getUserSql)) {
                    getUserStmt.setString(1, login);
                    ResultSet rs = getUserStmt.executeQuery();

                    if (!rs.next()) {
                        response += "status:400#";
                        return response;
                    }

                    int userId = rs.getInt("id");

                    String insertPostSql = "INSERT INTO posts (user_id, content) VALUES (?, ?)";
                    try (PreparedStatement insertPostStmt = dbManager.getConnection().prepareStatement(insertPostSql)) {
                        insertPostStmt.setInt(1, userId);
                        insertPostStmt.setString(2, content);

                        int rowsAffected = insertPostStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            response += "status:200#";
                        } else {
                            response += "status:400#";
                        }
                        return response;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error during sending post: " + e.getMessage());
                return "type:send_post_response#message_id:%s#status:400#".formatted(message_id);
            }
        }
    }

    public static void main(String[] args) {
        new Posts().startService();
    }
}