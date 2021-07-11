package info.kgeorgiy.ja.fadeev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HelloUDPServer implements HelloServer {
    private ExecutorService service;
    private DatagramSocket socket;

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Invalid number of arguments");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Required not null arguments");
                return;
            }
        }
        int port, threads;
        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse required numeric values " + e.getMessage());
            return;
        }
        HelloServer server = new HelloUDPServer();
        server.start(port, threads);
    }

    @Override
    public void start(int port, int threads) {
        try {
            service = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);
            for (int i = 0; i < threads; i++) {
                service.submit(() -> {
                    try {
                        DatagramPacket request = new DatagramPacket(
                                new byte[socket.getReceiveBufferSize()],
                                socket.getReceiveBufferSize());
                        while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                            try {
                                socket.receive(request);
                                final String requestMsg = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
                                final String responseMsg = "Hello, " + requestMsg;
                                DatagramPacket response = new DatagramPacket(
                                        responseMsg.getBytes(StandardCharsets.UTF_8),
                                        responseMsg.length(),
                                        request.getSocketAddress());
                                socket.send(response);
                            } catch (SocketException ignored) {
                            }
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }
        } catch (SocketException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        shutdownAndAwaitTermination(service);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
