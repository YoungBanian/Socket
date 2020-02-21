package server;

import java.io.*;
import java.net.Socket;

public class ChatHandler implements Runnable {

    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 存储新用户
            server.addClient(socket);
            // 读取用户信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = null;
            while((message = reader.readLine()) != null) {
                String forwardMessage = "客户端[" + socket.getPort() + "]: " + message + "\n";
                System.out.print(forwardMessage);
                // 转发消息给其他客户端
                server.forwardMessage(socket, forwardMessage);
                if (server.isQuit(message)) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
