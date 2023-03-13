import org.checkerframework.checker.units.qual.A;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Command {
    static ArrayList<Thread> download = new ArrayList<>();
    static HashMap<Long, Chatinfo> info = new HashMap<>();
    static HashMap<Long, ArrayList<ArrayList<String>>> banned = Action.Read_banned();
    static boolean waifu_on = false;
    static Queue<ArrayList<String>> upscale_list = new LinkedList<>();
    static int upscale_count = 0;
    static boolean check = false;
    void sendHitomi(long chat_id, String number){
        try {
            File file = new File(System.getProperty("user.dir") + "/hitomi/", "hitomi.webp");
            File file_dir = file.getParentFile();
            if(!file_dir.exists())
            if(!file_dir.exists())
                file_dir.mkdir();

            Action action = new Action();

            String address = "https://hitomi.la/reader/" + number + ".html";
            System.out.println(address);

            try {
                gethitomi get = new gethitomi();
                get.getimage(number, 1,file);
            }catch (FileNotFoundException e){
                action.SendMessage(chat_id, "일치하는 번호가 없습니다.");
                return;
            }

            action.SendMessage(chat_id, address);
            action.SendPhoto(chat_id, file);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sendHitomiZip(long chat_id, String number)throws Exception{
        Thread th = new Thread(new Hitomizip(number, chat_id));
        th.start();
        download.add(th);
    }

    void mute(long user_id ,long chat_id, long usage_id, String name) throws Exception{
        Action ac = new Action();

        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        String mute_status = ac.getChatMember(chat_id, user_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        if(mute_status.equals("creator") || mute_status.equals("administrator")){
            ac.SendMessage(chat_id, "관리자 권한 이상의 등급을 가진 유저는 뮤트가 불가능합니다.");
            return;
        }

        ac.ChatPermissions(chat_id, user_id, false, false, false, false, false, false, true,false);

        ac.SendMessage(chat_id, new Unicodekor().uniToKor(name) + "님을 뮤트하였습니다.");
    }

    void unmute(long user_id, long chat_id, long usage_id, String name) throws Exception {
        Action ac = new Action();

        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }

        ac.ChatPermissions(chat_id, user_id, true, true, true, true, true, true, true, true);
        ac.SendMessage(chat_id, new Unicodekor().uniToKor(name) + "님을 뮤트 해제하였습니다.");
    }

    void getChat(long chat_id, String name) throws Exception{
        Action ac = new Action();
        JSONObject object = ac.getChat(name);
        System.out.println(object.toString());
    }
    void Saveinfo(long chat_id, JSONObject data){
        long user_id = data.getLong("id");
        if(!data.has("username"))
            return;
        String user_name = data.getString("username");
        if(Command.info.get(chat_id) == null) {
            Chatinfo info = new Chatinfo();
            info.saveUserid(user_name, user_id);
            Command.info.put(chat_id, info);
        }
    }
    void banChat(long chat_id,long usage_id, String text) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        if(banned.get(chat_id) == null){
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            ArrayList<String> ban_list = new ArrayList<>();
            ban_list.add(text);
            list.add(ban_list);
            banned.put(chat_id, list);
        }else{
            ArrayList<ArrayList<String>> list = banned.get(chat_id);
            if(list.get(list.size()-1).size() >= 10){
                ArrayList<String> ban_list = new ArrayList<>();
                ban_list.add(text);
                list.add(ban_list);
            }else{
                ArrayList<String> ban_list = list.get(list.size()-1);
                ban_list.add(text);
                list.remove(list.size()-1);
                list.add(ban_list);
            }
            banned.remove(chat_id);
            banned.put(chat_id, list);
        }
        Action.Write_banned();
        ac.SendMessage(chat_id, "성공");
    }
    void unbanChat(long chat_id, long usage_id, String text) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        final boolean[] check = {false};
        final int[] count = {0};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size());
        for (int i = 0; i < ban_list.size(); i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    int num = count[0];
                    ArrayList<String> list = ac.found_banned(ban_list.get(count[0]++), text, true, true);
                    if(list != null) {
                        ban_list.remove(num);
                        if(!list.isEmpty()) {
                            ban_list.add(num, list);
                        }
                        check[0] = true;
                    }
                }
            };
            service.submit(run);
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){
            if(check[0]){
                service.shutdownNow();
                break;
            }
        }
        banned.remove(chat_id);
        if(!ban_list.isEmpty()){
            banned.put(chat_id, ban_list);
        }
        ac.SendMessage(chat_id, "성공");
        Action.Write_banned();
    }
    void check_banned(String text, long message_id, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if((status.equals("creator") || status.equals("administrator"))){
            return;
        }
        final boolean[] check = {false};
        final int[] count = {0};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        if(ban_list == null)
            return;
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size());
        for (int i = 0; i < ban_list.size(); i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> list = ac.found_banned(ban_list.get(count[0]++), text, false, false);
                    if(list != null)
                        check[0] = true;
                }
            };
            service.submit(run);
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){
            if(check[0]){
                service.shutdownNow();
                break;
            }
        }
        if(check[0])
            ac.delete_massage(chat_id, message_id);
    }
    void banned_list(long chat_id){
        ArrayList<ArrayList<String>> list = banned.get(chat_id);
        Action ac = new Action();
        StringBuilder sb = new StringBuilder();
        sb.append("금지어 목록").append("\n");
        sb.append("chat id: ").append(chat_id).append("\n");
        try{
            for(ArrayList<String> banned_list:list){
                for(String banned_text : banned_list)
                    sb.append(banned_text).append("\n");
            }
        }catch (NullPointerException e){
            ac.SendMessage(chat_id, "금지어가 없습니다.");
        }
        System.out.println(sb.toString());
        ac.SendMessage(chat_id, sb.toString());
    }

    void ban_sticker(String unique_id, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        if(banned.get(chat_id) == null){
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            ArrayList<String> ban_list = new ArrayList<>();
            ban_list.add(unique_id);
            list.add(ban_list);
            banned.put(chat_id, list);
        }else{
            ArrayList<ArrayList<String>> list = banned.get(chat_id);
            if(list.get(list.size()-1).size() >= 10){
                ArrayList<String> ban_list = new ArrayList<>();
                ban_list.add(unique_id);
                list.add(ban_list);
            }else{
                ArrayList<String> ban_list = list.get(list.size()-1);
                ban_list.add(unique_id);
                list.remove(list.size()-1);
                list.add(ban_list);
            }
            banned.remove(chat_id);
            banned.put(chat_id, list);
        }
        Action.Write_banned();
        ac.SendMessage(chat_id, "성공");
    }
    void ban_sticker_set(String set_name, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        if(banned.get(chat_id) == null){
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            ArrayList<String> ban_list = new ArrayList<>();
            ban_list.add(set_name);
            list.add(ban_list);
            banned.put(chat_id, list);
        }else{
            ArrayList<ArrayList<String>> list = banned.get(chat_id);
            if(list.get(list.size()-1).size() >= 10){
                ArrayList<String> ban_list = new ArrayList<>();
                ban_list.add(set_name);
                list.add(ban_list);
            }else{
                ArrayList<String> ban_list = list.get(list.size()-1);
                ban_list.add(set_name);
                list.remove(list.size()-1);
                list.add(ban_list);
            }
            banned.remove(chat_id);
            banned.put(chat_id, list);
        }
        Action.Write_banned();
        ac.SendMessage(chat_id, "성공");
    }
    void unban_sticker(String unique_id, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        final boolean[] check = {false};
        final int[] count = {0};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size());
        for (int i = 0; i < ban_list.size(); i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    int num = count[0];
                    ArrayList<String> list = ac.found_banned(ban_list.get(count[0]++), unique_id, true, true);
                    if(list != null) {
                        ban_list.remove(num);
                        if(!list.isEmpty()) {
                            ban_list.add(num, list);
                        }
                        check[0] = true;
                    }
                }
            };
            service.submit(run);
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){
            if(check[0]){
                service.shutdownNow();
                break;
            }
        }
        banned.remove(chat_id);
        if(!ban_list.isEmpty()){
            banned.put(chat_id, ban_list);
        }
        ac.SendMessage(chat_id, "성공");
        Action.Write_banned();
    }
    void unban_sticker_set(String set_name, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        final boolean[] check = {false};
        final int[] count = {0};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size());
        for (int i = 0; i < ban_list.size(); i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    int num = count[0];
                    ArrayList<String> list = ac.found_banned(ban_list.get(count[0]++), set_name, true, true);
                    if(list != null) {
                        ban_list.remove(num);
                        if(!list.isEmpty()) {
                            ban_list.add(num, list);
                        }
                        check[0] = true;
                    }
                }
            };
            service.submit(run);
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){
            if(check[0]){
                service.shutdownNow();
                break;
            }
        }
        banned.remove(chat_id);
        if(!ban_list.isEmpty()){
            banned.put(chat_id, ban_list);
        }
        ac.SendMessage(chat_id, "성공");
        Action.Write_banned();
    }
    boolean check_sticker_ban(String unique_id, String set_name, long chat_id,long usage_id, long message_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if((status.equals("creator") || status.equals("administrator"))){
            return true;
        }
        final boolean[] check = {false};
        final int[] count = {0};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        if(ban_list == null)
            return false;
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size()*2);
        for (int i = 0; i < ban_list.size(); i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> list = ac.found_banned(ban_list.get(count[0]), unique_id, true, false);
                    if(list != null)
                        check[0] = true;
                }
            };
            service.submit(run);
            run = new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> list = ac.found_banned(ban_list.get(count[0]), set_name, true, false);
                    if(list != null)
                        check[0] = true;
                }
            };
            service.submit(run);
            count[0]++;
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){
            if(check[0]){
                service.shutdownNow();
                break;
            }
        }
        if(check[0]) {
            ac.delete_massage(chat_id, message_id);
            return true;
        }
        return false;
    }

    void dccon(long user_id,String chat_id,String number){
        try {
            getDCcon dccon = new getDCcon(number);
            convwebm webm = new convwebm();
            Action ac = new Action();
            String title = dccon.get_title();
            File locate = dccon.saveAll();
            File[] list = locate.listFiles();

            int resume = ac.SendMessage(Long.parseLong(chat_id), "진행중...0/" + (list.length/3 + 3));
            int count = 0;

            String path = System.getProperty("user.dir") + "/webm/" + title + "/";
            Files.createDirectories(Paths.get(path));
            for (int i = 0; i < list.length; i++) {
                if(i % 3 == 0){
                    ac.Edittext(Long.parseLong(chat_id), resume, "진행중..." + ++count + "/" + (list.length/3 + 3));
                }
                System.out.println(i + "번째 시작");
                webm.convert(list[i], new File(path + i + ".webm"));
                System.out.println(i + "번째 완료");
            }

            ac.createNewStickerSet(Long.toString(user_id), number, title, new File(System.getProperty("user.dir") + "/webm/" + title + "/0.webm"), "\uD83C\uDF5E");

            ac.Edittext(Long.parseLong(chat_id), resume, "진행중..." + ++count + "/" + (list.length/3 + 3));
            for (int i = 1; i < list.length; i++) {
                ac.addStickerToSet(Long.toString(user_id), number, new File(System.getProperty("user.dir") + "/webm/" + title + "/" + i + ".webm"), "\uD83C\uDF5E");
            }

            ac.Edittext(Long.parseLong(chat_id), resume, "진행완료..." + (list.length/3+3) + "/" + (list.length/3 + 3));
            String first_path = ac.getStickerSet("dccon_num" + number + "_by_" + Main.setting.getString("bot_username").replaceAll("@", "")).getJSONObject("result").getJSONArray("stickers")
                    .getJSONObject(0).getString("file_id");
            ac.sendSticker(chat_id, first_path);

        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            e.printStackTrace();
            new Action().SendMessage(Long.parseLong(chat_id), "에러가 발생하였습니다.\n" + sStackTrace);
        }
    }


    void ban_photo(String[] unique_ids, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        for(String unique_id : unique_ids) {
            if (banned.get(chat_id) == null) {
                ArrayList<ArrayList<String>> list = new ArrayList<>();
                ArrayList<String> ban_list = new ArrayList<>();
                ban_list.add(unique_id);
                list.add(ban_list);
                banned.put(chat_id, list);
            } else {
                ArrayList<ArrayList<String>> list = banned.get(chat_id);
                if (list.get(list.size() - 1).size() >= 10) {
                    ArrayList<String> ban_list = new ArrayList<>();
                    ban_list.add(unique_id);
                    list.add(ban_list);
                } else {
                    ArrayList<String> ban_list = list.get(list.size() - 1);
                    ban_list.add(unique_id);
                    list.remove(list.size() - 1);
                    list.add(ban_list);
                }
                banned.remove(chat_id);
                banned.put(chat_id, list);
            }
        }
        Action.Write_banned();
        ac.SendMessage(chat_id, "성공");
    }

    void unban_photo(String[] unique_ids,long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if(!(status.equals("creator") || status.equals("administrator"))){
            ac.SendMessage(chat_id, "관리자 이상의 등급만 사용할 수 있습니다.");
            return;
        }
        final int[] count = {0};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size() * unique_ids.length);
        for(int j = 0;j<unique_ids.length;j++) {
            for (int i = 0; i < ban_list.size(); i++) {
                int finalJ = j;
                int finalI = i;
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        int num = count[0];
                        ArrayList<String> list = ac.found_banned(ban_list.get(finalI), unique_ids[finalJ], true, true);
                        if (list != null) {
                            ban_list.remove(num);
                            if (!list.isEmpty()) {
                                ban_list.add(num, list);
                            }
                        }
                    }
                };
                service.submit(run);
            }
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){

        }
        banned.remove(chat_id);
        if(!ban_list.isEmpty()){
            banned.put(chat_id, ban_list);
        }
        ac.SendMessage(chat_id, "성공");
        Action.Write_banned();
    }

    void check_photo_ban(String[] unique_ids, long message_id, long chat_id, long usage_id) throws Exception{
        Action ac = new Action();
        String status = ac.getChatMember(chat_id, usage_id).getJSONObject("result").getString("status");
        if((status.equals("creator") || status.equals("administrator"))){
            return;
        }
        final boolean[] check = {false};
        ArrayList<ArrayList<String>> ban_list = banned.get(chat_id);
        if(ban_list == null)
            return;
        ExecutorService service = Executors.newFixedThreadPool(ban_list.size() * unique_ids.length);
        for(int j = 0;j<unique_ids.length;j++) {
            for (int i = 0; i < ban_list.size(); i++) {
                int finalJ = j;
                int finalI = i;
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<String> list = ac.found_banned(ban_list.get(finalI), unique_ids[finalJ], true, false);
                        if (list != null)
                            check[0] = true;
                    }
                };
                service.submit(run);
            }
        }
        service.shutdown();
        while (!service.awaitTermination(10, TimeUnit.MILLISECONDS)){
            if(check[0]){
                service.shutdownNow();
                break;
            }
        }
        if(check[0])
            ac.delete_massage(chat_id, message_id);
    }
}
