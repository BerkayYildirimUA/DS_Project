package nintendods.ds_project.utility;

import com.google.gson.Gson;
import java.io.File;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Scanner;

public class JsonConverter {
    private String fileName = "";

    public JsonConverter() {
    }

    public JsonConverter(String fileName) {
        this.fileName = fileName;

        //check that file exists and create if necessary
        try {
            this.checkFileExistance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        try{
            this.checkFileExistance();
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
        try {
            this.checkFileExistance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String data = this.toJson(ob);
        this.toFile(data);
    }

    /***
     * Extracts an object from a file that's being assigned at the constructor.
     * @param ob the object skeleton as Object.class
     * @return the Object or else null if the json conversion failed
     */
    public Object fromFile(Type ob){


        String data = "";
        try {
            this.checkFileExistance();
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // decompose to an object
        return this.toObject(data, ob);
    }

    private void checkFileExistance() throws IOException{
        if(fileName.equals(""))
            throw new IOException("No file specified!");

        File file = new File(this.fileName);
        if (!file.exists())
            file.createNewFile();

    }
}