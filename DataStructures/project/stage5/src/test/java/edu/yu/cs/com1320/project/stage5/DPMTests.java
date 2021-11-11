package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DPMTests {
    @Test
    public void TestCreateDoc() throws URISyntaxException, IOException {
        try {
            PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);
            URI uri1 = null;
            String txt1;
            uri1 = new URI("http://www.yu.edu/documents/doc1");
            txt1 = "This doc1 plain text string Computer Headphones";
            Document myDoc = new DocumentImpl(uri1, txt1);
            Document myDoc2 = new DocumentImpl(uri1, txt1);
            pm.serialize(uri1, myDoc);
            pm.deserialize(uri1);
        } catch(FileNotFoundException e){
            fail();
        }
    }
    @Test
    public void TestCreateDoc2() throws URISyntaxException, IOException {
        try {
            PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);
            URI uri1 = null;
            String txt1;
            uri1 = new URI("http://edu.yu.cs/com1320/project/doc3");
            txt1 = "This is the text of doc3";
            Document myDoc = new DocumentImpl(uri1, txt1);
            Document myDoc2 = new DocumentImpl(uri1, txt1);
            pm.serialize(uri1, myDoc);
            pm.deserialize(uri1);
        } catch(FileNotFoundException e){
            fail();
        }

    }
    @Test
    public void TestTextDoc() throws URISyntaxException, IOException {
        try {
            PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);
            URI uri1 = null;
            String txt1;
            uri1 = new URI("http://www.yu.edu/documents/doc1");
            txt1 = "This doc1 plain text string Computer Headphones";
            Document myDoc = new DocumentImpl(uri1, txt1);
            Document myDoc2 = new DocumentImpl(uri1, txt1);
            pm.serialize(uri1, myDoc);
            Document ifThisWorksIllBHappy = pm.deserialize(uri1);
            assertNotNull(ifThisWorksIllBHappy);
            assertEquals(myDoc2,ifThisWorksIllBHappy);
            ifThisWorksIllBHappy.getWordMap();
            assertFalse(pm.delete(uri1));
        } catch(FileNotFoundException e){
            fail();
        }
    }
    @Test
    public void TestBinaryDoc() throws URISyntaxException, IOException {
        try {
            PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(null);
            DocumentStore ds = new DocumentStoreImpl();
            URI uri1 = null;
            String txt1;
            uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
            txt1 = "This doc1 plain text string Computer Headphones";
            Document myDoc = new DocumentImpl(uri1, txt1.getBytes());
            Document myDoc2 = new DocumentImpl(uri1, txt1.getBytes());
            ds.putDocument(new ByteArrayInputStream(txt1.getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
            uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
            txt2 = "This doc1 plain text string Compr Headphones";
            ds.setMaxDocumentCount(1);
            ds.putDocument(new ByteArrayInputStream(txt2.getBytes()), uri2, DocumentStore.DocumentFormat.BINARY);
            pm.serialize(uri1, myDoc);
            Document ifThisWorksIllBHappy = pm.deserialize(uri1);
            assertNotNull(ifThisWorksIllBHappy);
            String pLEASE = new String(ifThisWorksIllBHappy.getDocumentBinaryData());
            System.out.println(new String(ifThisWorksIllBHappy.getDocumentBinaryData()));
            assertArrayEquals(myDoc2.getDocumentBinaryData(),ifThisWorksIllBHappy.getDocumentBinaryData());
            //ifThisWorksIllBHappy.getWordMap();
            assertFalse(pm.delete(uri1));
        } catch(FileNotFoundException e){
            fail();
        }
    }
    private URI uri1;
    private String txt1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;

    //variables to hold possible values for doc3
    private URI uri3;
    private String txt3;

    //variables to hold possible values for doc4
    private URI uri4;
    private String txt4;

    private int bytes1;
    private int bytes2;
    private int bytes3;
    private int bytes4;

    @BeforeEach
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This doc1 plain text string Computer Headphones";

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text doc2 plain String";

        //init possible values for doc3
        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "This is the text of doc3";

        //init possible values for doc4
        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "This is the text of doc4";

        this.bytes1 = this.txt1.getBytes().length;
        this.bytes2 = this.txt2.getBytes().length;
        this.bytes3 = this.txt3.getBytes().length;
        this.bytes4 = this.txt4.getBytes().length;
    }
    @Test
    public void stage4TestMaxDocCountViaPut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        //uri1 and uri2 should both be gone, having been pushed out by 3 and 4


        assertNotNull(store.getDocument(this.uri4),"uri4 should still be in memory");
        assertNotNull(store.getDocument(this.uri3),"uri1 should be brought back into memory");
        assertNotNull(store.getDocument(this.uri2),"uri1 should be brought back into memory");
        assertNotNull(store.getDocument(this.uri1),"uri3 should still be in memory");
        //assertNull(store.getDocument(this.uri2),"uri2 should've been pushed out of memory when uri4 was inserted");
    }
    @Test
    public void stage4TestMaxDocCountViaSearch() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(3);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        //all 3 should still be in memory
        assertNotNull(store.getDocument(this.uri1),"uri1 should still be in memory");
        assertNotNull(store.getDocument(this.uri2),"uri2 should still be in memory");
        assertNotNull(store.getDocument(this.uri3),"uri3 should still be in memory");
        //"touch" uri1 via a search
        store.search("doc1");
        //add doc4, doc2 should be pushed out, not doc1
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
//        assertNotNull(store.getDocument(this.uri1),"uri1 should still be in memory");
//        assertNotNull(store.getDocument(this.uri3),"uri3 should still be in memory");
//        assertNotNull(store.getDocument(this.uri4),"uri4 should still be in memory");
//        //uri2 should've been pushed out of memory
//        assertNotNull(store.getDocument(this.uri2),"uri2 should not still be in memory");
    }
    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearchByPrefix() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<Document> results = store.searchByPrefix("pla");

        //was last use time updated on the searchByPrefix?
        //assertTrue(before < doc.getLastUseTime(),"last use time of search result should be after the time at which the document was put");
    }
    @Test
    public void undoPutPushesDocOut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = new DocumentImpl(this.uri1,this.txt1);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        //doc 1 gets pushed to disk, memory contains doc2 and doc3
        File test = new File(makeFilePath(uri1));
        assertTrue(test.exists());
        assertNotNull(store.getDocument(this.uri3));
        //doc 1 gets brought back in and doc3 gets pushed out, order is doc2 doc1
        store.undo();
        assertFalse(test.exists());
        assertNull(store.getDocument(uri3));
        assertEquals(doc1, store.getDocument(this.uri1));
        //pushes doc2 out order should be doc1 doc4
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        File test2 = new File(makeFilePath(uri2));
        assertTrue(test2.exists());
        deleteAllFiles();
    }
    @Test
    public void undoUriPutPushesDocOut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        Document doc1 = new DocumentImpl(this.uri1,this.txt1);
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        File one = new File(makeFilePath(uri1));
        assertTrue(one.exists());
        assertNotNull(store.getDocument(this.uri3));
        store.undo(uri1);
        assertFalse(one.exists());
        assertNull(store.getDocument(uri1));
        //assertEquals(doc1, store.getDocument(this.uri1));
        File test2 = new File(makeFilePath(uri2));
        assertTrue(test2.exists());
        deleteAllFiles();
    }
    @Test
    public void undoPutOverwrite() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        Document doc11 = new DocumentImpl(this.uri1,this.txt1);
        Document doc14 = new DocumentImpl(this.uri1,this.txt4);
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        File one = new File(makeFilePath(uri1));
        assertTrue(one.exists());
        assertNotNull(store.getDocument(this.uri3));
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        assertFalse(one.exists());
        assertEquals(doc14,store.getDocument(uri1));
        //uri 1 gets brought back in under a new doc and doc2 gets pushed out, order is uri1 doc3
        store.undo();
        //assertNull(store.getDocument(uri3));
        assertFalse(one.exists());
        assertEquals(doc11, store.getDocument(this.uri1));
        //pushes doc2 out order should be doc1 doc4
        //store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        File test2 = new File(makeFilePath(uri2));
        //assertNull(store.getDocument(this.uri2));
        assertTrue(test2.exists());
        deleteAllFiles();
    }
    @Test
    public void undoUriPutOverwrite() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        Document doc11 = new DocumentImpl(this.uri1,this.txt1.getBytes());
        Document doc14 = new DocumentImpl(this.uri1,this.txt4.getBytes());
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.BINARY);
        File one = new File(makeFilePath(uri1));
        assertTrue(one.exists());
        assertNotNull(store.getDocument(this.uri3));
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);

        assertFalse(one.exists());
        assertEquals(doc14,store.getDocument(uri1));
        store.setMaxDocumentCount(1);
        store.putDocument(new ByteArrayInputStream((this.txt4 + " hi").getBytes()), this.uri4, DocumentStore.DocumentFormat.BINARY);
        store.setMaxDocumentCount(2);
        //uri 1 gets brought back in under a new doc and doc2 gets pushed out, order is uri1 doc3
        store.undo(uri1);
        //assertNull(store.getDocument(uri3));
        assertFalse(one.exists());
        assertEquals(doc11, store.getDocument(this.uri1));
        //pushes doc2 out order should be doc1 doc4
        //store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        File two = new File(makeFilePath(uri2));
        File three = new File(makeFilePath(uri2));
        //assertNull(store.getDocument(this.uri2));
        assertTrue(two.exists());
        assertTrue(three.exists());
        //deleteAllFiles();
    }

    private String makeFilePath(URI uri){
        return System.getProperty("user.dir") + uri.getSchemeSpecificPart() + ".json";
    }
    private void deleteAllFiles(){
        File test1 = new File(makeFilePath(uri1));
        File test2 = new File(makeFilePath(uri2));
        File test3 = new File(makeFilePath(uri3));
        File test4 = new File(makeFilePath(uri4));
        test1.delete();
        test2.delete();
        test3.delete();
        test4.delete();
    }
}
