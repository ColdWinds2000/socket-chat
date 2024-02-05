package org.sqs.socketchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * 通信服务端
 */
public class ChatServer {
    //定义端口号
    private static final int PORT = 8080;
    //定义聊天处理器,将每个客户端存入处理,用Set不会重复
    private static Set<ChatHandler> handlers = new HashSet<>();

    public static void main(String[] args) {
        //监听端口号
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("聊天服务端正在此端口上运行：" + PORT);

            //开始循环监听
            while (true) {
                //1.服务器调用 serverSocket.accept() 方法：该方法会一直阻塞，直到有客户端请求连接到服务器。
                //2.客户端发起连接请求： 当有客户端发起连接请求时，serverSocket.accept() 会返回一个新的 Socket 对象，该对象代表与客户端建立的连接。
                //3.创建新的 Socket 对象： 服务器通过 serverSocket.accept() 创建一个新的 Socket 对象，该对象包含了客户端的信息，包括客户端的地址和端口等。
                //4.处理客户端连接： 服务器可以使用返回的 Socket 对象与客户端进行通信，发送和接收数据
                Socket clientSocket = serverSocket.accept();
                System.out.println("新的客户端已连接 " + clientSocket);

                //将此socket客户端连接存入Set当中
                ChatHandler handler = new ChatHandler(clientSocket, handlers);
                handlers.add(handler);

                //创一个新线程来处理此客户端的消息。建议：新线程可以用线程池来管理,固定1线程的线程池或者调整线程池策略
                Thread handlerThread = new Thread(handler);
                handlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast消息给所有客户端。广播
    public static void broadcast(String message, ChatHandler sender) {
        for (ChatHandler handler : handlers) {
            // 避免将消息发回给发送者
            if (handler != sender) {
                handler.sendMessage(message);
            }
        }
    }

    // 发送私密消息给指定客户端
    public static void sendPrivateMessage(String message, String targetClient, ChatHandler sender) {
        for (ChatHandler handler : handlers) {
            // 找到目标客户端并发送私密消息
            if (handler.getUsername().equals(targetClient)) {
                handler.sendMessage(sender.getUsername() + " (私密): " + message);
                return;
            }
        }

        // 如果目标客户端不存在，通知发送者
        sender.sendMessage("用户 '" + targetClient + "' 找不到.");
    }
}
