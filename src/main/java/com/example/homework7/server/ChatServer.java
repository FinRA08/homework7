package com.example.homework7.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;//сервис аутаиндификации
    private final List<ClientHandler> clients;//поле содержит перечень клиентов

    public ChatServer() {
        this.authService = new SimpleAuthService();
        this.clients = new ArrayList<>();//инициализировали поле для клиентов

        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true){
               final Socket socket = serverSocket.accept();
               new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNickBusy(String nick){
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick)){
                return true;
            }
        }
        return false;
    }

    public void subscribe(ClientHandler client){
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client){
        clients.remove(client);
    }

    public void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }
}
