package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private URI myUri = null;
    private String myTxt = "";
    //add transient?
    private byte[] myBinaryData = null;
    private transient boolean stringDoc;
    private transient byte[] emptyByteArray = new byte[0];
    private transient Set<String> myWords = new HashSet<>();
    private Map<String,Integer> wordMap = new HashMap<>();
    private transient long time;

    public DocumentImpl(URI uri, String txt) {
        if (uri == null || txt == null){
            throw new IllegalArgumentException("either argument is null or empty/blank");
        }
        if (uri.hashCode() != 0 && txt != "") {
            this.myUri = uri;
            this.myTxt = txt;
            this.stringDoc = true;
            for (String word : txt.split(" ")){
                word = word.replaceAll("[^A-Za-z0-9]", "");
                word = word.toLowerCase();
                if (!word.equals("")) {
                    if (myWords.add(word)) {
                        wordMap.put(word, 1);
                    } else {
                        wordMap.put(word, wordMap.get(word) + 1);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("either argument is null or empty/blank");
        }
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null || binaryData == null || Arrays.equals(emptyByteArray, binaryData)){
            throw new IllegalArgumentException("either argument is null or empty/blank");
        }
        if (uri.hashCode() != 0 && binaryData.hashCode() != 0) {
            this.myUri = uri;
            this.myBinaryData = binaryData;
        } else {
            throw new IllegalArgumentException("either argument is null or empty/blank");
        }
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        if(!this.myTxt.equals("")) {
            return this.myTxt;
        } else {
            return null;
        }
    }


    /**
     * @return content of binary data document
     */
    public byte[] getDocumentBinaryData(){
        if (this.myTxt.equals("")){
            return  this.myBinaryData;
        } else {
            return null;
        }
    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey(){

        return myUri;
    }
    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        word = word.replaceAll("[^A-Za-z0-9]", "");
        word = word.toLowerCase();
        if (!this.stringDoc || !wordMap.containsKey(word)) {
            return 0;
        } else {
            return this.wordMap.get(word);
        }
    }

    @Override
    public Set<String> getWords() {
        if (!this.stringDoc) {
            return new HashSet<>();
        } else {
            return this.myWords;
        }
    }

    @Override
    public long getLastUseTime() {
        return this.time;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.time = timeInNanoseconds;

    }

    @Override
    public Map<String, Integer> getWordMap() {
        return Map.copyOf(wordMap);
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordMap = wordMap;
    }

    @Override
    public int hashCode() {
        int result = myUri.hashCode();
        result = 31 * result + (myTxt != null ? myTxt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(myBinaryData);
        return result;
    }
    @Override
    public boolean equals(Object ob){
        return this.hashCode() == ob.hashCode();
    }

    @Override
    public int compareTo(Document o) {
        if (this.getLastUseTime() == o.getLastUseTime()){
            return 0;
        }
        if (this.getLastUseTime() > o.getLastUseTime()){
            return 1;
        } else {
            return -1;
        }
    }
}
