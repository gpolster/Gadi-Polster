package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


public class DocumentStoreImpl implements DocumentStore{
    public DocumentStoreImpl(){

    }
    private HashTable<URI, Document> myHashTable = (HashTable<URI, Document>) new HashTableImpl<URI,Document>();
    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0.
     * If there is a previous doc, return the hashCode of the previous doc.
     * If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     */
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException{
        int hashCode = 0;
        if (this.getDocument(uri) != null) {
            hashCode = this.getDocument(uri).hashCode();
        }
        if (input == null) {
                this.deleteDocument(uri);
                return hashCode;
        }
        byte[] byteArray = input.readAllBytes();
        if(format == DocumentStore.DocumentFormat.TXT ){
            String documentTXT = new String(byteArray);
            DocumentImpl newTxtDoc = new DocumentImpl(uri, documentTXT);
            this.myHashTable.put(uri, newTxtDoc);
        } else {
            DocumentImpl newBinaryDoc = new DocumentImpl(uri, byteArray);
            this.myHashTable.put(uri,newBinaryDoc);
        }
        return hashCode;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    public Document getDocument(URI uri){
        return this.myHashTable.get(uri);
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri){
        if(this.getDocument(uri)== null){
            return false;
        }
        if(this.myHashTable.put(uri, null) == null) {
            return true;
        } else {
            return false;
        }
    }
}
