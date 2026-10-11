import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientServer {
    private static List<String> localOrders = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = 5001;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", new ShopHandler());
        server.createContext("/api/order", new OrderHandler());
        server.createContext("/api/get-orders", new ProvideOrdersHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("🛍️ [CLIENT SERVER] Dang chay tai: http://localhost:" + port);
    }

    static class ShopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("public/index.html");
            if (!file.exists()) {
                String err = "<h1>404 Khong tim thay file public/index.html</h1>";
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

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) { sb.append(line); }
                
                String clientJson = sb.toString();
                String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String orderJsonWithId = clientJson.replace("{", "{\"id\":\"" + orderId + "\",");
                localOrders.add(orderJsonWithId);
                
                System.out.println("--> Khach vua dat hang (ID: " + orderId + ")");

                String response = "{\"status\":\"success\",\"message\":\"Dat hang thanh cong! Ma: " + orderId + "\"}";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }

    static class ProvideOrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < localOrders.size(); i++) {
                json.append(localOrders.get(i));
                if (i < localOrders.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}