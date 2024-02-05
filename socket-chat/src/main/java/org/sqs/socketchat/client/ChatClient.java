package org.sqs.socketchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 通信客户端
 */
public class ChatClient {
    public static void main(String[] args) {


        String serverHost = "127.0.0.1";
        int serverPort = 8080;

        try (Socket socket = new Socket(serverHost, serverPort);
             //字符流读取
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             //输出字符流
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             //读取控制台么
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            // 启动接收消息线程
            new Thread(() -> {
                try {
                    while (true) {
                        String message = reader.readLine();
                        if (message == null) {
                            break;
                        }
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 发送用户输入的消息
            while (true) {
                String userInput = consoleReader.readLine();
                //退出链接
                if (userInput.equals("exit")) {
                    break;
                }
                //往服务端输出消息
                writer.println(userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
