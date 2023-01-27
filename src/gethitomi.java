
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class gethitomi {

    static int count = 0;
    void getimage(String key, int page_number, File file) throws Exception{
        //이미지를 가져올 주소
        String URL = "https://hitomi.la/reader/" + key + ".html#1";

        //이미지의 해시값을 가져옴
        URL url = new URL("https://ltn.hitomi.la/galleries/" + key + ".js");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        JSONObject info = new JSONObject(sb.toString().substring(18));
        String hash = info.getJSONArray("files").getJSONObject(page_number-1).getString("hash");

        //중간에 들어갈 주소 구하기 hash의 마지막 글자 3개를 2 0 1 순으로 배치한 것을 16진수로 취급하고 10진수로 변환
        String postfix = hash.substring(hash.length()-3);
        int secret = Integer.parseInt(String.valueOf(postfix.charAt(2)) + postfix.charAt(0) + postfix.charAt(1), 16);

        //gethitomisub에서 중간에 들어갈 주소가 a 인지 b인지 구함 참고: https://ltn.hitomi.la/gg.js
        String hitomiurl = new gethitomisub().check(secret) + "/" + secret +"/" +  hash + ".webp" ;

        url = new URL(hitomiurl);
        con = (HttpURLConnection) url.openConnection();

        //헤더에 리퍼러를 꼭 reader 주소로 해야함 리퍼러 설정하지 않을 시 403 리턴
        con.setRequestProperty("Referer", URL);
        con.setRequestMethod("GET");

        //이미지 저장
        InputStream is = con.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(file);

        final int BUFFER_SIZE = 4096;
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = is.read(buffer)) != -1){
            outputStream.write(buffer, 0, bytesRead);
        }
        count++;

        is.close();
        outputStream.close();

    }
}
