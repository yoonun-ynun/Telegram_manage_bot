import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class tagSearch {
    HashSet<Integer> search(String category, String tag) throws IOException {
        category = category.replaceAll(" ", "");
        boolean is_n = !category.equals("artist") && !category.equals("type");
        String gender = "";
        String language = "all";
        if(category.equals("female") || category.equals("male")){
            gender = category + ":";
            category = "tag";
        }
        if(category.equals("language")){
            category = "";
            language = tag;
            tag = "index";
        }
        tag = tag.replaceAll("_", " ");
        tag = tag.replaceAll(" ", "%20");
        String addr = "https://ltn.hitomi.la";
        if(is_n)
            addr = addr + "/n";
        if(!language.equals("all")) {
            addr = addr + "/index-" + language + ".nozomi";
        }else {
            addr = addr + "/" + category + "/" + gender + tag + "-" + language + ".nozomi";
        }

        URL url = new URL(addr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        ByteBuffer buffer;
        try (InputStream in = con.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024 * 8];
            int length = 0;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            buffer = ByteBuffer.wrap(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HashSet<Integer> list = new HashSet<>();
        long limit = buffer.limit()/4;
        for(int i = 0;i<limit;i++){
            list.add(buffer.getInt(i*4));
        }
        return list;

    }
}
