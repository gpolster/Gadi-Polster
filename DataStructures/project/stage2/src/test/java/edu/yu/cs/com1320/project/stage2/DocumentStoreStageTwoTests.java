package edu.yu.cs.com1320.project.stage2;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage2.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage2.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

;
;

//Jonathan Wenger's Tests

class DocumentStoreStageTwoTests {
    URI[] uriArray = new URI[21];
    Document[] docArray = new Document[21];
    String[] stringArray = { "The blue parrot drove by the hitchhiking mongoose.",
            "She thought there'd be sufficient time if she hid her watch.",
            "Choosing to do nothing is still a choice, after all.", "He found the chocolate covered roaches quite tasty.",
            "The efficiency we have at removing trash has made creating trash more acceptable.",
            "Peanuts don't grow on trees, but cashews do.",
            "A song can make or ruin a person's day if they let it get to them.", "You bite up because of your lower jaw.",
            "He realized there had been several deaths on this road, but his concern rose when he saw the exact number.",
            "So long and thanks for the fish.", "Three years later, the coffin was still full of Jello.",
            "Weather is not trivial - it's especially important when you're standing in it.",
            "He walked into the basement with the horror movie from the night before playing in his head.",
            "He wondered if it could be called a beach if there was no sand.", "Jeanne wished she has chosen the red button.",
            "It's much more difficult to play tennis with a bowling ball than it is to bowl with a tennis ball.",
            "Pat ordered a ghost pepper pie.", "Everyone says they love nature until they realize how dangerous she can be.",
            "The memory we used to share is no longer coherent.",
            "My harvest will come Tiny valorous straw Among the millions Facing to the sun",
            "A dreamy-eyed child staring into night On a journey to storyteller's mind Whispers a wish speaks with the stars the words are silent in him" };

    @Test
    void testUndo() {
        for (int i = 0; i < 7; i++) {
            uriArray[i] = URI.create("www.google" + i + ".com");
        }

        for (int i = 0; i < 7; i++) {
            docArray[i] = new DocumentImpl(uriArray[i], stringArray[i]);
        }
        for (int i = 0; i < 7; i++) {
            docArray[i + 7] = new DocumentImpl(uriArray[i], stringArray[i + 7].getBytes());
        }
        for (int i = 0; i < 7; i++) {
            docArray[i + 14] = new DocumentImpl(uriArray[i], stringArray[i + 14]);
        }
        DocumentStore documentStore = new DocumentStoreImpl();
        try {
            int testa1 = documentStore.putDocument(new ByteArrayInputStream(stringArray[0].getBytes()), uriArray[0],
                    DocumentStore.DocumentFormat.TXT);
            int testa2 = documentStore.putDocument(new ByteArrayInputStream(stringArray[1].getBytes()), uriArray[1],
                    DocumentStore.DocumentFormat.TXT);
            int testa3 = documentStore.putDocument(new ByteArrayInputStream(stringArray[2].getBytes()), uriArray[2],
                    DocumentStore.DocumentFormat.TXT);
            int testa4 = documentStore.putDocument(new ByteArrayInputStream(stringArray[3].getBytes()), uriArray[3],
                    DocumentStore.DocumentFormat.TXT);
            int testa5 = documentStore.putDocument(new ByteArrayInputStream(stringArray[4].getBytes()), uriArray[4],
                    DocumentStore.DocumentFormat.TXT);
            int testa6 = documentStore.putDocument(new ByteArrayInputStream(stringArray[5].getBytes()), uriArray[5],
                    DocumentStore.DocumentFormat.TXT);
            int testa7 = documentStore.putDocument(new ByteArrayInputStream(stringArray[6].getBytes()), uriArray[6],
                    DocumentStore.DocumentFormat.TXT);
            assertEquals(testa1, 0);
            assertEquals(testa2, 0);
            assertEquals(testa3, 0);
            assertEquals(testa4, 0);
            assertEquals(testa5, 0);
            assertEquals(testa6, 0);
            assertEquals(testa7, 0);
        } catch (java.io.IOException e) {
            fail();
        }

        documentStore.undo();

        assertEquals(docArray[0], documentStore.getDocument(uriArray[0]));
        assertEquals(docArray[1], documentStore.getDocument(uriArray[1]));
        assertEquals(docArray[2], documentStore.getDocument(uriArray[2]));
        assertEquals(docArray[3], documentStore.getDocument(uriArray[3]));
        assertEquals(docArray[4], documentStore.getDocument(uriArray[4]));
        assertEquals(docArray[5], documentStore.getDocument(uriArray[5]));
        assertEquals(null, documentStore.getDocument(uriArray[6]));

        documentStore.undo(uriArray[1]);

        try {
            int testb1 = documentStore.putDocument(new ByteArrayInputStream(stringArray[7].getBytes()), uriArray[0],
                    DocumentStore.DocumentFormat.BINARY);
            int testb2 = documentStore.putDocument(new ByteArrayInputStream(stringArray[8].getBytes()), uriArray[1],
                    DocumentStore.DocumentFormat.BINARY);
            int testb3 = documentStore.putDocument(new ByteArrayInputStream(stringArray[9].getBytes()), uriArray[2],
                    DocumentStore.DocumentFormat.BINARY);
            int testb4 = documentStore.putDocument(new ByteArrayInputStream(stringArray[10].getBytes()), uriArray[3],
                    DocumentStore.DocumentFormat.BINARY);
            int testb5 = documentStore.putDocument(new ByteArrayInputStream(stringArray[11].getBytes()), uriArray[4],
                    DocumentStore.DocumentFormat.BINARY);
            int testb6 = documentStore.putDocument(new ByteArrayInputStream(stringArray[12].getBytes()), uriArray[5],
                    DocumentStore.DocumentFormat.BINARY);
            int testb7 = documentStore.putDocument(new ByteArrayInputStream(stringArray[13].getBytes()), uriArray[6],
                    DocumentStore.DocumentFormat.BINARY);
            assertEquals(testb1, docArray[0].hashCode());
            assertEquals(testb2, 0);
            assertEquals(testb3, docArray[2].hashCode());
            assertEquals(testb4, docArray[3].hashCode());
            assertEquals(testb5, docArray[4].hashCode());
            assertEquals(testb6, docArray[5].hashCode());
            assertEquals(testb7, 0);
        } catch (java.io.IOException e) {
            fail();
        }

        documentStore.undo(uriArray[1]);
        documentStore.undo(uriArray[4]);
        documentStore.undo(uriArray[5]);

        assertEquals(docArray[7], documentStore.getDocument(uriArray[0]));
        assertEquals(null, documentStore.getDocument(uriArray[1]));
        assertEquals(docArray[9], documentStore.getDocument(uriArray[2]));
        assertEquals(docArray[10], documentStore.getDocument(uriArray[3]));
        assertEquals(docArray[4], documentStore.getDocument(uriArray[4]));
        assertEquals(docArray[5], documentStore.getDocument(uriArray[5]));
        assertEquals(docArray[13], documentStore.getDocument(uriArray[6]));

        try {
            int testc1 = documentStore.putDocument(new ByteArrayInputStream(stringArray[14].getBytes()), uriArray[0],
                    DocumentStore.DocumentFormat.TXT);
            int testc2 = documentStore.putDocument(new ByteArrayInputStream(stringArray[15].getBytes()), uriArray[1],
                    DocumentStore.DocumentFormat.TXT);
            int testc3 = documentStore.putDocument(new ByteArrayInputStream(stringArray[16].getBytes()), uriArray[2],
                    DocumentStore.DocumentFormat.TXT);
            int testc4 = documentStore.putDocument(new ByteArrayInputStream(stringArray[17].getBytes()), uriArray[3],
                    DocumentStore.DocumentFormat.TXT);
            int testc5 = documentStore.putDocument(new ByteArrayInputStream(stringArray[18].getBytes()), uriArray[4],
                    DocumentStore.DocumentFormat.TXT);
            int testc6 = documentStore.putDocument(new ByteArrayInputStream(stringArray[19].getBytes()), uriArray[5],
                    DocumentStore.DocumentFormat.TXT);
            int testc7 = documentStore.putDocument(new ByteArrayInputStream(stringArray[20].getBytes()), uriArray[6],
                    DocumentStore.DocumentFormat.TXT);

            documentStore.undo(uriArray[1]);
            documentStore.undo(uriArray[6]);
            documentStore.undo();

            assertEquals(testc1, docArray[7].hashCode());
            assertEquals(testc2, 0);
            assertEquals(testc3, docArray[9].hashCode());
            assertEquals(testc4, docArray[10].hashCode());
            assertEquals(testc5, docArray[4].hashCode());
            assertEquals(testc6, docArray[5].hashCode());
            assertEquals(testc7, docArray[13].hashCode());
        } catch (java.io.IOException e) {
            fail();
        }

        assertEquals(docArray[14], documentStore.getDocument(uriArray[0]));
        assertEquals(null, documentStore.getDocument(uriArray[1]));
        assertEquals(docArray[16], documentStore.getDocument(uriArray[2]));
        assertEquals(docArray[17], documentStore.getDocument(uriArray[3]));
        assertEquals(docArray[18], documentStore.getDocument(uriArray[4]));
        assertEquals(docArray[5], documentStore.getDocument(uriArray[5]));
        assertEquals(docArray[13], documentStore.getDocument(uriArray[6]));

        for (int i = 0; i < 7; i++) {
            documentStore.undo();
        }

        assertEquals(docArray[7], documentStore.getDocument(uriArray[0]));
        assertEquals(null, documentStore.getDocument(uriArray[1]));
        assertEquals(docArray[2], documentStore.getDocument(uriArray[2]));
        assertEquals(docArray[3], documentStore.getDocument(uriArray[3]));
        assertEquals(docArray[4], documentStore.getDocument(uriArray[4]));
        assertEquals(docArray[5], documentStore.getDocument(uriArray[5]));
        assertEquals(null, documentStore.getDocument(uriArray[6]));
    }
    @Test
    void hasParameterlessPublicConstructorTestHashTable() {
        try {
            new HashTableImpl<>();
        } catch (RuntimeException e) {
            assertTrue(false);
        }
    }
    @Test
    void hasParameterlessPublicConstructorTestStack() {
        try {
            new StackImpl<>();
        } catch (RuntimeException e) {
            fail("no parameterless constructor");
        }
    }
    @Test
    void testParameterLessUndoOnDoc() throws URISyntaxException, IOException {
        URI uri = new URI("YouAreEye");
        String first = "first";
        String second = "second";
        Document one = new DocumentImpl(uri, first);
        Document two = new DocumentImpl(uri, second);
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(first.getBytes()), uri, DocumentFormat.TXT);
        assertEquals(store.getDocument(uri), one);
        assertEquals(store.putDocument(new ByteArrayInputStream(second.getBytes()), uri,
                DocumentFormat.TXT), one.hashCode());
        assertEquals(store.getDocument(uri), two);
        store.undo();
        assertEquals(store.getDocument(uri), one);
    }
    @Test
    void testParameterUndoOnDoc() throws URISyntaxException, IOException {
        URI uri = new URI("YouAreEye");
        String first = "first";
        String second = "second";
        Document one = new DocumentImpl(uri, first);
        Document two = new DocumentImpl(uri, second);
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(first.getBytes()), uri, DocumentFormat.TXT);
        assertEquals(store.getDocument(uri), one);
        assertEquals(store.putDocument(new ByteArrayInputStream(second.getBytes()), uri, DocumentFormat.TXT),
                one.hashCode());
        assertEquals(store.getDocument(uri), two);
        store.undo(uri);
        assertEquals(store.getDocument(uri), one);
    }
    @Test
    void testUndoWithEmptyStack() throws URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        assertThrows(IllegalStateException.class, ()->{
            store.undo();
        });
        assertThrows(IllegalStateException.class, () -> {
            store.undo(new URI("uri"));
        });
    }
    @Test
    void testStackUndo() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1);
        ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri1 = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri1, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri1, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri1));
        store.undo();
        assertEquals(null, store.getDocument(uri1));
        boolean test = false;
        try {
            store.undo();
        } catch (IllegalStateException e) {
            test = true;
        }
        assertTrue(test);
    }
    @Test
    void testStackUndoUri() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1); ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri1 = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri1, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri1, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri1));
        String str2 = "2";
        byte[] array2 = str2.getBytes();
        ByteArrayInputStream stream2 = new ByteArrayInputStream(array2);
        ByteArrayInputStream stream22 = new ByteArrayInputStream(array2);
        URI uri2 = new URI("2");
        assertEquals(0, store.putDocument(stream2, uri2, DocumentFormat.BINARY));
        Document doc2 = new DocumentImpl(uri2, stream22.readAllBytes());
        assertEquals(doc2, store.getDocument(uri2));
        store.undo(uri1);
        assertEquals(null, store.getDocument(uri1));
        assertEquals(doc2, store.getDocument(uri2));
    }
    @Test
    void testStackUriPutOverwrite() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1); ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri));
        String str2 = "2"; byte[] array2 = str2.getBytes();
        ByteArrayInputStream stream2 = new ByteArrayInputStream(array2); ByteArrayInputStream stream22 = new ByteArrayInputStream(array2);
        assertEquals(doc.hashCode(), store.putDocument(stream2, uri, DocumentFormat.BINARY));
        System.out.println("DOING STUFF");
        Document doc2 = new DocumentImpl(uri, stream22.readAllBytes());
        assertEquals(doc2, store.getDocument(uri));
        System.out.println("before i undo the action, get document = " + store.getDocument(uri));
        store.undo();
        System.out.println("Doc2 = " + doc2 + " and the get document = " + store.getDocument(uri));
        assertNotEquals(doc2, store.getDocument(uri));
        System.out.println("doc = " + doc);
        assertEquals(doc, store.getDocument(uri));
        System.out.println("get document = "+ store.getDocument(uri));
        store.undo();
        assertEquals(null, store.getDocument(uri));
    }
    @Test
    void testStackUriDeleteOverwrite() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1); ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri));
        String str2 = "2"; byte[] array2 = str2.getBytes();
        ByteArrayInputStream stream2 = new ByteArrayInputStream(array2); ByteArrayInputStream stream22 = new ByteArrayInputStream(array2);
        assertEquals(doc.hashCode(), store.putDocument(stream2, uri, DocumentFormat.BINARY));
        Document doc2 = new DocumentImpl(uri, stream22.readAllBytes());
        assertEquals(doc2, store.getDocument(uri));
        assertTrue(store.deleteDocument(uri)); assertEquals(null, store.getDocument(uri));
        String str3 = "3"; byte[] array3 = str3.getBytes();
        ByteArrayInputStream stream3 = new ByteArrayInputStream(array3); ByteArrayInputStream stream33 = new ByteArrayInputStream(array3);
        assertEquals(0, store.putDocument(stream3, uri, DocumentFormat.BINARY));
        Document doc3 = new DocumentImpl(uri, stream33.readAllBytes());
        assertEquals(doc3, store.getDocument(uri));
        store.undo(uri); assertEquals(null, store.getDocument(uri));
        store.undo(uri); assertEquals(doc2, store.getDocument(uri));
        store.undo(uri); assertEquals(doc, store.getDocument(uri));
        store.undo(uri); assertEquals(null, store.getDocument(uri));
    }
    @Test
    void testStackUriDeleteOverwriteNoParams() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1); ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri));
        String str2 = "2"; byte[] array2 = str2.getBytes();
        ByteArrayInputStream stream2 = new ByteArrayInputStream(array2); ByteArrayInputStream stream22 = new ByteArrayInputStream(array2);
        assertEquals(doc.hashCode(), store.putDocument(stream2, uri, DocumentFormat.BINARY));
        Document doc2 = new DocumentImpl(uri, stream22.readAllBytes());
        assertEquals(doc2, store.getDocument(uri));
        assertTrue(store.deleteDocument(uri)); assertEquals(null, store.getDocument(uri));
        String str3 = "3"; byte[] array3 = str3.getBytes();
        ByteArrayInputStream stream3 = new ByteArrayInputStream(array3); ByteArrayInputStream stream33 = new ByteArrayInputStream(array3);
        assertEquals(0, store.putDocument(stream3, uri, DocumentFormat.BINARY));
        Document doc3 = new DocumentImpl(uri, stream33.readAllBytes());
        assertEquals(doc3, store.getDocument(uri));
        store.undo(); assertEquals(null, store.getDocument(uri));
        store.undo(); assertEquals(doc2, store.getDocument(uri));
        store.undo(); assertEquals(doc, store.getDocument(uri));
        store.undo(); assertEquals(null, store.getDocument(uri));
    }
    @Test
    void testStackUriDeleteOverwriteMultipleDocs() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1); ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri));
        String str2 = "2"; byte[] array2 = str2.getBytes();
        ByteArrayInputStream stream2 = new ByteArrayInputStream(array2); ByteArrayInputStream stream22 = new ByteArrayInputStream(array2);
        assertEquals(doc.hashCode(), store.putDocument(stream2, uri, DocumentFormat.BINARY));
        Document doc2 = new DocumentImpl(uri, stream22.readAllBytes());
        assertEquals(doc2, store.getDocument(uri));
        assertTrue(store.deleteDocument(uri)); assertEquals(null, store.getDocument(uri));
        String str3 = "3"; byte[] array3 = str3.getBytes();
        URI uri2 = new URI("Hello");
        ByteArrayInputStream stream3 = new ByteArrayInputStream(array3); ByteArrayInputStream stream33 = new ByteArrayInputStream(array3);
        assertEquals(0, store.putDocument(stream3, uri2, DocumentFormat.BINARY));
        Document doc3 = new DocumentImpl(uri2, stream33.readAllBytes());
        assertEquals(doc3, store.getDocument(uri2));
        store.undo(uri); assertEquals(doc3, store.getDocument(uri2)); assertEquals(doc2, store.getDocument(uri));
        store.undo(); assertEquals(null, store.getDocument(uri2));
        store.undo(); assertEquals(doc, store.getDocument(uri));
        store.undo(); assertEquals(null, store.getDocument(uri));
    }
    @Test
    public void undoTest() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();

        String string1 = "It was a dark and stormy night";
        String string2 = "It was the best of times, it was the worst of times";
        String string3 = "It was a bright cold day in April, and the clocks were striking thirteen";
        InputStream inputStream1 = new ByteArrayInputStream(string1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(string2.getBytes());
        InputStream inputStream3 = new ByteArrayInputStream(string3.getBytes());
        URI uri1 = URI.create("www.wrinkleintime.com");

        documentStore.putDocument(inputStream1, uri1, DocumentFormat.TXT);
        assertEquals(string1, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.putDocument(inputStream2, uri1, DocumentFormat.TXT);
        assertEquals(string2, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.undo();
        assertEquals(string1, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.undo();
        assertEquals(null, documentStore.getDocument(uri1));

        documentStore.putDocument(inputStream3, uri1, DocumentFormat.TXT);
        assertEquals(string3, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.deleteDocument(uri1);
        assertEquals(null, documentStore.getDocument(uri1));
        documentStore.undo();
        assertEquals(string3, documentStore.getDocument(uri1).getDocumentTxt());
    }
    @Test
    void testThrowsException() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        boolean test = false;
        try {
            store.undo();
        } catch (IllegalStateException e) {
            test = true;
        }
        assertTrue(test);
        test = false;
        String str1 = "1"; byte[] array1 = str1.getBytes();
        ByteArrayInputStream stream1 = new ByteArrayInputStream(array1); ByteArrayInputStream stream11 = new ByteArrayInputStream(array1);
        URI uri = new URI("1");
        assertEquals(0, store.putDocument(stream1, uri, DocumentFormat.BINARY));
        Document doc = new DocumentImpl(uri, stream11.readAllBytes());
        assertEquals(doc, store.getDocument(uri));
        URI uriFake = new URI("ThisIsAFake");
        try {
            store.undo(uriFake);
        } catch (IllegalStateException e) {
            test = true;
        }
        assertTrue(test);
    }
    @Test
    public void undoTest2() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        Boolean test = false;
        try {
            documentStore.undo();
        } catch (IllegalStateException e) {
            test = true;
        }
        assertEquals(true, test);
        String string1 = "It was a dark and stormy night";
        String string2 = "It was the best of times, it was the worst of times";
        String string3 = "It was a bright cold day in April, and the clocks were striking thirteen";
        InputStream inputStream1 = new ByteArrayInputStream(string1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(string2.getBytes());
        InputStream inputStream3 = new ByteArrayInputStream(string3.getBytes());
        URI uri1 = URI.create("www.wrinkleintime.com");

        documentStore.putDocument(inputStream1, uri1, DocumentFormat.TXT);
        assertEquals(string1, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.putDocument(inputStream2, uri1, DocumentFormat.TXT);
        assertEquals(string2, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.undo();
        assertEquals(string1, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.undo();
        assertEquals(null, documentStore.getDocument(uri1));

        documentStore.putDocument(inputStream3, uri1, DocumentFormat.TXT);
        assertEquals(string3, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.deleteDocument(uri1);
        assertEquals(null, documentStore.getDocument(uri1));
        documentStore.undo();
        assertEquals(string3, documentStore.getDocument(uri1).getDocumentTxt());
    }
    @Test
    public void testUndoSpecificUri() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();

        String string1 = "It was a dark and stormy night";
        String string2 = "It was the best of times, it was the worst of times";
        String string3 = "It was a bright cold day in April, and the clocks were striking thirteen";
        String string4 = "I am free, no matter what rules surround me.";
        InputStream inputStream1 = new ByteArrayInputStream(string1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(string2.getBytes());
        InputStream inputStream3 = new ByteArrayInputStream(string3.getBytes());
        InputStream inputStream4 = new ByteArrayInputStream(string4.getBytes());
        URI uri1 = URI.create("www.wrinkleintime.com");
        URI uri2 = URI.create("www.taleoftwocities.com");
        URI uri3 = URI.create("www.1984.com");

        documentStore.putDocument(inputStream1, uri1, DocumentFormat.TXT);
        assertEquals(string1, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.putDocument(inputStream2, uri2, DocumentFormat.TXT);
        assertEquals(string2, documentStore.getDocument(uri2).getDocumentTxt());
        documentStore.undo(uri1);
        assertEquals(null, documentStore.getDocument(uri1));
        assertEquals(string2, documentStore.getDocument(uri2).getDocumentTxt());
        documentStore.putDocument(inputStream3, uri1, DocumentFormat.TXT);
        assertEquals(string3, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.putDocument(inputStream4, uri1, DocumentFormat.TXT);
        assertEquals(string4, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.deleteDocument(uri1);
        assertEquals(null, documentStore.getDocument(uri1));
        documentStore.undo(uri2);
        assertEquals(null, documentStore.getDocument(uri2));
        documentStore.undo();
        assertEquals(string4, documentStore.getDocument(uri1).getDocumentTxt());
        documentStore.undo(uri1);
        assertEquals(string3, documentStore.getDocument(uri1).getDocumentTxt());

        Boolean test = false;
        try {
            documentStore.undo(uri3);
        } catch (IllegalStateException e) {
            test = true;
        }
        assertEquals(true, test);
    }
    @Test

    void DocumentImplTest() throws URISyntaxException {

        URI uri1 = new URI("www.tuvwxyz.com");

        String txt = "YAGILU";

        URI uri2 = new URI("www.xyz.com");

        URI uri = new URI("cmpsci4days.com");

        String txt2 = "Up too late at night";

        byte[] pic = "Lots of Pics and Lots of Bytes".getBytes();

        byte[] pic2 = "Even more pix".getBytes();

        DocumentImpl doc1 = new DocumentImpl(uri1, txt);

        DocumentImpl pic1 = new DocumentImpl(uri2, pic2);

        DocumentImpl doc2 = new DocumentImpl(uri1, txt);

        assertEquals(uri1, doc1.getKey());

        assertEquals(uri2, pic1.getKey());

        assertEquals(txt, doc1.getDocumentTxt());

        assertEquals(pic2, pic1.getDocumentBinaryData());

        assertEquals(doc1.hashCode(), doc2.hashCode());

    }

    @Test

    void StackImplTest() {

        StackImpl stack = new StackImpl<Integer>();

        for (int i = 0; i < 30; i++) {

            stack.push(i);

        }

        assertEquals(29, stack.pop());

        assertEquals(28, stack.peek());

        assertEquals(29, stack.size());

        stack.push(123);

        assertEquals(123, stack.peek());

        assertEquals(123, stack.pop());

    }

    @Test

    void HashTableImplTest() {

        HashTableImpl table = new HashTableImpl<String, Integer>();

        table.put("WHY", 12);

        table.put("Do", 13);

        table.put("I Do", 14);

        table.put("Silly Things", 15);

        table.put("?", 16);

        assertEquals(12, table.get("WHY"));

        assertEquals(16, table.get("?"));

        assertNull(table.get("ALWAYS"));

        table.put("Always", 73);

        assertEquals(13, table.put("Do", 28347));

        assertEquals(28347, table.get("Do"));

        assertEquals(73, table.get("Always"));

        HashTableImpl test2 = new HashTableImpl<Integer, Integer>();

        for (int i = 0; i < 4000; i++) {

            test2.put(i, i + 1);

        }

        assertEquals(4000, test2.get(3999));

        assertEquals(2348, test2.put(2347, 1));

        assertEquals(1, test2.get(2347));

    }


    @Test

    void DocStoreTest() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("www.tuvwxyz.com");
        String txt = "YAGILU";
        URI uri2 = new URI("www.xyz.com");
        URI uri = new URI("cmpsci4days.com");
        String txt2 = "Up too late at night";
        byte[] pic = "Lots of Pics and Lots of Bytes".getBytes();
        byte [] pic2 = "Even more pix".getBytes();
        DocumentImpl doc1 = new DocumentImpl(uri1, txt);
        DocumentImpl pic1 = new DocumentImpl(uri2, pic2);
        DocumentImpl doc2 = new DocumentImpl(uri1, txt);
        for (int i = 0; i < 30; i++) {
            String str = "";
            str += i + i * 2;
            String str2 = "";
            str2+= i * i;
            if(str2.equals("225")){
                System.out.println("string equals 254");
            }
            URI uri34 = new URI(str2);
            int dq = store.putDocument(new ByteArrayInputStream(str.getBytes()), uri34, DocumentFormat.TXT);
            assertEquals(0,dq);
        }
        DocumentImpl doc23 = new DocumentImpl(new URI("36"), "18");
        assertEquals(doc23,store.getDocument(new URI("36")));
        store.undo(new URI ("36"));
        System.out.println("before creating 225 document is " + store.getDocument(new URI("225")));
        assertNull(store.getDocument(new URI("36")));
        URI x = new URI("225");
        DocumentImpl doc225 = new DocumentImpl(x, "45");
        //Change the Value
        System.out.println("document is: " + store.getDocument(x));
        assertEquals(doc225.hashCode(), store.putDocument(new ByteArrayInputStream("1".getBytes()),x, DocumentStore.DocumentFormat.TXT));
        //Delete
        System.out.println("document is: " + store.getDocument(x));
        store.deleteDocument(x);
        System.out.println("document is: " + store.getDocument(x));
        //Make Sure it is gone
        assertNull(store.getDocument(x));
        //Put it back
        store.putDocument(new ByteArrayInputStream("123".getBytes()), x, DocumentStore.DocumentFormat.TXT);
        doc225 = new DocumentImpl(x, "123");
        assertEquals(doc225, store.getDocument(x));
        //Change the top of the command stack
        DocumentImpl doc764 = new DocumentImpl(new URI("764"), "789");
        store.putDocument(new ByteArrayInputStream("789".getBytes()), new URI ("764"), DocumentStore.DocumentFormat.TXT);
        store.undo(x);
        //Undid the put so should be deleted
        assertNull(store.getDocument(x));
        //should undo the delete
        store.undo(x);
        assertEquals(new DocumentImpl(x, "1"), store.getDocument(x));
        store.undo(x);
        assertEquals(new DocumentImpl(x, "45"), store.getDocument(x));
        //Undo the top of the command stack, whcih should get rid of doc764
        store.undo();
        assertNull(store.getDocument(new URI("764")));
        System.out.println("PASSED FREAKIN DOCSTORETEST!!!");
    }

}

