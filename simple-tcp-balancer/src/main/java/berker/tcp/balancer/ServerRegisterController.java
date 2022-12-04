package berker.tcp.balancer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class ServerRegisterController {
    private static final int DEFAULT_PORT = 8089;
    private static final String POST_METHOD = "POST";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<RegisteredServer> serverSet = Collections.synchronizedSet(new LinkedHashSet<>());

    private final HttpServer httpServer;

    public ServerRegisterController() {
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerRegisterController(int port) {
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startServer(){
        createContext();
        httpServer.start();
    }

    private void createContext(){
        httpServer.createContext("/register",new RegisterHandler());
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!POST_METHOD.equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(404, 0);
            }
            RegisteredServer serializedObject = objectMapper.readValue(exchange.getRequestBody(), RegisteredServer.class);
            serverSet.add(serializedObject);
        }
    }
}
