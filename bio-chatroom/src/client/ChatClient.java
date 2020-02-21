package client;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_SERVER_PORT = 8888;
    private final String QUIT = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 客户端发送信息
     * @param message 用户输入信息
     * @throws IOException
     */
    public void send(String message) throws IOException {
        if (!socket.isOutputShutdown()) {
            writer.write(message + "\n");
            writer.flush();
        }
    }

    /**
     * 客户端接受服务器信息
     * @return
     * @throws IOException
     */
    public String receive() throws IOException {
        String message = null;
        if (!socket.isInputShutdown()) {
            message = reader.readLine();
        }
        return message;
    }

    public synchronized void close() {
        if (writer != null) {
            try {
                System.out.println("客户端socket关闭");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断用户是否输入退出指令
     * @param message 用户输入信息
     * @return
     */
    public boolean isQuit(String message) {
        return QUIT.equals(message);
    }


    public void start() {
        try {
            // 连接服务器
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
            // 创建IO流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 处理用户输入
            new Thread(new UserInputHandler(this)).start();

            // 读取服务器转发的信息
            String message = null;
            while ((message = receive()) != null) {
                // 接受服务器信息
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

}
