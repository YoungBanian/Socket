package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";
    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;
    private ExecutorService executorService;

    public ChatServer() {
        executorService = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<>();
    }

    /**
     * 添加客户端
     * @param socket 被添加的客户端socket
     * @throws IOException
     */
    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port, writer);
            System.out.println("客户端[" + port + "]已连接服务器");
        }
    }

    /**
     * 移除客户端
     * @param socket 需要移除的客户端socket
     * @throws IOException
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
            }
            connectedClients.remove(socket.getPort());
            System.out.println("客户端[" + socket.getPort() + "]已断开连接");
        }
    }

    /**
     * 判断客户端是否发出退出指令
     * @param message
     * @return
     */
    public boolean isQuit(String message) {
        return message.equals(QUIT);
    }

    public synchronized void forwardMessage(Socket socket, String message) throws IOException {
        for (Integer port : connectedClients.keySet()) {
            if (!port.equals(socket.getPort())) {
                Writer writer = connectedClients.get(port);
                writer.write(message);
                writer.flush();
            }
        }
    }

    /**
     * 服务器关闭需要关闭的东西
     */
    public synchronized void close() {
        if (connectedClients != null && connectedClients.size() != 0) connectedClients.clear();
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("服务器已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务器启动初始化
     */
    public void start() {
        try {
            // 绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);
            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建handler
//                new Thread(new ChatHandler(this, socket)).start();
                executorService.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }


}
