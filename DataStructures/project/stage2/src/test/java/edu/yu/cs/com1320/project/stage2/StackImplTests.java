package edu.yu.cs.com1320.project.stage2;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StackImplTests{
    @Test
    public void oneStackImpl(){
        Stack<Integer> stack = new StackImpl<>();
        for(int i = 0; i < 100; i++){
            stack.push(i);

        }
        for(int i = 99; i >= 0; i--){
            assertEquals(i, stack.peek());
            assertEquals(i, stack.pop());
        }
        passMessage();
    }
    //Code below belongs to Azriel Bachrach
    @Test
    public void simplePushAndPop() {
        Stack<String> s = new StackImpl<>();
        s.push("one");
        s.push("two");
        s.push("three");

        assertEquals(3, s.size());
        assertEquals("three", s.peek());
        assertEquals("three", s.pop());
        assertEquals("two", s.peek());
        assertEquals("two", s.peek());
        assertEquals(2, s.size());
        assertEquals("two", s.pop());
        assertEquals("one", s.pop());
        assertEquals(0, s.size());
        passMessage();
    }

    @Test
    public void aLotOfData() {
        Stack<Integer> s = new StackImpl<>();
        for (int i = 0; i < 1000; i++) {
            s.push(i);
            assertEquals((Integer)i, s.peek());
        }
        assertEquals(1000, s.size());
        assertEquals((Integer)999, s.peek());
        for (int i = 999; i >= 0; i--) {
            assertEquals((Integer)i, s.peek());
            assertEquals((Integer)i, s.pop());
        }
        assertEquals(0, s.size());
        passMessage();
    }
    public void passMessage(){
        System.out.println("\nPassed test \"" + Thread.currentThread().getStackTrace()[2].getMethodName() + "\"\n");
    }
}
// testing the function
