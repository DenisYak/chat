package ru.geekbrains;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private ServerSocket serverSocket;
    private Vector<ClientHandler> clients; // коллекция с синхронизацией при многопоточном обращении

    public Server () {
        try {
            SQLHandler.connect();
            serverSocket = new ServerSocket(8189); // создан сервер который слушает порт 8189
            clients = new Vector<ClientHandler>();
            System.out.println("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept(); // блокирующая операция (ожидание подключения)
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            SQLHandler.disconnect();
        }
    }

    public void broadcastMsg (ClientHandler client, String msg) {
        String outMsg = client.getNick() + ": " + msg;
        for (ClientHandler o: clients) {
            o.sendMsg(outMsg);
        }
    }

    public void subscribe (ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe (ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }
}
