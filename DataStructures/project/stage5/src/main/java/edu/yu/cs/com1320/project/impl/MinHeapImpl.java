package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap {
    //
    public MinHeapImpl(){
        elements = new Comparable[10];
        //this.arrayIndex = 0;
    }

    @Override
    public void reHeapify(Comparable element) {
        MinHeap<E> heap = new MinHeapImpl<>();
        for(Comparable n : elements){
            if (n != null) {
                heap.insert((E) n);
            }
        }
        for(int i = 1; i < getElementsSize(); i++){
            elements[i] = heap.remove();
        }
    }
    //throws no such element exception if element doesnt exist
    @Override
    protected int getArrayIndex(Comparable element) {
        int index = 1;

        while(index <= getElementsSize() && !this.elements[index].equals(element)){
            index += 1;
        }
        if (this.elements[index].equals(element)){
            return index;
        }
        throw new NoSuchElementException("element not in elementS");
    }

    @Override
    protected void doubleArraySize() {
        Comparable[] oldArray = this.elements;
        elements = new Comparable[2* oldArray.length];
        for (int i = 0; i < oldArray.length; i++) {
            elements[i] = oldArray[i];
        }
    }
    private int getElementsSize(){
        int size = 1;
        while (elements[size] != null){
            size +=1;
        }
        return size;
    }
}
