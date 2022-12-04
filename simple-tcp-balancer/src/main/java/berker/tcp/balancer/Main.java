package berker.tcp.balancer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static final ServerSocket server;

    private static final BlockingQueue<RegisteredServer> serverQueue = new LinkedBlockingQueue<>(100);
    private static final int DEFAULT_TCP_SOCKET = 8099;



    static {
        try {
            server = new ServerSocket(DEFAULT_TCP_SOCKET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var httpController = new ServerRegisterController();
        var httpThread = Thread.ofVirtual().start(httpController::startServer);
        var tcpThread =  Thread.ofVirtual().start(Main::tcpBalancerLoop);
        //Will be replaced by wait and notify mech.
        httpThread.join();
        tcpThread.join();
    }

    private static void tcpBalancerLoop()  {
        //True will be replaced by a real bool variable
        while (true) {
            Socket client = null;
            try {
                client = server.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var handlerThread = Thread.ofVirtual().start(new ConnectionHandler(client, serverQueue));
        }
    }

}