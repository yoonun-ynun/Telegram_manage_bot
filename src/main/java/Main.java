import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.time.Duration;

import com.sun.net.httpserver.*;
import com.theokanning.openai.OpenAiService;
import org.json.JSONObject;

import javax.net.ssl.*;


public class Main {
    public static JSONObject setting = new JSONObject();
    public static OpenAiService AiService;
    public static void main(String[] args) {
        try {
            BufferedWriter bw;
            BufferedReader br;
            File file = new File("config.txt");
            if (!file.exists()) {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write("hostname: 0.0.0.0\nkey_path: \nkey_password: \nTelegram_token: \nchatgpt_api: \nbot_username: \nmanager_userid: ");
                bw.flush();
                System.out.println("config.txt 파일을 확인 해 주세요");
                System.out.println("key 파일은 jks만 지원합니다.");
                return;
            }
            br = new BufferedReader(new FileReader(file));
            String[] setting = new String[7];
            int count = 0;
            String line;
            while ((line = br.readLine()) != null){
                setting[count] = line;
                count++;
            }
            for(String result : setting){
                try {
                    if(result.split(":")[0].equals("Telegram_token")){
                        Main.setting.put(result.split(":")[0], result.substring(15).replaceAll(" ", ""));
                        continue;
                    }
                    Main.setting.put(result.split(":")[0], result.split(":")[1].replaceAll(" ", ""));
                }catch (ArrayIndexOutOfBoundsException e){
                    Main.setting.put(result.split(":")[0], "");
                }
            }
            Main.AiService = new OpenAiService(Main.setting.getString("chatgpt_api"), Duration.ofSeconds(1800));

            if(Main.setting.getString("key_path").length() == 0 || Main.setting.getString("key_password").length() == 0 || Main.setting.getString("Telegram_token").length() == 0 ){
                System.out.println("config.txt의 key_path, key_password, Telegram_token 값을 채워주세요");
                return;
            }

            if(Main.setting.getString("hostname").equals("0.0.0.0")){
                String server_ip = InetAddress.getLocalHost().getHostAddress();
                Main.setting.put("hostname", server_ip);
            }

            HttpsServer server = create_server("0.0.0.0", Main.setting.getString("key_password"), Main.setting.getString("key_path"));
            if(server == null){
                System.out.println("서버를 시작하는데 실패하였습니다.");
                return;
            }
            br = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                System.out.println("종료하시려면 stop을 입력해 주세요");
                String command = br.readLine();
                if(command.equals("stop")){
                    server.stop(1);
                    break;
                }
            }
            Telegram.service.shutdownNow();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    static HttpsServer create_server(String hostname, String passwd, String key_path){
        try {
            InetSocketAddress addr = new InetSocketAddress(hostname, 443);
            HttpsServer server = HttpsServer.create(addr, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            char[] password = passwd.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(key_path);
            ks.load(fis, password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        System.err.println("Failed to create HTTPS port");
                    }
                }
            });

            server.createContext("/Telegram", new Telegram());
            server.createContext("/file", new SendFile());
            server.setExecutor(null);
            server.start();

            return server;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
