import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Action {
    static long num = 0;
    static long scale_num = 0;
    String Token = Main.setting.getString("Telegram_token");

    JSONObject setting = Main.setting;
    String user_path = System.getProperty("user.dir");
    String Address;
    String FileAddress;
    Action(){
        Address = "https://api.telegram.org/bot" + Token + "/";
        FileAddress = "https://api.telegram.org/file/bot" + Token + "/";
    }
    Action(String Token){
        this.Token = Token;
        Address = "https://api.telegram.org/bot" + this.Token + "/";
        FileAddress = "https://api.telegram.org/file/bot" + this.Token + "/";
    }

    int SendMessage(Long id, String text){
        try {
            String Address = this.Address + "sendMessage";
            String data = "chat_id=" + id + "&text=" + text;
            URL url = new URL(Address);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
            bw.write(data);
            bw.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            JSONObject ob = new JSONObject(sb.toString());

            return ob.getJSONObject("result").getInt("message_id");
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    void SendPhoto(Long id, String image){
        try {
            String Address = this.Address + "sendPhoto" + "?chat_id=" + id + "&photo=" + image;
            URL url = new URL(Address);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            InputStream result = con.getInputStream();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    StringBuilder SendPhoto(Long id, File file) throws Exception{
        String Address = this.Address + "sendPhoto";
        Multipart multi = new Multipart(Address);
        String ContentType = "image" + "/" + file.getName().split("\\.")[1];
        multi.input_text("chat_id", Long.toString(id));
        multi.input_file(ContentType, "photo", file);
        return multi.start();
    }

    void SendDocument(long id, File file) throws Exception{
        String Address = this.Address + "sendDocument";
        Multipart multi = new Multipart(Address);
        multi.input_text("chat_id", Long.toString(id));
        multi.input_file("document", "document", file);
        StringBuilder result = multi.start();
    }

    void ChatPermissions
            (Long chat_id, Long mute_id,Boolean can_send_messages, Boolean can_send_media_messages, Boolean can_send_polls, Boolean can_send_other_messages, Boolean can_add_web_page_previews, Boolean can_change_info,Boolean can_invite_users, Boolean can_pin_messages) throws Exception{
        JSONObject sending = new JSONObject();
        sending.append("{can_send_messages:",can_send_messages).append("can_send_media_messages",can_send_media_messages).append("can_send_polls",can_send_polls).append("can_send_other_messages", can_send_other_messages).append("can_add_web_page_previews",can_add_web_page_previews).append("can_change_info",can_change_info).append("can_invite_users",can_invite_users).append("can_pin_messages",can_pin_messages);


        String Address = this.Address + "restrictChatMember?" + "chat_id=" + chat_id + "&user_id=" + mute_id + "&permissions=" + sending;
        Address = Address.replaceAll("[\\[\\[\\]]","");

        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        InputStream result = con.getInputStream();

    }

    JSONObject getChatMember(Long chat_id, Long user_id) throws Exception{

        String Address = this.Address + "getChatMember?" + "chat_id=" + chat_id + "&user_id=" + user_id;
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null){
            sb.append(line).append('\n');
        }
        return new JSONObject(sb.toString());
    }
    JSONObject getChat(String chat_id) throws Exception{
        String Address = this.Address + "getChat?" + "chat_id=" + chat_id;
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null){
            sb.append(line);
        }
        return new JSONObject(sb.toString());
    }
    void Edittext(long chat_id, int message_id, String text) throws Exception{
        String Address = this.Address + "editMessageText?" + "chat_id=" + chat_id + "&message_id=" + message_id + "&text=" + text;
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader result = new BufferedReader(new InputStreamReader(con.getInputStream()));
    }
    void delete_massage(long chat_id, long message_id) throws Exception{
        String Address = this.Address + "deleteMessage?" + "chat_id=" + chat_id + "&message_id=" + message_id;
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        InputStream result = con.getInputStream();
    }

    File getFile(String file_id) throws Exception{
        String Address = this.Address + "getFile?" + "file_id=" + file_id;
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        String line;
        StringBuilder result = new StringBuilder();
        BufferedReader br;

        int status = con.getResponseCode();
        if(status == HttpURLConnection.HTTP_OK){
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = br.readLine()) != null)
                result.append(line).append('\n');
            System.out.println(result);
        }else{
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            while((line = br.readLine()) != null)
                result.append(line).append('\n');
            System.out.println(result);
            return null;
        }

        JSONObject ob = new JSONObject(result.toString());
        String path = ob.getJSONObject("result").getString("file_path");
        String File_Address = FileAddress + path;

        File file = new File(user_path + "/Image/", num + "." + path.split("\\.")[1]);

        File file_dir = new File(file.getParent());

        if(!file_dir.exists()){
            file_dir.mkdir();
        }

        url = new URL(File_Address);
        con = (HttpURLConnection) url.openConnection();

        InputStream is = con.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(file);

        final int BUFFER_SIZE = 4096;
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = is.read(buffer)) != -1){
            outputStream.write(buffer, 0, bytesRead);
        }
        num++;
        is.close();
        outputStream.close();

        return file;
    }



    void start_waifu2x() throws Exception{
        String Address = "https://api.waifu2x.me/api/instance/start";
        Post post = new Post(Address);
        post.input_data("id", setting.getString("waifu_id"));
        post.input_data("apikey", setting.getString("waifu_api"));
        StringBuilder result = post.start();
        System.out.println(result);
    }

    void stop_waifu2x() throws Exception{
        String Address = "https://api.waifu2x.me/api/instance/stop";
        Post post = new Post(Address);
        post.input_data("id", setting.getString("waifu_id"));
        post.input_data("apikey", setting.getString("waifu_api"));
        StringBuilder result = post.start();
        System.out.println(result);
    }

    String status_waifu2x() throws Exception{
        String Address = "https://api.waifu2x.me/api/instance/status" + "?id=" + setting.getString("waifu_id") + "&apikey=" + setting.getString("waifu_api");
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        String line;
        StringBuilder result = new StringBuilder();
        BufferedReader br;

        int status = con.getResponseCode();
        if(status == HttpURLConnection.HTTP_OK){
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = br.readLine()) != null)
                result.append(line).append('\n');
            System.out.println(result);
        }else{
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            while((line = br.readLine()) != null)
                result.append(line).append('\n');
            System.out.println(result);
            return "";
        }
        JSONObject ob = new JSONObject(result.toString());
        return ob.getJSONObject("instance").getString("status");
    }



    File upscaling(String scale, String Style, File file, int num) throws Exception{
        String Address = "https://api.waifu2x.me/api/file/convert";
        Multipart multi = new Multipart(Address);
        multi.input_text("id", setting.getString("waifu_id"));
        multi.input_text("style", Style);
        multi.input_text("noise", "3");
        multi.input_text("scale", scale);
        multi.input_text("comp", "0");
        multi.input_text("apikey", setting.getString("waifu_api"));
        multi.input_file("image/png", "file", file);

        StringBuilder sb = multi.start();
        System.out.println(sb);

        JSONObject ob = new JSONObject(sb.toString());
        String File_Address = ob.getString("src");

        while (!scaling_status(num).equals("success")){
            if(scaling_status(num).equals("failed")){
                return null;
            }
            TimeUnit.SECONDS.sleep(5);
        }

        File result = new File(user_path + "/upscaling/", scale_num + "." + File_Address.split("\\.")[3]);

        File file_dir = result.getParentFile();
        if(!file_dir.exists())
            file_dir.mkdir();


        URL url = new URL(File_Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        InputStream is = con.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(result);

        final int BUFFER_SIZE = 4096;
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = is.read(buffer)) != -1){
            outputStream.write(buffer, 0, bytesRead);
        }
        scale_num++;
        is.close();
        outputStream.close();

        return result;
    }

    String scaling_status(int num) throws Exception{
        String Address = "https://api.waifu2x.me/api/file/list?id="+ setting.getString("waifu_id") +"&apikey=" + setting.getString("waifu_api");
        URL url = new URL(Address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        String line;
        StringBuilder result = new StringBuilder();
        BufferedReader br;

        int status = con.getResponseCode();
        if(status == HttpURLConnection.HTTP_OK){
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = br.readLine()) != null)
                result.append(line).append('\n');
            System.out.println(result);
        }else{
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            while((line = br.readLine()) != null)
                result.append(line).append('\n');
            System.out.println(result);
            return "";
        }
        JSONObject ob = new JSONObject(result.toString());
        return ob.getJSONArray("files").getJSONObject(num).getString("status");
    }

    static void Write_banned(){
        try {
            File save = new File(System.getProperty("user.dir"), "banchat.txt");
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save)));
            } catch (FileNotFoundException E) {
                E.printStackTrace();
                return;
            }
            HashMap<Long, ArrayList<ArrayList<String>>> banned = Command.banned;
            for (Map.Entry<Long, ArrayList<ArrayList<String>>> entry : banned.entrySet()) {
                bw.write(Long.toString(entry.getKey()));
                bw.newLine();
                for(ArrayList<String> texts:entry.getValue()){
                    for(String text : texts) {
                        bw.write(text);
                        bw.newLine();
                    }
                }
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    static HashMap<Long, ArrayList<ArrayList<String>>> Read_banned(){
        HashMap<Long, ArrayList<ArrayList<String>>> result = new HashMap<>();
        try {
            ArrayList<String> banned = new ArrayList<>();
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            File read = new File(System.getProperty("user.dir"), "banchat.txt");
            boolean check = false;
            long id = 0L;
            BufferedReader br = null;
            try{
                br = new BufferedReader(new InputStreamReader(new FileInputStream(read)));
            }catch (FileNotFoundException e){
                return result;
            }
            String input = "";
            int count = 0;
            while((input = br.readLine()) != null){
                if(!input.equals("")){
                    if(!check){
                        id = Long.parseLong(input);
                        check = true;
                    }else{
                        banned.add(input);
                        count++;
                        if(count == 10){
                            list.add(banned);
                            banned = new ArrayList<>();
                            count = 0;
                        }
                    }
                }else{
                    list.add(banned);
                    result.put(id, list);
                    list = new ArrayList<>();
                    banned = new ArrayList<>();
                    count = 0;
                    id = 0L;
                    check = false;
                }
            }
            br.close();
            return result;
        }catch (IOException e) {
            e.printStackTrace();
            return result;
        }
    }
    ArrayList<String> found_banned(ArrayList<String> banned, String text, Boolean equals,Boolean Remove){
        for(String banned_text : banned){
            String check_text = banned_text.toLowerCase();
            text = text.toLowerCase();
            if(!equals){
                if(String_Contain(text, check_text)){
                    if(Remove){
                        banned.remove(banned_text);
                        return banned;
                    }else{
                        return banned;
                    }
                }
            }else{
                if(text.equals(check_text)){
                    if(Remove){
                        banned.remove(banned_text);
                        return banned;
                    }else{
                        return banned;
                    }
                }
            }
        }
        return null;
    }
    boolean String_Contain(String text1, String text2){
        String[] text_arr = text2.split("");
        for (String s : text_arr) {
            if (!text1.contains(s)) {
                return false;
            }
        }
        return true;
    }
}