import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Hitomizip implements Runnable{
    String key;
    long chat_id;
    Hitomizip(String key, long chat_id){this.key = key; this.chat_id = chat_id;}
    public void run() {
        try {
            Action ac = new Action();

            URL url = new URL("https://ltn.hitomi.la/galleries/" + key + ".js");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }catch(FileNotFoundException e){
                ac.SendMessage(chat_id, "일치하는 번호가 없습니다.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject info = new JSONObject(sb.toString().substring(18));
            int length = info.getJSONArray("files").length();

            ExecutorService service = Executors.newFixedThreadPool(7);
            int message_id = ac.SendMessage(chat_id, "0/" + length + " 개 다운로드 완료");
            final int[] count = {0};

            Files.createDirectories(Paths.get(System.getProperty("user.dir") + "/hitomi/" + key));

            for (int i = 1; i <= length; i++) {
                int num = i;
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        for(int retries = 0;;retries++) {
                            try {
                                new gethitomi().getimage(key, num, new File(System.getProperty("user.dir") + "/hitomi/" + key + "/", num + ".webp"));
                                count[0]++;
                                break;
                            } catch (Exception e) {

                            }
                        }
                    }
                };
                service.submit(run);
            }
            service.shutdown();
            int TIME_count = 0;
            int private_count = 0;
            while (!service.awaitTermination(2, TimeUnit.SECONDS)){
                if(TIME_count>300){
                    ac.Edittext(chat_id,message_id, "다운로드 실패 cause: 시간초과");
                    service.shutdownNow();
                    return;
                }
                if(private_count != count[0]){
                    try {
                        ac.Edittext(chat_id, message_id, count[0] + "/" + length + "개 다운로드 완료");
                    }catch (Exception e){
                        System.out.println("Http 429에러 채팅 업데이트 건너뜀");
                    }
                    private_count = count[0];
                }
                TIME_count++;
            }

            TimeUnit.SECONDS.sleep(1);
            ac.Edittext(chat_id, message_id, "파일 압축중..");
            File zipfile = new File(System.getProperty("user.dir") + "/hitomi/", key + ".zip");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));

            for(int i = 1;i<=length;i++){
                FileInputStream in = new FileInputStream(new File(System.getProperty("user.dir") + "/hitomi/" + key + "/", i + ".webp"));
                ZipEntry en = new ZipEntry(i + ".webp");
                out.putNextEntry(en);

                final int BUFFER_SIZE = 4096;
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = in.read(buffer)) != -1){
                    out.write(buffer, 0, bytesRead);
                }

                out.closeEntry();
                in.close();
            }
            out.close();

            TimeUnit.SECONDS.sleep(1);
            ac.Edittext(chat_id, message_id, "전송중..");

            long file_size = (zipfile.length()/1024)/1024;
            if(file_size>=50){
                ac.SendMessage(chat_id, "https://" + Main.setting.getString("hostname") + "/hitomi/" + key + ".zip");
            }else {
                ac.SendDocument(chat_id, zipfile);
            }
            ac.Edittext(chat_id, message_id, "전송 완료");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
