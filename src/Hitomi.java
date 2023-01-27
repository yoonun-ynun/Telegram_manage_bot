import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class Hitomi implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String request_method = exchange.getRequestMethod();

        if(request_method.equals("GET")){
            exchange.sendResponseHeaders(200, 0);
            System.out.println(exchange.getRequestURI());
            OutputStream response = exchange.getResponseBody();
            Headers responseheader = exchange.getResponseHeaders();
            responseheader.set("Content-Type", "text/html");
            response.write("HelloWorld!!".getBytes());
            response.flush();
            response.close();
        }
    }
}
