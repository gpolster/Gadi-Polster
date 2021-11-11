package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class HeapImplTests {
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

    @BeforeEach
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
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
    }
    @Test
    public void reheapifyTest() throws URISyntaxException {
        MinHeap heap = new MinHeapImpl();
        DocumentStore store = new DocumentStoreImpl();
        DocumentImpl doc1 = new DocumentImpl(this.uri1, this.txt1);
        doc1.setLastUseTime(1);
        DocumentImpl doc2 = new DocumentImpl(this.uri2, this.txt2);
        doc2.setLastUseTime(2);
        DocumentImpl doc3 = new DocumentImpl(this.uri3, this.txt3);
        doc3.setLastUseTime(3);
        DocumentImpl doc4 = new DocumentImpl(this.uri4, this.txt4);
        doc4.setLastUseTime(4);
        DocumentImpl doc5 = new DocumentImpl(this.uri5, this.txt5);
        doc5.setLastUseTime(5);
        heap.insert(doc1);
        heap.insert(doc2);
        heap.insert(doc3);
        heap.insert(doc4);
        heap.insert(doc5);
        doc1.setLastUseTime(6);
        heap.reHeapify(doc1);
        doc2.setLastUseTime(10);
        heap.reHeapify(doc2);
        doc4.setLastUseTime(1);
        heap.reHeapify(doc4);
        assertEquals(doc4, heap.remove());
        try{
            heap.reHeapify(doc4);
        } catch (NoSuchElementException e){
            System.out.println("thats good");
        }

        //assertThrows(new NoSuchElementException(), heap.reHeapify(txtDoc2));
        assertEquals(heap.remove(), doc3);
        assertEquals(heap.remove(), doc5);
        assertEquals(heap.remove(), doc1);
        assertEquals(heap.remove(), doc2);



    }
}

