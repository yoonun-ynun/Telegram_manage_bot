import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class getDCcon {
    private final String save_path = System.getProperty("user.dir") + "/dccon/"; //디시콘을 저장 할 경로
    private final String number;
    private final JSONObject info;
    private final JSONArray detail;


    public getDCcon(String number, long chat_id){
        //디시콘 정보 가져오기
        Unicodekor kor = new Unicodekor();
        Multipart multi = new Multipart("https://dccon.dcinside.com/index/package_detail");
        multi.setProperty("x-requested-with", "XMLHttpRequest");
        multi.input_text("package_idx", number);
        String result = multi.start().toString();
        if(result.equals("error")){
            new Action().SendMessage(chat_id, "존재하지 않는 디시콘 입니다.");
        }
        JSONObject info = new JSONObject(result).getJSONObject("info");
        JSONArray detail = new JSONObject(result).getJSONArray("detail");

        this.number = number;
        this.info = info;
        this.detail = detail;
    }

    public File saveAll() throws Exception{
            //디시콘 다운로드
            Files.createDirectories(Paths.get(save_path + number));
            int length = detail.length();
            InputStream is;
            FileOutputStream outputStream;
            for(int i = 0;i<length;i++){
                String path = detail.getJSONObject(i).getString("path");
                URL url = new URL("https://dcimg5.dcinside.com/dccon.php?no=" + path);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                is = con.getInputStream();
                String disposition = con.getHeaderField("Content-Disposition");
                String filename = disposition.split("filename=")[1];
                outputStream = new FileOutputStream(save_path + number + "/" + i + filename.substring(filename.lastIndexOf(".")));

                final int BUFFER_SIZE = 4096;
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = is.read(buffer)) != -1){
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return new File(save_path + number);
    }
    public String get_title(){
        Unicodekor kor = new Unicodekor();
        return kor.uniToKor(info.getString("title"));
    }
}
