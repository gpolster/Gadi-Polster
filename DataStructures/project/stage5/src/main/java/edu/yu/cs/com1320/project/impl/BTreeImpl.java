package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static java.lang.System.nanoTime;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private static final int MAX = 4;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private Set<Key> diskSet = new HashSet<>();
    private PersistenceManager<Key, Value> pm = (PersistenceManager<Key, Value>) new DocumentPersistenceManager(null);


    //B-tree node data type
    private static final class Node {
        private int entryCount; // number of entries
        Entry[] entries = new Entry[BTreeImpl.MAX]; // the array of children
        // create a node with k entries
        Node(int k) {
            this.entryCount = k;
        }
    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
     static class Entry {
        Comparable key;
        Object val;
        Node child;

         Entry(Comparable key, Object val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }
    }

    public BTreeImpl(){
        this.root = new Node(0);
    }

    @Override
    public Value get(Key key){
        if (key == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, key, this.height);
        if(diskSet.remove(key)){
            try {
                Value v = pm.deserialize(key);
                put(key,v);
                return v;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(entry != null) {

                return (Value) entry.val;

//            try{
//                return pm.deserialize(key);
//            } catch(Exception e){
//                return null;
//            }
        }
        return null;
    }

    private Entry get(Node currentNode, Key key, int height) {
        Entry[] entries = currentNode.entries;
        if (height == 0) {
            for (int j = 0; j < currentNode.entryCount; j++) {
                if(isEqual(key, (Key)entries[j].key)) {
                    return entries[j];
                }
            }
            return null;
        }
        else {
            for (int j = 0; j < currentNode.entryCount; j++) {
                if (j + 1 == currentNode.entryCount || less(key, (Key) entries[j + 1].key)) {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            return null;
        }
    }



    @Override
    public Value put(Key key, Value val) {
        if (key == null) {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        if (val == null){
            try { pm.delete(key); } catch (IOException e) { e.printStackTrace(); }
        }
        if(get(key) != null){
            try { pm.delete(key); } catch (IOException e) { e.printStackTrace(); }
        }
        Entry alreadyThere = this.get(this.root, key, this.height);
        if(alreadyThere != null) {
            alreadyThere.val = val;
            return (Value) alreadyThere.val;
        }
        Node newNode = this.put(this.root, key, val, this.height);
        this.n++;
        if (newNode == null) {
            return val;
        }
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry((Key) this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry((Key) newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return val;
    }
    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node put(Node currentNode, Key key, Value val, int height) {
        int j;
        Entry newEntry = new Entry(key, val, null);
        if (height == 0) {
            for (j = 0; j < currentNode.entryCount; j++) { if (less(key, (Key) currentNode.entries[j].key)) { break; } }
        }
        else {
            for (j = 0; j < currentNode.entryCount; j++) {
                if ((j + 1 == currentNode.entryCount) || less(key, (Key) currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX) { return null; } else { return this.split(currentNode, height); }
    }

    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeImpl.MAX / 2; j++) {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        return newNode;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Key k1, Key k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean isEqual(Key k1, Key k2) {
        return k1.compareTo(k2) == 0;
    }

    @Override
    public void moveToDisk(Key k) throws Exception {
        Value val = get(k);
        put(k, null);
        pm.serialize(k, val);
        Entry alreadyThere = this.get(this.root, k, this.height);
        Function<Key, Value> reference = (Key k2) -> {
            Value v = null;
            try {
                v = pm.deserialize(k);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return v;
        };
        if(alreadyThere != null) {
            alreadyThere.val = null;
            this.diskSet.add(k);
            return;
        }
        Node newNode = this.put(this.root, k, null, this.height);
        this.diskSet.add(k);
        if (newNode == null) {
            return;
        }
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry((Key) this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry((Key) newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1

    }

    @Override
    public void setPersistenceManager(PersistenceManager pm) {
        this.pm = pm;
    }
}
