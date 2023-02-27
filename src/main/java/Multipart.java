import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

public class Multipart {
    Multipart(String Address){
        try{
            URL url = new URL(Address);
            connection = (HttpURLConnection) url.openConnection();
        }catch(Exception e){e.printStackTrace();}
    }
    HttpURLConnection connection;
    private final LinkedList<ArrayList<Object>> map = new LinkedList<>();
    private final String boundary= "yoonunbotjdkfldjsaf";
    private final String end = "\r\n";
    StringBuilder start(){
        try {
            FileInputStream in;
            final String two_hyphen = "--";

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(two_hyphen + boundary + end);

            long length = map.size();

            for (int i = 0; i < length; i++) {
                ArrayList<Object> list = map.get(i);
                String key = (String) list.get(0);
                if (key.equals("text")) {
                    out.writeBytes((String) list.get(1));
                    System.out.println((String) list.get(1));
                    if (i == length - 1)
                        out.writeBytes(two_hyphen);
                    out.writeBytes(end);
                }
                if (key.equals("file")) {
                    in = new FileInputStream((File) list.get(1));
                    int BUFFER_SIZE = 4096;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.writeBytes(end + two_hyphen + boundary);
                    if (i == length - 1)
                        out.writeBytes(two_hyphen);
                    out.writeBytes(end);
                }
            }

            out.flush();
            out.close();

            String line;
            StringBuilder result = new StringBuilder();
            BufferedReader br;

            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null)
                    result.append(line).append('\n');
                System.out.println(result);
                return result;
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = br.readLine()) != null)
                    result.append(line).append('\n');
                System.out.println(result);
                return result;
            }
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public void input_text(String name, String data){
        String input = "Content-Disposition: form-data; name=\"" + name + "\"" + end + end + data + end + "--" + boundary;
        ArrayList<Object> list = new ArrayList<>();
        list.add("text");
        list.add(input);
        map.add(list);
    }
    public void input_file(String Content_Type, String name, File file){
        String input = "Content-Disposition: form-data; name=\"" + name +  "\"; filename=\"" + file.getName() + "\"" + end +
                "Content-Type:" + Content_Type + end;
        ArrayList<Object> list = new ArrayList<>();
        list.add("text");
        list.add(input);
        map.add(list);
        list = new ArrayList<>();
        list.add("file");
        list.add(file);
        map.add(list);
    }
    public void setProperty(String key, String data){
        connection.setRequestProperty(key, data);
    }
}
