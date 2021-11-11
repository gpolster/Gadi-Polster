package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreStageFourTests {
    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;

    //variables to hold possible values for doc2
    private URI uri2;
    String txt2;

    private URI uri3;
    String txt3;

    private URI uri4;
    String txt4;

    private URI uri5;
    String txt5;

    private URI uri6;
    String txt6;

    private URI uri7;
    String txt7;

    private URI uri8;
    String txt8;

    private URI uri9;
    String txt9;

    private URI uriThinkingOutLoud;
    String txtThinkingOutLoud;

    @BeforeEach
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://www.yu.edu/documents/doc1");
        this.txt1 = "Mr and Mrs Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much.";

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "They were the last people you’d expect to be involved in anything strange or mysterious, because they just didn’t hold with such nonsense. ";

        //init possible values for doc3
        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "Mr Dursley was the director of a firm called Grunnings, which made drills. ";

        //init possible values for doc4
        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "He was a big, beefy man with hardly any neck, although he did have a very large moustache.";

        //init possible values for doc5
        this.uri5 = new URI("http://edu.yu.cs/com1320/project/doc5");
        this.txt5 = "Mrs Dursley was thin and blonde and had nearly twice the usual amount of neck, " +
                "which came in very useful as she spent so much of her time craning over garden fences, spying on the neighbours.";
        //init possible values for doc6
        this.uri6 = new URI("http://edu.yu.cs/com1320/project/doc6");
        this.txt6 = "The Dursleys had a small son called Dudley and in their opinion there was no finer boy anywhere. ";

        this.uri7 = new URI("http://edu.yu.cs/com1320/project/doc7");
        this.txt7 = " The Dursleys had everything they wanted, but they also had a secret, and their greatest fear was that somebody would discover it.";

        //init possible values for doc5
        this.uri8 = new URI("http://edu.yu.cs/com1320/project/doc8");
        this.txt8 = "They didn’t think they could bear it if anyone found out about the Potters.";
        //init possible values for doc6
        this.uri9 = new URI("http://edu.yu.cs/com1320/project/doc9");
        this.txt9 = " Mrs Potter was Mrs Dursley’s sister, but they hadn’t met for several years; in fact, Mrs Dursley pretended she didn’t have a sister, because her sister and her good- for-nothing husband were as unDursleyish as it was possible to be.";

        this.uriThinkingOutLoud = new URI("http://edu.yu.cs/com1320/project/doc77");
        this.txtThinkingOutLoud = "I'm very curious if anyone ever looks at our tests and if they are confused by the different names and texts we come up with. I hope it would give them a laugh ;) ";
    }
    @Test
    public void basicDocCountTest() throws IOException {
        System.out.println(this.uri1.getRawSchemeSpecificPart() + ".json");
        DocumentStore store = new DocumentStoreImpl();
        DocumentImpl doc1 = new DocumentImpl(this.uri1, this.txt1);
        DocumentImpl doc2 = new DocumentImpl(this.uri2, this.txt2);
        DocumentImpl doc3 = new DocumentImpl(this.uri3, this.txt3);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        assertNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        System.out.println("passed basicDocCountTest, woot!");
        assertEquals(0, store.search("and").size());
        //assertThrows(new IllegalArgumentException(), store.undo(this.uri1));
        try{
            store.undo(this.uri1);
        } catch(IllegalStateException e){
            System.out.println("caught!!");
        }
    }
    @Test
    public void basicBinaryCountTest() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        DocumentImpl doc2 = new DocumentImpl(this.uri2, this.txt2);
        DocumentImpl doc1 = new DocumentImpl(this.uri1, this.txt1);
        DocumentImpl doc3 = new DocumentImpl(this.uri3, this.txt3);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        store.setMaxDocumentBytes(this.txt1.getBytes().length + this.txt3.getBytes().length);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.BINARY);
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri3));
        assertEquals(0, store.search("people").size());
        //assertThrows(new IllegalArgumentException(), store.undo(this.uri1));
        try{
            store.undo(this.uri2);
        } catch(IllegalStateException e){
            System.out.println("caught!!");
        }
        System.out.println("passed basicBinaryCountTest, woot!");
    }
    @Test
    public void longerMemoryTest() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        DocumentImpl doc1 = new DocumentImpl(this.uri1, this.txt1);
        DocumentImpl doc2 = new DocumentImpl(this.uri2, this.txt2);
        DocumentImpl doc3 = new DocumentImpl(this.uri3, this.txt3);
        DocumentImpl doc4 = new DocumentImpl(this.uri4, this.txt4);
        DocumentImpl doc5 = new DocumentImpl(this.uri5, this.txt5);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(3);
        store.getDocument(this.uri1);
        store.getDocument(this.uri2);//order is now 3,1,2
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt5.getBytes()), this.uri5, DocumentStore.DocumentFormat.TXT);
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri1)); //now order is 2,4,5
        store.setMaxDocumentBytes(this.txt5.getBytes().length);
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));
        assertNotNull(store.getDocument(this.uri5));
        store.setMaxDocumentCount(10);
        store.setMaxDocumentBytes(Integer.MAX_VALUE);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        store.getDocument(this.uri5);
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertNotNull(store.getDocument(this.uri5));
        store.search("craning"); //order should now be 1,2,3,4,5
        store.deleteDocument(this.uri5);
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertNull(store.getDocument(this.uri5));
        store.setMaxDocumentBytes(getByteMemory(txtThinkingOutLoud,txt2,txt3,txt4));
        store.setMaxDocumentCount(4);
        store.putDocument(new ByteArrayInputStream(this.txtThinkingOutLoud.getBytes()), this.uriThinkingOutLoud, DocumentStore.DocumentFormat.BINARY); //order should be 2,3,4,7
        assertNull(store.getDocument(this.uri5));
        assertNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uriThinkingOutLoud));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }
    private int getByteMemory(String a, String b,String c, String d){
        return a.getBytes().length + b.getBytes().length + c.getBytes().length + d.getBytes().length;
    }
    @Test
    public void notSoBasicUndoTimeTest() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        DocumentImpl doc2 = new DocumentImpl(this.uri2, this.txt2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        store.undo();
        //store.setMaxDocumentCount(2);
        store.setMaxDocumentBytes(this.txt1.getBytes().length + this.txt3.getBytes().length);//should be deleting uri2 bc uri1's time was just updated in undo
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri1));
        store.getDocument(this.uri1);
        store.setMaxDocumentBytes(Integer.MAX_VALUE);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt6.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.getDocument(this.uri3);
        store.undo();//testing an undo overwrite making sure it still works
        store.setMaxDocumentCount(2);
        assertNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertEquals(doc2,store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
    }
    @Test
    public void slightlyLessBasicUndoTimeTest() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt5.getBytes()),this.uri5, DocumentStore.DocumentFormat.TXT);
        //store.setMaxDocumentBytes(this.txt1.getBytes().length + this.txt3.getBytes().length);
        store.setMaxDocumentCount(4);
        store.undo(uri1);
        //should be deleting uri2 bc uri1's time was just updated in undo
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri1));
    }
    @Test
    public void straightUpComplicatedUndoCommandSetTest() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txtThinkingOutLoud.getBytes()),this.uriThinkingOutLoud, DocumentStore.DocumentFormat.TXT);
        //assertEquals(3, store.search("they").size());
        store.setMaxDocumentCount(2);
        assertNull(store.getDocument(this.uri1));
        assertEquals(2, store.deleteAll("they").size());
        //store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        assertNotNull(store.getDocument(this.uri3));
        assertEquals(0, store.search("they").size());
        try{
            store.undo(uri1);
            fail();
        } catch (IllegalStateException e){
        }
        assertEquals(0, store.search("they").size());
        store.undo(uri2);
        assertEquals(1, store.search("they").size());
        store.undo(uriThinkingOutLoud);
        assertEquals(2, store.search("they").size());
        //store.setMaxDocumentCount(2);
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri2));
        assertEquals(0, store.search("director").size());
        System.out.println("passed test straightUpComplicatedUndoCommandSetTest, WOOOOOOOOOOOT!");
    }
    @Test
    public void straightUpREALLYComplicatedUndoCommandSetTest() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txtThinkingOutLoud.getBytes()),this.uriThinkingOutLoud, DocumentStore.DocumentFormat.TXT);
        //assertEquals(3, store.search("they").size());
        store.setMaxDocumentCount(2);//2,T
        assertEquals(2, store.deleteAll("they").size());
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uriThinkingOutLoud));
        store.setMaxDocumentCount(5); //passes when count is set to 2, why????
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt5.getBytes()),this.uri5, DocumentStore.DocumentFormat.TXT);
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertNotNull(store.getDocument(this.uri5));//3,4,5
        assertEquals(0, store.search("they").size());
        try{
            store.undo(uri1);
            fail();
        } catch (IllegalStateException e){
        }
        assertNull(store.getDocument(this.uri1));
        assertEquals(0, store.search("they").size());
        store.undo(uri2);
        //assertNotNull(store.getDocument(this.uri2));
        //assertNull(store.getDocument(this.uriThinkingOutLoud));
        store.undo(uriThinkingOutLoud);//order should be 3,4,5,2/T
//        assertNotNull(store.getDocument(this.uri3));
//        assertNotNull(store.getDocument(this.uri4));
//        assertNotNull(store.getDocument(this.uri5));
//        assertNotNull(store.getDocument(this.uri2));
//        assertNotNull(store.getDocument(this.uriThinkingOutLoud));
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        //store.setMaxDocumentCount(5);
        assertNull(store.getDocument(this.uri3));
        store.putDocument(new ByteArrayInputStream(this.txt6.getBytes()),this.uri6, DocumentStore.DocumentFormat.TXT);
        assertNull(store.getDocument(this.uri4));
        store.putDocument(new ByteArrayInputStream(this.txt7.getBytes()),this.uri7, DocumentStore.DocumentFormat.TXT);
        assertNull(store.getDocument(this.uri5));
        store.putDocument(new ByteArrayInputStream(this.txt8.getBytes()),this.uri8, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt9.getBytes()),this.uri9, DocumentStore.DocumentFormat.TXT);
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uriThinkingOutLoud));
        assertNotNull(this.uri1);
        assertNotNull(this.uri6);
        assertNotNull(this.uri7);
        assertNotNull(this.uri8);
        assertNotNull(this.uri9);
        System.out.println("passed test straightUpREALLYComplicatedUndoCommandSetTest, WOOT WOOOOOOOOOOOT!");
    }
}
