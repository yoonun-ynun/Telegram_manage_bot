import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;

public class Telegram implements HttpHandler{
    static HashMap<Long, String> check = new HashMap<>();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String request_method = exchange.getRequestMethod();
        if(request_method.equals("GET")){
            OutputStream response = exchange.getResponseBody();
            Headers responseheader = exchange.getResponseHeaders();
            exchange.sendResponseHeaders(200, 0);
            responseheader.set("Content-Type", "text/html");
            responseheader.set("Connection", "keep-alive");
            response.write("HelloWorld!!".getBytes());
            response.flush();
            response.close();
        }
        if(request_method.equals("POST")){
            try {
                JSONObject ob = new JSONObject();
                ob.put("code", 1);
                ob.put("message", "ok");
                Headers responseheader = exchange.getResponseHeaders();
                OutputStream response = exchange.getResponseBody();
                responseheader.set("Content-Type", "application/json");
                responseheader.set("Connection", "keep-alive");
                exchange.sendResponseHeaders(200, ob.toString().length());
                response.write(ob.toString().getBytes());
                response.flush();

                Action ac = new Action();

                StringBuilder sb = new StringBuilder();

                String line;
                BufferedReader request = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                while ((line = request.readLine()) != null) {
                    sb.append(line);
                }
                System.out.println(sb);
                request.close();
                response.close();
                JSONObject jObject = new JSONObject(sb.toString());
                Command cmd = new Command(jObject);
                long key;
                long usage_id = jObject.getJSONObject("message").getJSONObject("from").getLong("id");
                if(jObject.has("edited_message")){
                    System.out.println("check");
                    key = jObject.getJSONObject("edited_message").getLong("message_id");
                    String message = jObject.getJSONObject("edited_message").getString("text");
                    long chat_id = jObject.getJSONObject("edited_message").getJSONObject("chat").getLong("id");
                    String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
                    if(!(status.equals("creator") || status.equals("administrator"))){
                        cmd.check_banned(message, key, chat_id);
                    }
                }
                key = jObject.getJSONObject("message").getLong("message_id");
                long chat_id = jObject.getJSONObject("message").getJSONObject("chat").getLong("id");
                System.out.println(key);
                if(jObject.getJSONObject("message").has("sticker")){
                    System.out.println("check");
                    String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
                    if(!(status.equals("creator") || status.equals("administrator"))) {
                        String unique_id = jObject.getJSONObject("message").getJSONObject("sticker").getJSONObject("thumb").getString("file_unique_id");
                        String set_name = jObject.getJSONObject("message").getJSONObject("sticker").getString("set_name");
                        cmd.check_sticker_ban(unique_id, set_name, chat_id, key);
                    }
                }
                if(check.get(usage_id) != null){
                    String command = check.get(usage_id).split(" ")[0];
                    if(command.equals("sticker")){
                        String unique_id = jObject.getJSONObject("message").getJSONObject("sticker").getJSONObject("thumb").getString("file_unique_id");
                        cmd.ban_sticker(unique_id, chat_id, usage_id);
                    }
                    if(command.equals("set")){
                        String set_name = jObject.getJSONObject("message").getJSONObject("sticker").getString("set_name");
                        cmd.ban_sticker_set(set_name, chat_id, usage_id);
                    }
                    if(command.equals("un_sticker")){
                        String unique_id = jObject.getJSONObject("message").getJSONObject("sticker").getJSONObject("thumb").getString("file_unique_id");
                        cmd.unban_sticker(unique_id, chat_id, usage_id);
                    }
                    if(command.equals("un_set")){
                        String set_name = jObject.getJSONObject("message").getJSONObject("sticker").getString("set_name");
                        cmd.unban_sticker_set(set_name, chat_id, usage_id);
                    }
                    if(command.equals("scaling")){
                        String file_id = "";
                        if(jObject.getJSONObject("message").has("document")) {
                            file_id = jObject.getJSONObject("message").getJSONObject("document").getString("file_id");
                        }else if(jObject.getJSONObject("message").has("photo")){
                            file_id = jObject.getJSONObject("message").getJSONArray("photo").getJSONObject(3).getString("file_id");
                        }
                        int scale = Integer.parseInt(check.get(usage_id).split(" ")[2]);
                        if(scale > 10){
                            ac.SendMessage(chat_id, "스케일은 1~10 사이에서 설정 가능합니다.");
                        }
                        cmd.Upscaling(chat_id, file_id, check.get(usage_id).split(" ")[1], check.get(usage_id).split(" ")[2]);
                    }
                    check.remove(usage_id);
                }
                String message = jObject.getJSONObject("message").getString("text");
                try {
                    JSONArray jArray = jObject.getJSONObject("message").getJSONArray("entities");
                    JSONObject obj = jArray.getJSONObject(0);
                    String type = obj.getString("type");
                    if (type.equals("bot_command")) {
                        String command = message.split(" ")[0];
                        if (command.equals("/hitomi"))
                            cmd.sendHitomi(message.split(" ")[1]);

                        if (command.equals("/mute"))
                            cmd.mute(message.split(" ")[1]);
                        if (command.equals("/unmute"))
                            cmd.unmute(message.split(" ")[1]);
                        if (command.equals("/getinfo")) {
                            cmd.getChat(message.split(" ")[1]);
                        }
                        if (command.equals("/gethitomi"))
                            cmd.sendHitomiZip(message.split(" ")[1]);
                        if (command.equals("/banchat")) {
                            cmd.banChat(message.substring(9));
                            return;
                        }
                        if (command.equals("/unbanchat"))
                            cmd.unbanChat(message.substring(11));
                        if(command.equals("/getbanchat"))
                            cmd.banned_list();
                        if(command.equals("/bansticker")){
                            check.put(usage_id, "sticker");
                        }
                        if(command.equals("/banset")){
                            check.put(usage_id, "set");
                        }
                        if(command.equals("/unbansticker")){
                            check.put(usage_id, "un_sticker");
                        }
                        if(command.equals("/unbanset")){
                            check.put(usage_id, "un_set");
                        }
                        if(command.equals("/upscaling")){
                            check.put(usage_id, "scaling " + message.substring(11));
                        }

                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
                if(!(status.equals("creator") || status.equals("administrator"))){
                    cmd.check_banned(message, key, chat_id);
                }
                JSONObject userdata = jObject.getJSONObject("message").getJSONObject("from");
                cmd.Saveinfo(userdata);

                System.out.println();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
