package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {

    class StackEntry<E>{
        E entry;
        private StackEntry<E> next;
        StackEntry(E t, StackEntry next){
            if(t == null){
                throw new IllegalArgumentException("entry was null");
            }
            entry = t;
            this.next = next;
        }
    }
    public StackImpl() {}
        private StackEntry<T> head;
        private int size = 0;
    /**
     * @param element object to add to the Stack
     */
    public void push(T element) {
        if (head == null){
            StackEntry<T> newEntry = new StackEntry(element, null);
            this.head = newEntry;
            size++;
        } else {
            StackEntry newEntry = new StackEntry(element, this.head);
            this.head = newEntry;
            size++;
        }
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){
        if (size ==0){
            return null;
        }
        T temp = this.head.entry;
        this.head = this.head.next;
        size--;
        return temp;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek(){
        if (size ==0){
            return null;
        }
        return (T)this.head.entry;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size(){
        return size;
    }
}
