import java.util.HashMap;

public class Chatinfo {
    private final HashMap<String, Long> userid = new HashMap<>();
    private final HashMap<String, Boolean> banned_text = new HashMap<>();
    long getUserid (String name) throws Exception{
        long id = userid.get(name);
        return id;
    }
    void saveUserid (String name, long id){
        if(userid.get(name) != null)
            return;
        userid.put(name, id);
    }
    void ban_text(String text){
        banned_text.put(text, true);
    }
    boolean get_ban(String text){
        return banned_text.get(text) != null;
    }
}
