package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;

import java.net.URI;
import java.util.Arrays;

public class DocumentImpl implements Document {
    private URI myUri = null;
    private String myTxt = "";
    private byte[] myBinaryData = null;
    private boolean stringDoc = false;
    byte[] emptyByteArray = new byte[0];

    public DocumentImpl(URI uri, String txt) {
        if (uri == null || txt == null){
            throw new IllegalArgumentException("either argument is null or empty/blank");
        }
        if (uri.hashCode() != 0 && txt != "") {
            this.myUri = uri;
            this.myTxt = txt;
            this.stringDoc = true;
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
        if(this.stringDoc) {
            return this.myTxt;
        } else {
            return null;
        }
    }


    /**
     * @return content of binary data document
     */
    public byte[] getDocumentBinaryData(){
        if (!this.stringDoc){
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
}
