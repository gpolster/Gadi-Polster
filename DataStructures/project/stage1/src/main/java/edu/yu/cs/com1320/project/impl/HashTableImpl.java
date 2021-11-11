package edu.yu.cs.com1320.project.impl;


import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {
    private int tableSize;



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

    private int hashFunction(Key key){
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
        int counter = 0;
        Entry<?,?> current = this.table[index];
        if(current != null) {
            while (current != null && !current.key.equals(k)) {
                counter++;
                current = current.next;
            }
            if (current != null) {
                return current.value;
            }
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
        int index = this.hashFunction(k);
        Entry old = this.table[index];
        Value oldValue = get(k);
        Entry<Key,Value> putEntry = new Entry<Key,Value>(k,v, (Entry)this.table[index]);
        this.table[index] = putEntry;
        if(old != null){
            return oldValue;
            //return (Value) old.value;
        } else {
            tableSize += 1;
            return null;
        }
    }
    private void remove(Key key){
        int bucket = hashFunction(key);
        if (this.table[bucket] != null) {
            if (this.table[bucket].key == key) {
                this.table[bucket] = this.table[bucket].next;
                tableSize -= 1;
            } else {
                Entry current = this.table[bucket];
                while (current.next != null && current.next.key != key) {
                    current = current.next;
                }
                if (current.next != null) {
                    current.next = current.next.next;
                    tableSize -= 1;
                }
            }
        }
    }
}
