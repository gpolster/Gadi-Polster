package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {
    public DocumentStoreImpl(){

    }
    private HashTable<URI, Document> myHashTable = (HashTable<URI, Document>) new HashTableImpl<URI,Document>();
    private Stack<Undoable> myStack = new StackImpl<>();
    private Trie<Document> myTrie = new TrieImpl<>();
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
                myStack.push(new GenericCommand((uri), undo));
                return 0;
            }
            this.deleteDocument(uri);
            return hashCode;
        }
        addTrieActionToStack(uri);
        this.myHashTable.put(uri,createNewDoc(input,uri,format));
        return hashCode;
    }
    private void addTrieActionToStack(URI uri){
        Document doc = getDocument(uri);
        if (doc == null) {
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.deleteDocumentNoUndo(uri);
                this.myHashTable.put(uri2,null);
                return true;
            };
            myStack.push(new GenericCommand((uri), undo));
        } else {
            Function<URI, Boolean> undo = (URI uri2) -> {
                //this.myHashTable.put(uri2,null);
                //this.myHashTable.put(uri2,doc);
                this.deleteDocumentNoUndo(uri);
                for (String word : doc.getWords()){
                    this.myTrie.put(word, doc);
                }
                this.myHashTable.put(uri2,null);
                this.myHashTable.put(uri2,doc);
                return true;
            };
            myStack.push(new GenericCommand((uri), undo));
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
        Document doc = this.getDocument(uri);
        if(this.getDocument(uri)== null){
            Function<URI, Boolean> undo = (uri2) -> true;
            myStack.push(new GenericCommand((uri), undo));
            return false;
        } else {
            this.myHashTable.put(uri, null);
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.myHashTable.put(uri2,doc);
                for (String word : doc.getWords()){
                    this.myTrie.put(word, doc);
                }
                return true;
            };
            for(String word : doc.getWords()){
                try {
                    this.myTrie.delete(word, doc);
                } catch (NullPointerException e){

                }
            }
            myStack.push(new GenericCommand((uri), undo));
            return true;
        }
    }
    private boolean deleteDocumentNoUndo(URI uri){
        Document doc = this.getDocument(uri);
        this.myHashTable.put(uri, null);
        for(String word : doc.getWords()){
            try {
                this.myTrie.delete(word, doc);
            } catch (NullPointerException e){

            }
        }
        //myStack.push(new GenericCommand((uri), undo));
        return true;

    }
    private DocumentImpl createNewDoc(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException {
        Document oldDoc = getDocument(uri);
        byte[] byteArray = input.readAllBytes();
        DocumentImpl newDoc;
        if(format == DocumentStore.DocumentFormat.TXT){
            String documentTXT = new String(byteArray);
            newDoc = new DocumentImpl(uri, documentTXT);
            if (oldDoc != null){
                for(String word : oldDoc.getWords()){
                    this.myTrie.delete(word, oldDoc);
                }
            }
            for (String word : newDoc.getWords()){
                this.myTrie.put(word, newDoc);
            }
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
        Stack<Undoable> tempStack = new StackImpl<>();
        Undoable c =  this.myStack.peek();
        boolean throwException = false;
        while(c != null){
            if ((c instanceof GenericCommand)) {
                if (((GenericCommand<?>) c).getTarget().equals(uri)) {break;}
            } else {
                if (((CommandSet<Object>) c).containsTarget(uri)){ break;}
            }
            c = this.myStack.pop();
            tempStack.push(c);
            c = this.myStack.peek();
            numberToPop++;
        }
        if (c != null) {
            if (c instanceof GenericCommand) {
                this.myStack.pop().undo();
            } else {
                CommandSet<Object> cs = ((CommandSet<Object>)this.myStack.pop());
                cs.undo(uri);
                if (!cs.isEmpty()){ this.myStack.push(cs); }
            }
        }else { throwException = true;}
        for(int i = 1; i <= numberToPop; i++){ this.myStack.push(tempStack.pop()); }
        if (throwException){ throw new IllegalStateException("no action for given uri");}
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        String newKeyword = keyword.replace("[^A-Za-z0-9]", "").toLowerCase();
        return this.myTrie.getAllSorted(keyword,(doc1, doc2) -> {
            if ( doc1.wordCount(newKeyword) < doc2.wordCount(newKeyword)) {
                return 1;
            } else if (doc1.wordCount(newKeyword) > doc2.wordCount(newKeyword)) {
                return -1;
            }
            return 0;});
    }
    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        String newKeywordPrefix = keywordPrefix.replace("[^A-Za-z0-9]", "").toLowerCase();
        return this.myTrie.getAllWithPrefixSorted(keywordPrefix,(doc1, doc2) -> {
            if ( this.prefixWordCount(doc1, newKeywordPrefix) < this.prefixWordCount(doc2, newKeywordPrefix)) {
                return 1;
            } else if (this.prefixWordCount(doc1, newKeywordPrefix) > this.prefixWordCount(doc2, newKeywordPrefix)) {
                return -1;
            }
            return 0;});
    }
    private int prefixWordCount(Document doc, String prefix){
        int counter = 0;
        for(String word : doc.getWords()){
            if (word.startsWith(prefix)){
                counter+=1;
            }
        }
        return counter;
    }
    /**
     * Completely remove any trace of any document which contains the given keyword
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        String newKeyword = keyword.replace("[^A-Za-z0-9]", "").toLowerCase();
        Set<URI> uriSet = new HashSet<>();
        CommandSet<GenericCommand> commandSet = new CommandSet<>();
        Set<Document> deletedSet = this.myTrie.deleteAll(newKeyword);
        if (deletedSet.isEmpty()){ pushNoOpCommand(); }
        for (Document doc: deletedSet) {
            URI ur = doc.getKey();
            uriSet.add(ur);
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.myHashTable.put(uri2,doc);
                for (String word : doc.getWords()){
                    this.myTrie.put(word, doc);
                }
                return true;
            };
            GenericCommand com = new GenericCommand(ur,undo);
            if (!commandSet.containsTarget(com)){
                commandSet.addCommand(com);
            }
            this.deleteDocumentNoUndo(ur);
        }
        this.myStack.push(commandSet);
        return uriSet;
    }
    private Set<URI> pushNoOpCommand (){
        Function<URI, Boolean> undo = (uri2) -> true;
        myStack.push(new GenericCommand((""), undo));
        return new HashSet<>();
    }
    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        String newKeywordPrefix = keywordPrefix.replace("[^A-Za-z0-9]", "").toLowerCase();
        Set<URI> uriSet = new HashSet<>();
        CommandSet<GenericCommand> commandSet = new CommandSet<>();
        Set<Document> deletedSet = this.myTrie.deleteAllWithPrefix(newKeywordPrefix);
        if (deletedSet.isEmpty()){ pushNoOpCommand(); }
        for (Document doc: deletedSet) {
            URI ur = doc.getKey();
            uriSet.add(doc.getKey());
            Function<URI, Boolean> undo = (URI uri2) -> {
                this.myHashTable.put(uri2,doc);
                for (String word : doc.getWords()){
                    this.myTrie.put(word, doc);
                }
                return true;
            };
            GenericCommand com = new GenericCommand(ur,undo);
            if (!commandSet.containsTarget(com)){
                commandSet.addCommand(com);
            }
            this.deleteDocumentNoUndo(doc.getKey());
        }
        this.myStack.push(commandSet);
        return uriSet;
    }

}
