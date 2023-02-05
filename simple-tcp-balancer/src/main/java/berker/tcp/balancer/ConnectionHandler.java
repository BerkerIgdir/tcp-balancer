package berker.tcp.balancer;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHandler implements Runnable {

    private final Socket client;
    private final BlockingQueue<RegisteredServer> registeredServers;

    public ConnectionHandler(Socket clientSocket, BlockingQueue<RegisteredServer> registeredServers) {
        this.client = clientSocket;
        this.registeredServers = registeredServers;
    }

    @Override
    public void run() {
        try {
            var inputStream = client.getInputStream();
            var serverToRedirectPojo = registeredServers.take();
            var servertoRedirectSocket = new Socket(serverToRedirectPojo.url(), serverToRedirectPojo.port());
            var serverToRedirectOutputStream = servertoRedirectSocket.getOutputStream();
            var buffer = new byte[2048];
            var clientInputStream = client.getInputStream();
            var sentBytes = clientInputStream.read(buffer, 0, inputStream.available());
            System.out.println("number of redirected bytes is " + sentBytes);

            servertoRedirectSocket.getOutputStream().write(Arrays.copyOfRange(buffer, 0, sentBytes));
            serverToRedirectOutputStream.flush();

            synchronized (this) {
                var atomicBool = new AtomicBoolean(false);
                Thread.ofVirtual().start(() -> {
                    try {
                        notifier(servertoRedirectSocket.getInputStream(), atomicBool);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                wait(500);
                atomicBool.set(true);
                if (servertoRedirectSocket.getInputStream().available() <= 0) {
                    throw new RuntimeException("TIMEOUT EXCEPTION");
                }
            }

            var serverToRedirectSocketInputStream = servertoRedirectSocket.getInputStream();
            var readBytes = serverToRedirectSocketInputStream.read(buffer, 0, serverToRedirectSocketInputStream.available());

            client.getOutputStream().write(Arrays.copyOfRange(buffer, 0, readBytes));
            client.getOutputStream().flush();
            client.close();
            servertoRedirectSocket.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void notifier(InputStream inputStream, AtomicBoolean atomicBoolean) throws IOException, InterruptedException {
        while (inputStream.available() <= 0 || !atomicBoolean.get()) {

        }
        notifyAll();
    }


}
