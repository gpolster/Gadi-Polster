package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {
    private int tableSize;
    private final double maxLoadFactor = .75;


    class Entry<K,V>{
        Key key;
        Value value;
        private Entry<K,V> next;
        Entry(Key k, Value v, Entry next){
            if(k== null){
                throw new IllegalArgumentException("key was null");
            }
            key = k;
            value = v;
            this.next = next;
        }
    }
    private Entry<?,?>[] table;
    public HashTableImpl() {
        this.table = new Entry[5];
        this.tableSize = 5;
    }
    public HashTableImpl(int size){
        this.table = new Entry[size];
        this.tableSize = size;
    }

    private int hashFunction(Object key){
        //return Math.abs(key.hashCode()) % this.table.length;
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }


    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k){
        int index = this.hashFunction(k);
        Entry returnValue = findEntry(this.table[index], k);
        if (returnValue != null){
            return (Value) returnValue.value;
        }
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     * To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v){
        if (v == null){
            this.remove(k);
            return null;
        }
        if (loadFactor() >= maxLoadFactor){
            rehash();
        }
        int index = this.hashFunction(k);
        Entry<?,?> current = findEntry(this.table[index], k);
        Value oldValue = current == null ? null : current.value;
        if (current != null){
            current.value=v;
        } else {
            Entry<Key, Value> putEntry = new Entry<Key, Value>(k, v, (Entry) this.table[index]);
            this.table[index] = putEntry;
            tableSize += 1;
        }
        return oldValue;
    }

    private void remove(Key key){
        int bucket = hashFunction(key);
        if (this.table[bucket] != null) {
            if (this.table[bucket].key.equals(key)) {
                this.table[bucket] = this.table[bucket].next;
                tableSize -= 1;
            } else {
                Entry current = this.table[bucket];
                while (current.next != null && !current.next.key.equals(key)) {
                    current = current.next;
                }
                if (current.next != null) {
                    current.next = current.next.next;
                    tableSize -= 1;
                }
            }
        }
    }
    private double loadFactor(){
        return (double) tableSize/table.length;
    }
    private void rehash(){
        Entry<?,?>[] oldTable = table;
        table = new Entry[2* oldTable.length];
        for (int i = 0; i < oldTable.length; i++){
            Entry<?, ?> current = oldTable[i];
            oldTable[i]=null;
            while(current != null){
                this.put(current.key, current.value);
                current = current.next;
            }
        }
    }
    private Entry findEntry(Entry current, Key key){
        while (current != null && !current.key.equals(key)) {
            current = current.next;
        }
        return current;
    }
}