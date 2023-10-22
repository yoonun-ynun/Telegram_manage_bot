import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class indexingH {

    HashSet<Integer> indexing(HashSet<Integer>[] lists){
        HashSet<Integer> result = lists[0];
        for(int i = 1;i<lists.length;i++){
            Iterator<Integer> iterator = result.iterator();
            while (iterator.hasNext()) {
                int key = iterator.next();
                if (!lists[i].contains(key)) {
                    iterator.remove();
                }
            }
        }
        return result;
    }
    HashMap<Integer, String> getTitle(int page, HashSet<Integer> result){
        HashMap<Integer, String> index = new HashMap<>();
        int count = 0;
        for (int key : result) {
            count++;
            if(count>(page-1)*6) {
                String title = getTitle(key);
                index.put(key, title);
            }
            if(count == page*6)
                break;
        }
        return index;
    }

    private String getTitle(int key) {
        try {
            String Addr = "https://ltn.hitomi.la/galleries/"+ key + ".js";
            URL url = new URL(Addr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            JSONObject info = new JSONObject(sb.toString().replaceAll("var galleryinfo = ", ""));
            return info.getString("title");
        }catch (IOException e){
            e.printStackTrace();
        }
        return "error";
    }
}
