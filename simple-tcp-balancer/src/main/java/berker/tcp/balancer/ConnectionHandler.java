package berker.tcp.balancer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

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
            var serverToRedirect = registeredServers.take();
            var clientToAppServer = new Socket(serverToRedirect.url(), serverToRedirect.port());
            var clientToAppOutputStream = clientToAppServer.getOutputStream();
            int readByte = 0;
            readByte = inputStream.read();
            while (readByte != -1) {
                clientToAppOutputStream.write(readByte);
                readByte = inputStream.read();
            }
            clientToAppOutputStream.flush();

            var responseInputStream = clientToAppServer.getInputStream();
            var clientOutputStream = client.getOutputStream();
            var responseByte = responseInputStream.read();
            while (responseByte != -1) {
                clientOutputStream.write(responseByte);
                responseByte = responseInputStream.read();
            }
            clientOutputStream.flush();

            client.close();
            clientToAppServer.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}