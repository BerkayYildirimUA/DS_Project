package nintendods.ds_project.helper;

import com.google.gson.Gson;
import java.io.File;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Scanner;

public class JsonConverter {
    private String fileName = "";

    /***
     * Constructor with the file name as parameter
     * Will create the file if not existent
     * @param fileName
     */
    public JsonConverter(String fileName) {
        this.fileName = fileName;

        //check that file exists and create if necessary
        this.checkFileExistance();
    }

    /***
     * Creates a json string format of a given Object
     * @param ob the object to be converted to a json string
     * @return the json string created from the given object
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

    /***
     * Converts a json string to a given object skeleton
     * @param jsonString The json string
     * @param ob the object structure as Object.class
     * @return An Object or null if json conversion has failed.
     */
    public Object toObject(String jsonString, Type ob){
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ob);
    }

    /***
     * Write a json string to a file that's being assigned at the constructor.
     * @param json the json string
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
     * @param ob the object skeleton as Object.class
     * @return the Object or else null if the json conversion failed
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