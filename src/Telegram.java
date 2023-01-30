import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;

public class Telegram implements HttpHandler{
    static HashMap<Long, String> check = new HashMap<>();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String request_method = exchange.getRequestMethod();
        //"일반적으로 사이트를 접속했을때"
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

        //"텔레그램 웹훅 수신"
        if(request_method.equals("POST")){
            try {
                //Response
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

                //Webhook 입력
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
                Command cmd = new Command();

                //기본 정보
                long chat_id;
                long usage_id;
                long user_id = 0;
                String message = "";
                String status = "";
                long key;

                //메시지 수정일 경우
                if(jObject.has("edited_message")){
                    key = jObject.getJSONObject("edited_message").getLong("message_id");
                    message = jObject.getJSONObject("edited_message").getString("text");
                    chat_id = jObject.getJSONObject("edited_message").getJSONObject("chat").getLong("id");
                    usage_id = jObject.getJSONObject("edited_message").getJSONObject("from").getLong("id");
                    cmd.check_banned(message, key, chat_id, usage_id);
                    return;
                }else{ //아닐경우
                    key = jObject.getJSONObject("message").getLong("message_id");
                    chat_id = jObject.getJSONObject("message").getJSONObject("chat").getLong("id");
                    usage_id = jObject.getJSONObject("message").getJSONObject("from").getLong("id");
                    if(jObject.getJSONObject("message").has("text")){
                        message = jObject.getJSONObject("message").getString("text");
                    }
                }
                System.out.println(key);

                //밴 되어있는 메시지인지 확인
                if(jObject.getJSONObject("message").has("sticker")){
                    String unique_id = jObject.getJSONObject("message").getJSONObject("sticker").getJSONObject("thumb").getString("file_unique_id");
                    String set_name = jObject.getJSONObject("message").getJSONObject("sticker").getString("set_name");
                    cmd.check_sticker_ban(unique_id, set_name, chat_id,usage_id,  key);
                }else{
                    cmd.check_banned(message, key, chat_id, usage_id);
                }

                //명령어 사용 후 입력받을 때
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
                        cmd.Upscaling(chat_id, file_id, check.get(usage_id).split(" ")[1], check.get(usage_id).split(" ")[2]);
                    }
                    check.remove(usage_id);
                }


                //명령어 사용시
                if(jObject.getJSONObject("message").has("entities")){
                    JSONArray jArray = jObject.getJSONObject("message").getJSONArray("entities");
                    JSONObject obj = jArray.getJSONObject(0);
                    String type = obj.getString("type");
                    //mute를 사용할 경우의 맨션할 사용자 id 추출
                    if(!jArray.isNull(1)){
                        String mention_type = jArray.getJSONObject(1).getString("type");
                        if(mention_type.equals("text_mention")){
                            user_id = jArray.getJSONObject(1).getJSONObject("user").getLong("id");
                        }else if(mention_type.equals("mention")){
                            user_id = Command.info.get(chat_id).getUserid(message.split(" ")[1].replaceAll("@", ""));
                        }
                    }
                    if (type.equals("bot_command")) {
                        String command = message.split(" ")[0];
                        if (command.equals("/hitomi"))
                            cmd.sendHitomi(chat_id ,message.split(" ")[1]);

                        if (command.equals("/mute"))
                            cmd.mute(user_id, chat_id, usage_id,message.split(" ")[1]);
                        if (command.equals("/unmute"))
                            cmd.unmute(user_id, chat_id, usage_id, message.split(" ")[1]);
                        if (command.equals("/getinfo")) {
                            cmd.getChat(chat_id ,message.split(" ")[1]);
                        }
                        if (command.equals("/gethitomi"))
                            cmd.sendHitomiZip(chat_id, message.split(" ")[1]);
                        if (command.equals("/banchat")) {
                            cmd.banChat(chat_id, usage_id, message.substring(9));
                            return;
                        }
                        if (command.equals("/unbanchat"))
                            cmd.unbanChat(chat_id, usage_id, message.substring(11));
                        if(command.equals("/getbanchat"))
                            cmd.banned_list(chat_id);
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
                }


                JSONObject userdata = jObject.getJSONObject("message").getJSONObject("from");
                cmd.Saveinfo(chat_id, userdata);

                System.out.println();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
