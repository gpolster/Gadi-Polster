package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.stage2.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {
    public DocumentStoreImpl(){

    }
    private HashTable<URI, Document> myHashTable = (HashTable<URI, Document>) new HashTableImpl<URI,Document>();
    private Stack<Command> myStack = new StackImpl<>();
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
        if (uri == null || format == null){
            throw new IllegalArgumentException("uri or format was null");
        }
        if (this.getDocument(uri) != null) {
            hashCode = this.getDocument(uri).hashCode();
        }
        if (input == null) {
            if(this.getDocument(uri)== null) {
                Function<URI, Boolean> undo = (uri2) -> true;
                myStack.push(new Command((uri), undo));
                return 0;
            }
            this.deleteDocument(uri);
            return hashCode;
        }
        addPutToStack(uri);
        this.myHashTable.put(uri,createNewDoc(input,uri,format));
        return hashCode;
    }
    private void addPutToStack(URI uri){
        if (getDocument(uri) == null) {
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.myHashTable.put(uri2,null);
                return true;
            };
            myStack.push(new Command((uri), undo));
        } else {
            Document docToBeReplaced = getDocument(uri);
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.myHashTable.put(uri2,null);
                this.myHashTable.put(uri2,docToBeReplaced);
                return true;
            };
            myStack.push(new Command((uri), undo));
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    public Document getDocument(URI uri){
        if(uri == null){
            throw new IllegalArgumentException("parameter was null");
        }
        return this.myHashTable.get(uri);
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri){
        if(this.getDocument(uri)== null){
            Function<URI, Boolean> undo = (uri2) -> true;
            myStack.push(new Command((uri), undo));
            return false;
        } else {
            Document docToBeReplaced = getDocument(uri);
            this.myHashTable.put(uri, null);
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.myHashTable.put(uri2,docToBeReplaced);
                return true;
            };
            myStack.push(new Command((uri), undo));
            return true;
        }
    }
    private DocumentImpl createNewDoc(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException {
        byte[] byteArray = input.readAllBytes();
        DocumentImpl newDoc;
        if(format == DocumentStore.DocumentFormat.TXT){
            String documentTXT = new String(byteArray);
            newDoc = new DocumentImpl(uri, documentTXT);
        }else{
            newDoc= new DocumentImpl(uri, byteArray);
        }
        return newDoc;
    }
    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (myStack.size() ==0){
            throw new IllegalStateException("stack is empty");
        }
            this.myStack.pop().undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException{
        int numberToPop = 0;
        Stack<Command> tempStack = new StackImpl<>();
        Command c = this.myStack.peek();
        Boolean throwException = false;
        while(c != null && !c.getUri().equals(uri)){
            c = this.myStack.pop();
            tempStack.push(c);
            c = this.myStack.peek();
            numberToPop++;
        }
        if (c != null) {
            this.myStack.pop().undo();
        }else {
            throwException = true;
        }
        for(int i = 1; i <= numberToPop; i++){
            Command com = tempStack.pop();
            this.myStack.push(com);
        }
        if (throwException){
            throw new IllegalStateException("no action for given uri");
        }
    }

}
