package nintendods.ds_project.helper;

import com.google.gson.Gson;
import java.io.File;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Scanner;

public class JsonConverter {
    private String fileLocation = "";

    public JsonConverter(String fileLocation) {
        this.fileLocation = fileLocation;

        //check that file exists and create if necessary
        this.checkFileExistance();
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

    public void toFile(String json){
        this.checkFileExistance();

        try{
            FileWriter fw = new FileWriter(this.fileLocation);
            fw.write(json);
            fw.close();
        }
        catch (IOException ex){
            System.out.println(ex);
        }
    }

    public void toFile(Object ob){
        this.checkFileExistance();
        String data = this.toJson(ob);
        this.toFile(data);
    }

    public Object fromFile(Object ob){
        this.checkFileExistance();
        String data = "";
        try {
            File file = new File(this.fileLocation);
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                data = reader.nextLine();
                System.out.println(data);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // decompose to an object
        return this.toObject(data, ob);
    }

    private void checkFileExistance(){
        try {
            File file = new File(this.fileLocation);
            if (!file.exists())
                file.createNewFile();
        }
        catch (IOException ex){
            System.out.println(ex);
        }
    }
}