import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class SendFile implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String request_method = exchange.getRequestMethod();

        if(request_method.equals("GET")){
            exchange.sendResponseHeaders(200, 0);
            System.out.println(exchange.getRequestURI());
            String URI = exchange.getRequestURI().toString().substring(6);
            File file = new File(System.getProperty("user.dir") + "/file/", URI);
            if(file.exists()){
                FileInputStream in = new FileInputStream(file);
                DataOutputStream response = new DataOutputStream(exchange.getResponseBody());
                exchange.getResponseHeaders().set("Content-length", Long.toString(file.length()));
                int BUFFER_SIZE = 4096;
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1){
                    response.write(buffer, 0, bytesRead);
                }
                response.flush();
                response.close();
                in.close();
            }else {
                OutputStream response = exchange.getResponseBody();
                Headers responseheader = exchange.getResponseHeaders();
                responseheader.set("Content-Type", "text/html");
                response.write("HelloWorld!!".getBytes());
                response.flush();
                response.close();
            }
        }
    }
}
