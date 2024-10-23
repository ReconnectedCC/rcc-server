package cc.reconnected.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cc.reconnected.server.RccServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServiceServer {
    private final HttpServer server;

    public HttpServer httpServer() {
        return this.server;
    }

    public ServiceServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(RccServer.CONFIG.httpPort()), 0);
        server.createContext("/tps", new TPSHandler());
        server.createContext("/mspt", new MSPTHandler());
        server.createContext("/player", new PlayerCountHandler());
        server.setExecutor(null);

        var httpThread = new Thread(server::start);
        httpThread.start();
    }

    static class TPSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            var tps = String.valueOf(RccServer.getTPS());
            t.sendResponseHeaders(200, tps.length());
            var body = t.getResponseBody();
            body.write(tps.getBytes());
            body.close();
        }
    }

    static class MSPTHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            var tps = String.valueOf(RccServer.getMSPT());
            t.sendResponseHeaders(200, tps.length());
            var body = t.getResponseBody();
            body.write(tps.getBytes());
            body.close();
        }
    }

    static class PlayerCountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            var tps = String.valueOf(RccServer.getPlayerCount());
            t.sendResponseHeaders(200, tps.length());
            var body = t.getResponseBody();
            body.write(tps.getBytes());
            body.close();
        }
    }
}
