package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserInputHandler implements Runnable {


    private ChatClient client;

    public UserInputHandler(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = reader.readLine();
                // 向服务器发送消息
                client.send(input);
                // 检测是否用户主动退出
                if (client.isQuit(input)) break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
