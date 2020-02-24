package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public  ChatServer(int port) {
        this.port = port;
    }

    /**
     * 处理服务器接受客户端各种事件
     * @param key
     * @throws IOException
     */
    private void handles(SelectionKey key) throws IOException {
        // ACCEPT 事件
        if (key.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println(getClientName(client) + "已连接");
        }
        // READ 事件
        else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            String message = receive(client);
            if (message.isEmpty()) {
                // 客户端异常
                key.cancel();
                selector.wakeup();
            } else {
                System.out.println(getClientName(client) + "：" + message);
                forwardMessage(client, message);
                if (isQuit(message)) {
                    key.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client) + "已断开");
                }
            }
        }
    }


    /**
     * 启动服务器
     */
    private void start() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();

            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + port);

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    handles(key);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }


    /**
     * 判断客户端是否退出
     * @param message 客户端消息
     * @return
     */
    private boolean isQuit(String message) {
        return QUIT.equals(message);
    }

    /**
     * 转发消息
     * @param client
     * @param message
     */
    private void forwardMessage(SocketChannel client, String message) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel connectedClient = key.channel();
            if (connectedClient instanceof ServerSocketChannel) {
                continue;
            }
            if (key.isValid() && !client.equals(connectedClient)) {
                wBuffer.clear();
                wBuffer.put(charset.encode(getClientName(client) + ":" + message));
                wBuffer.flip();
                while (wBuffer.hasRemaining()) {
                    ((SocketChannel)connectedClient).write(wBuffer);
                }
            }
        }
    }

    /**
     * 接收客户端消息
     * @param client
     * @return
     */
    private String receive(SocketChannel client) throws IOException {
        // 不会真正的删除掉buffer中的数据，只是把position移动到最前面，同时把limit调整为capacity
        rBuffer.clear();
        while (client.read(rBuffer) > 0);
        // 调用flip之后，读写指针指到缓存头部，并且设置了最多只能读出之前写入的数据长度(而不是整个缓存的容量大小)
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }


    private String getClientName(SocketChannel channel) {
        return "客户端[" + channel.socket().getPort() + "]";
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }


}
