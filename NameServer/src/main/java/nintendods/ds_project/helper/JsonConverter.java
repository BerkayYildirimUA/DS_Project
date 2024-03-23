package nintendods.ds_project.helper;

import com.google.gson.Gson;
import java.io.File;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Scanner;

public class JsonConverter {
    private String fileName = "";

    /**
     * Constructor for the JsonConverter class.
     * Initializes the object with the provided fileName.
     * It checks if the file exists, and creates it if necessary.
     *
     * @param fileName The name of the file to be used for JSON generations.
     */
    public JsonConverter(String fileName) {
        this.fileName = fileName;

        //check that file exists and create if necessary
        this.checkFileExistance();
    }

    /***
     * Converts the provided object to its JSON representation using the Gson library.
     * @param ob The object to be converted to JSON.
     * @return JSON string representing the provided object.
     */
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

    /**
     * Converts the provided JSON string to an object of the specified type using the Gson library.
     *
     * @param jsonString The JSON string to be converted to an object.
     * @param ob The type of object to which the JSON string should be converted.
     * @return An object of the specified type representing the JSON string.
     */
    public Object toObject(String jsonString, Type ob){
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ob);
    }

    /**
     * Writes the provided JSON string to a file.
     * If the file does not exist, it is created.
     *
     * @param json The JSON string to be written to the file.
     */
    public void toFile(String json){
        this.checkFileExistance();

        try{
            FileWriter fw = new FileWriter(this.fileName);
            fw.write(json);
            fw.close();
        }
        catch (IOException ex){
            System.out.println(ex);
        }
    }

    /***
     * Writes an Object to a file that's being assigned at the constructor.
     * @param ob the object that needs to be written to the file
     */
    public void toFile(Object ob){
        this.checkFileExistance();
        String data = this.toJson(ob);
        this.toFile(data);
    }

    /***
     * Extracts an object from a file that's being assigned at the constructor.
     * @param ob the object skeleton as Object.class.
     * @return the Object or else null if the json conversion failed.
     */
    public Object fromFile(Type ob){
        this.checkFileExistance();
        String data = "";
        try {
            File file = new File(this.fileName);
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
            File file = new File(this.fileName);
            if (!file.exists())
                file.createNewFile();
        }
        catch (IOException ex){
            System.out.println(ex);
        }
    }
}