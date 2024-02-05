package org.sqs.socketchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

/**
 * 消息处理
 */
public class ChatHandler implements Runnable{
    //socket客户端
    private Socket clientSocket;
    //客户端集
    private Set<ChatHandler> handlers;
    //缓冲输入字符流
    private BufferedReader reader;
    //输出字符流
    private PrintWriter writer;
    //添加用户名字段
    private String username;

    /**
     * 消息处理
     * @param socket
     * @param handlers
     */
    public ChatHandler(Socket socket, Set<ChatHandler> handlers) {
        this.clientSocket = socket;
        this.handlers = handlers;

        try {
            //输入流与输出流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            // 为每个客户端生成一个唯一的用户名（可根据实际需求修改）这里用的是当前时间,小伙伴可以用UUID来代替都行
            this.username = "User" + System.currentTimeMillis();
            sendMessage("您的客户端连接名为 " + username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取用户名
    public String getUsername() {
        return username;
    }

    /**
     * 开启消息处理
     */
    @Override
    public void run() {
        try {
            while (true) {
                //读取消息
                String message = reader.readLine();
                //消息非空校验
                if (message == null) {
                    break;
                }

                // 解析私密消息格式（示例：@targetUser message）
                if (message.startsWith("@")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length == 2) {
                        ChatServer.sendPrivateMessage(parts[1], parts[0].substring(1), this);
                        continue;
                    }
                }

                // 广播消息给所有客户端
                ChatServer.broadcast(username + ": " + message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 异常之后移除处理器
            handlers.remove(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}
