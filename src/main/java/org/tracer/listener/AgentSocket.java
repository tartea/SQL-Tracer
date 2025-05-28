package org.tracer.listener;

import org.tracer.handler.ParamHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 监听器，用于变更全局变量
 */
public class AgentSocket {

    public static void start() {

        if (ParamHandler.isServerEnable()) {
            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(ParamHandler.getServerPort())) {
                    System.out.println("sql tracer server started on port " + ParamHandler.getServerPort());

                    while (!serverSocket.isClosed()) {
                        Socket socket = serverSocket.accept();
                        new Thread(() -> handleConnection(socket)).start();
                    }
                } catch (IOException e) {
                    System.err.println("sql tracer server stopped: " + e.getMessage());
                }
            }).start();
        }

    }

    /**
     * 动态修改参数
     * 目前支持日志是否在控制台输出
     * 格式：key1=value1,key2=value2
     *
     * @param socket
     */
    private static void handleConnection(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] pairs = line.split(",");
                for (String pair : pairs) {
                    String[] entry = pair.split("=", 2); // 最多分割成两部分
                    if (entry.length == 2) {
                        String key = entry[0].trim();
                        String value = entry[1].trim();
                        if ("outputToConsole".equals(key)) {
                            ParamHandler.setOutputToConsole(value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("sql tracer server Error handling connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
