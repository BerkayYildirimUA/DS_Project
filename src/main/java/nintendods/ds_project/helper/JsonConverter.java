package nintendods.ds_project.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonConverter {
    private String fileLocation = "";

    public JsonConverter(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String toJson(Object ob){
        try {
            Gson gson = new Gson();
            return gson.toJson(ob);
        }
        catch (Exception ex){
            System.out.println(ex);
        }
        return "";
    }

    public Object toObject(String jsonString, Object ob){
        Gson gson = new Gson();
        return gson.fromJson(jsonString, (Type) ob);
    }
}