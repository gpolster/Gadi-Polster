package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.nanoTime;


public class DocumentStoreImpl implements DocumentStore {
    public DocumentStoreImpl() {

    }
    public DocumentStoreImpl(File baseDir) {
        PersistenceManager pm = new DocumentPersistenceManager(baseDir);
        myBTree.setPersistenceManager(pm);
    }

    private long smallestTimeValue = nanoTime();
    private BTree<URI, Document> myBTree = (BTree<URI, Document>) new BTreeImpl<URI, Document>();
    private Stack<Undoable> myStack = new StackImpl<>();
    private Trie<URI> myTrie = new TrieImpl<>();
    private MinHeap<FakeDoc> myHeap = new MinHeapImpl<>();
    private int currentDocCount = 0;
    private int currentdocBytes = 0;
    private int maxDocCount = 0;
    private int maxdocBytes = 0;
    private boolean isMaxCountSet = false;
    private boolean isMaxBytesSet = false;
    private Map<URI, FakeDoc> uriToFakeDoc = new HashMap<>();

    private class FakeDoc implements Comparable<FakeDoc> {
        private URI myUri;
        private long lastUseTime;

        FakeDoc(URI uri, long lastUseTime) {
            this.myUri = uri;
            this.lastUseTime = lastUseTime;
        }

        void setLastUseTime(long lastUseTime) {
            this.lastUseTime = lastUseTime;
        }

        long getLastUseTime() {
            return this.lastUseTime;
        }

        URI getUri() {
            return this.myUri;
        }

        @Override
        public int compareTo(FakeDoc o) {
            if (this.getLastUseTime() == o.getLastUseTime()) {
                return 0;
            }
            if (this.getLastUseTime() > o.getLastUseTime()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0.
     * If there is a previous doc, return the hashCode of the previous doc.
     * If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     */
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException {
        int hashCode = 0;
        if (uri == null || format == null) {
            throw new IllegalArgumentException("uri or format was null");
        }
        if (this.internalGetDocument(uri) != null) {
            if(!onDisk(uri)) {
                this.removeDocFromHeap(this.uriToFakeDoc.get(uri));
            }
            this.currentdocBytes -= getMemoryOfDoc(this.internalGetDocument(uri));
            this.currentDocCount -= 1;
            hashCode = this.internalGetDocument(uri).hashCode();
        }
        if (input == null) {
            if (this.internalGetDocument(uri) == null) {
                Function<URI, Boolean> undo = (uri2) -> true;
                myStack.push(new GenericCommand((uri), undo));
                return 0;
            }
            this.deleteDocument(uri);
            return hashCode;
        }
        Document newDoc = createNewDoc(input, uri, format);
        Set<Document> deletedSet = this.checkAndDeleteToMakeSpace(newDoc);
        addTrieActionToStack(uri, deletedSet);
        addDocToTableAndHeap(uri, newDoc, nanoTime());
        return hashCode;
    }

    private void addDocToTableAndHeap(URI uri, Document doc, long timeToSet) {
        this.myBTree.put(uri, doc);
        FakeDoc fd = new FakeDoc(uri, timeToSet);
        this.uriToFakeDoc.put(uri, fd);
        this.currentdocBytes += getMemoryOfDoc(doc);
        this.currentDocCount += 1;
        //doc.setLastUseTime(timeToSet);
        myHeap.insert(fd);
    }

    private void addTrieActionToStack(URI uri, Set<Document> deletedSet) {
        Document prevDoc = internalGetDocument(uri);
        if (prevDoc == null) {
            Function<URI, Boolean> undo = (URI uri2) -> {
                if(!onDisk(uri2)) {
                    this.removeDocFromHeap(this.uriToFakeDoc.get(uri2));
                }
                this.deleteDocumentNoUndo(uri);
                this.myBTree.put(uri2, null);
                long timeToSet = nanoTime();
                for(Document doc : deletedSet) {
                    repopulatingForLoop(doc);
                    doc.setLastUseTime(timeToSet);
                    FakeDoc fd = uriToFakeDoc.get(doc.getKey());
                    fd.setLastUseTime(timeToSet);
                    this.myHeap.reHeapify(fd);
                    this.currentDocCount+=1;
                    this.currentdocBytes+=getMemoryOfDoc(doc);
                }
                return true;
            };
            myStack.push(new GenericCommand((uri), undo));
        } else {
            putOverwriteUndoFunction(uri, deletedSet,prevDoc);
        }
    }
    private void putOverwriteUndoFunction(URI uri, Set<Document> deletedSet, Document prevDoc ){
        Function<URI, Boolean> undo = (URI uri2) -> {
            if(!onDisk(uri2)) {
                this.removeDocFromHeap(this.uriToFakeDoc.get(uri2));
            }
            this.currentDocCount+=1;
            this.currentdocBytes+=getMemoryOfDoc(prevDoc);
            this.deleteDocumentNoUndo(uri);
            long timeToSet = nanoTime();
            for(Document doc : deletedSet) {
                repopulatingForLoop(doc);
                doc.setLastUseTime(timeToSet);
                FakeDoc fd = uriToFakeDoc.get(doc.getKey());
                fd.setLastUseTime(timeToSet);
                this.myHeap.reHeapify(fd);
                this.currentDocCount+=1;
                this.currentdocBytes+=getMemoryOfDoc(doc);
            }
            this.myBTree.put(uri2, null);
            repopulatingForLoop(prevDoc);
            addDocToTableAndHeap(uri2, prevDoc, nanoTime());
            return true;
        };
        myStack.push(new GenericCommand((uri), undo));
    }
    private void repopulatingForLoop(Document doc){
        for (String word : doc.getWords()) {
            this.myTrie.put(word, doc.getKey());
        }
        //this.myBTree.put(uri2, null);
        this.checkAndDeleteToMakeSpace(doc);
        this.getDocument(doc.getKey());
        //addDocToTableAndHeap(uri2, doc, nanoTime());
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    public Document getDocument(URI uri) {
        if (uri == null) { throw new IllegalArgumentException("parameter was null"); }
        if (!this.onDisk(uri)) {
            Document doc = this.internalGetDocument(uri);
            FakeDoc fd = uriToFakeDoc.get(uri);
            setTime(doc,fd);
            myHeap.reHeapify(fd);
            return doc;
        }
        if (internalGetDocument(uri) == null) { return null; }
        int oldCount = this.currentDocCount;
        checkAndDeleteToMakeSpace(this.internalGetDocument(uri));
        updateCountsIfNeeded(oldCount, getMemoryOfDoc(this.internalGetDocument(uri)), uri);
        Document doc = this.myBTree.get(uri);
        long timeToSet = nanoTime();
        FakeDoc fd = new FakeDoc(uri, timeToSet);
        this.uriToFakeDoc.put(uri, fd);
        myHeap.insert(fd);
        setTime(doc,fd);
        myHeap.reHeapify(fd);
        return doc;
    }
    private void setTime(Document doc, FakeDoc fd){
        if (doc != null) {
            long nt = nanoTime();
            fd.setLastUseTime(nt);
            doc.setLastUseTime(nt);
        }
    }
    private Document internalGetDocument(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("parameter was null");
        }
        Document doc = this.myBTree.get(uri);
        return doc;
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
        Document doc = this.internalGetDocument(uri);
        if (this.internalGetDocument(uri) == null) {
            Function<URI, Boolean> undo = (uri2) -> true;
            myStack.push(new GenericCommand((uri), undo));
            return false;
        } else {
            this.myBTree.put(uri, null);
            Function<URI, Boolean> undo = (URI uri2) -> {
                int oldCount = this.currentDocCount;
                this.checkAndDeleteToMakeSpace(doc);
                if(onDisk(uri2) && this.getDocument(uri2) != null){
                    return true;
                }
                for (String word : doc.getWords()) {
                    this.myTrie.put(word, doc.getKey());
                }
                addDocToTableAndHeap(uri2, doc, nanoTime());
                return true;
            };
            deleteDocFromTrie(doc);
            removeDocFromHeap(this.uriToFakeDoc.get(uri));
            this.currentdocBytes -= getMemoryOfDoc(doc);
            this.currentDocCount -= 1;
            myStack.push(new GenericCommand((uri), undo));
            return true;
        }
    }

    private void removeDocFromHeap(FakeDoc fd) {
        fd.setLastUseTime(Long.MIN_VALUE);
        this.uriToFakeDoc.remove(fd.getUri());
        myHeap.reHeapify(fd);
        myHeap.remove();
        //}
    }

    private boolean deleteDocumentNoUndo(URI uri) {
        Document doc = this.internalGetDocument(uri);
        this.myBTree.put(uri, null);
        this.currentdocBytes -= getMemoryOfDoc(doc);
        this.currentDocCount -= 1;
        //this.removeDocFromHeap(doc);
        return deleteDocFromTrie(doc);
    }

    private boolean deleteDocFromTrie(Document doc) {
        for (String word : doc.getWords()) {
            try {
                this.myTrie.delete(word, doc.getKey());
            } catch (NullPointerException e) {
            }
        }
        return true;
    }

    private DocumentImpl createNewDoc(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException {
        Document oldDoc = internalGetDocument(uri);
        byte[] byteArray = input.readAllBytes();
        DocumentImpl newDoc;
        if (format == DocumentStore.DocumentFormat.TXT) {
            String documentTXT = new String(byteArray);
            newDoc = new DocumentImpl(uri, documentTXT);
            if (oldDoc != null) {
                for (String word : oldDoc.getWords()) {
                    this.myTrie.delete(word, oldDoc.getKey());
                }
            }
            for (String word : newDoc.getWords()) {
                this.myTrie.put(word, newDoc.getKey());
            }
        } else {
            newDoc = new DocumentImpl(uri, byteArray);
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
        if (myStack.size() == 0) {
            throw new IllegalStateException("stack is empty");
        }
        this.myStack.pop().undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {
        int numberToPop = 0;
        Stack<Undoable> tempStack = new StackImpl<>();
        Undoable c = this.myStack.peek();
        boolean throwException = false;
        while (c != null) {
            if ((c instanceof GenericCommand)) { if (((GenericCommand<?>) c).getTarget().equals(uri)) { break; } }
            else { if (((CommandSet<Object>) c).containsTarget(uri)) { break; } }
            c = this.myStack.pop();
            tempStack.push(c);
            c = this.myStack.peek();
            numberToPop++;
        }
        if (c != null) {
            if (c instanceof GenericCommand) { this.myStack.pop().undo(); }
            else {
                CommandSet<Object> cs = ((CommandSet<Object>) this.myStack.pop());
                cs.undo(uri);
                if (!cs.isEmpty()) { this.myStack.push(cs); }
            }
        } else { throwException = true; }
        for (int i = 1; i <= numberToPop; i++) {
            this.myStack.push(tempStack.pop());
        }
        if (throwException) { throw new IllegalStateException("no action for given uri"); }
    }


    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        String newKeyword = keyword.replace("[^A-Za-z0-9]", "").toLowerCase();
        List<URI> uriList = this.myTrie.getAllSorted(keyword, (uri1, uri2) -> {
            Document doc1 = this.getDocument(uri1);
            Document doc2 = this.getDocument(uri2);
            if (doc1.wordCount(newKeyword) < doc2.wordCount(newKeyword)) { return 1; }
            else if (doc1.wordCount(newKeyword) > doc2.wordCount(newKeyword)) { return -1; }
            return 0;
        });
        List<Document> docList = new ArrayList<>();
        long timeToSet = nanoTime();
        int docsAdded = 0;
        int bytesAdded = 0;
        for (int i = 0; i < uriList.size(); i++) {
            URI u = uriList.get(i);
            Document doc;
            if (onDisk(u)) {
                doc = this.getDocument(u);
                docsAdded++;
                bytesAdded += this.getMemoryOfDoc(doc);
            } else { doc = this.internalGetDocument(u); }
            docList.add(i, doc);
            doc.setLastUseTime(timeToSet);
            this.uriToFakeDoc.get(u).setLastUseTime(timeToSet);
            myHeap.reHeapify(this.uriToFakeDoc.get(u));
        }
        this.currentdocBytes += bytesAdded;
        this.currentDocCount += docsAdded;
        checkAndDeleteToMakeSpace(null);
        return docList;
    }

    private boolean onDisk(URI uri) {
        return !this.uriToFakeDoc.containsKey(uri);
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        String newKeywordPrefix = keywordPrefix.replace("[^A-Za-z0-9]", "").toLowerCase();
        List<URI> uriList = this.myTrie.getAllWithPrefixSorted(keywordPrefix, (uri1, uri2) -> {
            Document doc1 = this.getDocument(uri1);
            Document doc2 = this.getDocument(uri2);
            if (this.prefixWordCount(doc1, newKeywordPrefix) < this.prefixWordCount(doc2, newKeywordPrefix)) {
                return 1;
            } else if (this.prefixWordCount(doc1, newKeywordPrefix) > this.prefixWordCount(doc2, newKeywordPrefix)) {
                return -1;
            }
            return 0;
        });
        List<Document> docList = new ArrayList<>();
        long timeToSet = nanoTime();
        for (int i = 0; i < uriList.size(); i++) {
            URI u = uriList.get(i);
            Document doc = this.getDocument(u);
            docList.add(i, doc);
            doc.setLastUseTime(timeToSet);
            this.uriToFakeDoc.get(u).setLastUseTime(timeToSet);
            myHeap.reHeapify(this.uriToFakeDoc.get(u));
            int oldCount = this.currentDocCount;
            updateCountsIfNeeded(oldCount, getMemoryOfDoc(doc), u);
        }
        this.checkAndDeleteToMakeSpace(null);
        return docList;
    }

    private int prefixWordCount(Document doc, String prefix) {
        int counter = 0;
        for (String word : doc.getWords()) {
            if (word.startsWith(prefix)) {
                counter += 1;
            }
        }
        return counter;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        String newKeyword = keyword.replace("[^A-Za-z0-9]", "").toLowerCase();
        CommandSet<GenericCommand> commandSet = new CommandSet<>();
        Set<URI> deletedSet = this.myTrie.deleteAll(newKeyword);
        if (deletedSet.isEmpty()) {
            pushNoOpCommand();
        }
        for (URI ur : deletedSet) {
            Function<URI, Boolean> undo = (URI uri2) -> {
                long timeToSet = nanoTime();
                this.checkAndDeleteToMakeSpace(this.getDocument(ur));
                if(onDisk(uri2) && this.getDocument(uri2) != null){
                    return true;
                }
                for (String word : this.getDocument(ur).getWords()) {
                    this.myTrie.put(word, ur);
                }
                this.addDocToTableAndHeap(uri2, this.getDocument(ur), timeToSet); //passes tests when I use nanoTime() instead of timeToSet
                return true;
            };
            GenericCommand com = new GenericCommand(ur, undo);
            if (!commandSet.containsTarget(com)) {
                commandSet.addCommand(com);
            }
            this.removeDocFromHeap(this.uriToFakeDoc.get(ur));
            this.deleteDocumentNoUndo(ur);
        }
        this.myStack.push(commandSet);
        return deletedSet;
    }

    private Set<URI> pushNoOpCommand() {
        Function<URI, Boolean> undo = (uri2) -> true;
        myStack.push(new GenericCommand((""), undo));
        return new HashSet<>();
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        String newKeywordPrefix = keywordPrefix.replace("[^A-Za-z0-9]", "").toLowerCase();
        CommandSet<GenericCommand> commandSet = new CommandSet<>();
        Set<URI> deletedSet = this.myTrie.deleteAllWithPrefix(newKeywordPrefix);
        if (deletedSet.isEmpty()) {
            pushNoOpCommand();
        }
        for (URI ur : deletedSet) {
            Function<URI, Boolean> undo = (URI uri2) -> {
                long timeToSet = nanoTime();
                this.checkAndDeleteToMakeSpace(this.getDocument(ur));
                if(onDisk(uri2) && this.getDocument(uri2) != null){
                    //this.getDocument(uri2);
                    return true;
                }
                for (String word : this.getDocument(ur).getWords()) {
                    this.myTrie.put(word, ur);
                }
                addDocToTableAndHeap(uri2, this.getDocument(ur), timeToSet);
                return true;
            };
            GenericCommand com = new GenericCommand(ur, undo);
            if (!commandSet.containsTarget(com)) {
                commandSet.addCommand(com);
            }
            this.removeDocFromHeap(this.uriToFakeDoc.get(ur));
            this.deleteDocumentNoUndo(ur);
        }
        this.myStack.push(commandSet);
        return deletedSet;
    }

    private int getMemoryOfDoc(Document doc) {
        String memory = doc.getDocumentTxt();
        if (memory != null) {
            return memory.getBytes().length;
        } else {
            return doc.getDocumentBinaryData().length;
        }
    }

    private boolean hasSpace(Document doc) {
        if (!isMaxBytesSet && !isMaxCountSet) {
            return true;
        }
        if (doc == null) {
            if (((this.currentDocCount) <= this.maxDocCount || !isMaxCountSet) && (this.currentdocBytes <= this.maxdocBytes || !isMaxBytesSet)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (((this.currentDocCount + 1) <= this.maxDocCount || !isMaxCountSet) && ((this.currentdocBytes + this.getMemoryOfDoc(doc)) <= this.maxdocBytes || !isMaxBytesSet)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private Set<Document> deleteToMakeSpace(Document newDoc) {
        Set<Document> deletedSet = new HashSet<>();
        while (!this.hasSpace(newDoc)) {
            URI u = this.myHeap.remove().getUri();
            deletedSet.add(this.internalGetDocument(u));
            this.currentdocBytes -= getMemoryOfDoc(this.internalGetDocument(u));
            this.uriToFakeDoc.remove(u);
            this.currentDocCount -= 1;
            try {
                this.myBTree.moveToDisk(u);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedSet;
    }

    private Set<Document> checkAndDeleteToMakeSpace(Document doc) {
        Set<Document> returnSet = new HashSet<>();
        if (doc == null) {
            this.deleteToMakeSpace(null);
        }
        if (!this.hasSpace(doc)) {
            returnSet = this.deleteToMakeSpace(doc);
        }
        return returnSet;
    }

    private void updateCountsIfNeeded(int oldCount, int newBytes, URI uri) {
        if (oldCount > this.currentDocCount) {
            FakeDoc fd = new FakeDoc(uri, nanoTime());
            this.uriToFakeDoc.put(uri, fd);
            this.currentdocBytes += newBytes;
            this.currentDocCount += 1;
        }
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.isMaxCountSet = true;
        this.maxDocCount = limit;
        this.checkAndDeleteToMakeSpace(null);
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.isMaxBytesSet = true;
        this.maxdocBytes = limit;
        this.checkAndDeleteToMakeSpace(null);
    }
//    private Document getHeapRoot(){
//        Document root = myHeap.remove();
//        myHeap.insert(root);
//        myHeap.reHeapify(root);
//        return root;
//    }

}
