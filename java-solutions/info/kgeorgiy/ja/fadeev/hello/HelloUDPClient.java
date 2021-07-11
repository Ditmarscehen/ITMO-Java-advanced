package info.kgeorgiy.ja.fadeev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Invalid number of arguments");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Required not null arguments");
                return;
            }
        }
        String host = args[0], prefix = args[2];
        int port, threads, requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e){
            System.err.println("Failed to parse required numeric values " + e.getMessage());
            return;
        }
        HelloClient client = new HelloUDPClient();
        client.run(host, port, prefix, threads, requests);
    }

    @Override
    @SuppressWarnings("all")
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        ExecutorService service = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads).forEach(thread ->
                service.execute(() -> {
                    try (DatagramSocket socket = new DatagramSocket()) {
                        DatagramPacket response = new DatagramPacket(
                                new byte[socket.getReceiveBufferSize()],
                                socket.getReceiveBufferSize()
                        );
                        for (int j = 0; j < requests; j++) {
                            String requestMsg = prefix + thread + "_" + j;
                            DatagramPacket request = new DatagramPacket(
                                    requestMsg.getBytes(StandardCharsets.UTF_8),
                                    requestMsg.length(),
                                    socketAddress
                            );
                            socket.setSoTimeout(100);
                            String responseMsg = "";
                            while (!responseMsg.equals("Hello, " + requestMsg)) {
                                try {
                                    socket.send(request);
                                    socket.receive(response);
                                } catch (IOException ignored) {
                                }
                                responseMsg = new String(response.getData(), response.getOffset(), response.getLength(), StandardCharsets.UTF_8);
                            }
                            System.out.println(responseMsg);
                        }
                    } catch (SocketException e) {
                        System.err.println("Socket exception occurred " + e.getMessage());
                    }
                }));
        service.shutdownNow();
        try {
            service.awaitTermination((long) 10 * threads * requests, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {

        }
    }
}

