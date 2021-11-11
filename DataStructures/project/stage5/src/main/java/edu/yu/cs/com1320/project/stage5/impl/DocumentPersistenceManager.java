package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import jakarta.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

    private Map<URI,Map<String, Integer>> uriMap = new HashMap<>();
    private File directory;
    public DocumentPersistenceManager(File baseDir){
        directory = baseDir;
        if(directory == null){
            directory = new File(System.getProperty("user.dir"));
        }
    }
    private class DocumentSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            String base64Encoded = DatatypeConverter.printBase64Binary(src.getDocumentBinaryData());
//            o.("myBinaryData");
            o.remove("myBinaryData");
            if (src.getDocumentBinaryData() != null) {
                o.addProperty("myBinaryData", base64Encoded);
            }
//
            return o;
            //return new JsonPrimitive(src.toString());
        }
    }
    //Json desializer- gsno from json, type class/token
    private class DocumentDeserializer implements JsonDeserializer<Document>  {
        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Document returnDoc = null;
            if (jsonObject.get("myBinaryData").getAsString() == null){
                System.out.println("STRING");
                try {
                    returnDoc = new DocumentImpl(new URI(jsonObject.get("myUri").getAsString()), jsonObject.get("myTxt").getAsString());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("BINARY");
                try {
                    returnDoc = new DocumentImpl(new URI(jsonObject.get("myUri").getAsString()), DatatypeConverter.parseBase64Binary(jsonObject.get("myBinaryData").getAsString()));
                    System.out.println("did this just work?");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return returnDoc;
            //return context.deserialize(json.getAsJsonObject(), typeOfT);
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, new DocumentSerializer())
                .setPrettyPrinting().create();
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(val);

        try {
            String str = uri.getSchemeSpecificPart() + ".json";
            File actualFile = new File (directory, str);
            Files.createDirectories(Paths.get(actualFile.getParent()));
            actualFile.createNewFile();
            FileWriter writer = new FileWriter(actualFile);
            writer.write(json);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed");
        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, new DocumentDeserializer())
                .setPrettyPrinting().create();
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        Document doc = null;
        try {
            File actualFile = new File (directory, uri.getSchemeSpecificPart() + ".json");
            System.out.println(actualFile.getAbsolutePath());
            Reader r = new FileReader(actualFile);
            doc = gson.fromJson(r, DocumentImpl.class);
            actualFile.delete();
            //doc.setWordMap(this.uriMap.get(uri));
        } catch (Exception e){
            e.printStackTrace();
        }
        //reader.close();
        return doc;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        File actualFile = new File (directory, uri.getSchemeSpecificPart() + ".json");
        return actualFile.delete();
        //make sure im not actually supposed to throw an exception here
        //throw new IllegalArgumentException("you sir tried to delete something of which was not contained, thats bad and therefore I am yelling at you right now");
    }

}
