package connector;

import processor.ServletProcessor;
import processor.StaticProcessor;

import javax.servlet.ServletException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NIOConnector implements Runnable {

    private static final int DEFAULT_PORT = 8888;

    private ServerSocketChannel server;
    private Selector selector;

    private int port;

    public NIOConnector() {
        this(DEFAULT_PORT);
    }

    public NIOConnector(int port) {
        this.port = port;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
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
                for(SelectionKey key : selectionKeys) {
                    handles(key);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException, ServletException {
        // ACCEPT事件
        if (key.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        }
        // READ事件
        else {
            SocketChannel client = (SocketChannel) key.channel();
            // 解除当前请求的selectionKey与selector的关系，避免被selector轮询到从而报错.
            key.cancel();
            client.configureBlocking(true);
            Socket clientSocket = client.socket();
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            Request request = new Request(input);
            request.parse();

            Response response = new Response(output);
            response.setRequest(request);

            if (request.getRequestURI().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticProcessor processor = new StaticProcessor();
                processor.process(request, response);
            }
            close(client);
        }
    }

    public void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
