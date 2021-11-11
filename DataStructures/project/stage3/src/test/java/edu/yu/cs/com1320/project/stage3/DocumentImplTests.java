package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentImplTests {
    @Test
    public void wordCountAndGetWordsTest() throws URISyntaxException {
        DocumentImpl txtDoc = new DocumentImpl(new URI("placeholder"), " The!se ARE? sOme   W@o%$rds with^ s**ymbols (m)ixed [in]. Hope    this test test passes!");
        assertEquals(0, txtDoc.wordCount("bundle"));
        assertEquals(1, txtDoc.wordCount("these"));
        assertEquals(1, txtDoc.wordCount("WORDS"));
        assertEquals(1, txtDoc.wordCount("S-Y-M-B-O-??-LS"));
        assertEquals(1, txtDoc.wordCount("p@A$$sse$s"));
        assertEquals(2, txtDoc.wordCount("tEst"));
        Set<String> words = txtDoc.getWords();
        assertEquals(12, words.size());
        assertTrue(words.contains("some"));

        DocumentImpl binaryDoc = new DocumentImpl(new URI("0110"), new byte[] {0,1,1,0});
        assertEquals(0, binaryDoc.wordCount("anythingYouPutHereShouldBeZero"));
        Set<String> words2 = binaryDoc.getWords();
        assertEquals(0, words2.size());
    }
}
