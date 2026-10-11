import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class AdminServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", new AdminDashboardHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("📊 [ADMIN SERVER] Dang chay tai: http://localhost:" + port);
    }

    static class AdminDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("public/admin.html");
            if (!file.exists()) {
                String err = "<h1>404 Khong tim thay file public/admin.html</h1>";
                exchange.sendResponseHeaders(404, err.getBytes().length);
                exchange.getResponseBody().write(err.getBytes());
                exchange.getResponseBody().close();
                return;
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}