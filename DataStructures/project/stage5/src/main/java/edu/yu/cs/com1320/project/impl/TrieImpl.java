package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private final int ALPHABET_SIZE = 36;
    private Node root;
    private List<String> keyList = new ArrayList<>();
    class Node<Value> {
        private List<Value> valList = new ArrayList<>();
        private Node<?>[] links = new Node[ALPHABET_SIZE];
    }
    public TrieImpl() {}
    /**
     * add the given value at the given key
     * @param key
     * @param val
     */

    @Override
    public void put(String key, Value val) {
        if (key == null){
            throw new IllegalArgumentException("key was null, thats bad");
        }
        if (val == null) {
            return;
        }
        if (key == ""){
            return;
        }
        else {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node put(Node x, String key, Value val, int d) {
        key = key.toLowerCase();
        //create a new node
        if (x == null) {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length()) {
            if (!x.valList.contains(val)) {
                x.valList.add(val);
            }
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        if (isValidInteger(c)){
            x.links[c-22] = this.put(x.links[c-22], key, val, d + 1);
        } else {
            x.links[c - 97] = this.put(x.links[c - 97], key, val, d + 1);
        }
        return x;
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key
     * @param comparator used to sort  values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List getAllSorted(String key, Comparator<Value> comparator) {
        if (key == null){
            throw new IllegalArgumentException("key was null");
        }
        if (comparator==null){
            throw new IllegalArgumentException("comparator was null, cant sort with null");
        }
        Node x = this.get(this.root, key, 0);
        if (x == null){
            return new ArrayList();
        }
        List<Value> valist = new ArrayList<>();
        valist = x.valList;
        Collections.sort(valist, comparator);

        return valist;
    }

    private Node get(String key){
        return this.get(this.root, key, 0);
    }
    private Node get(Node x, String key, int d) {
        key = key.toLowerCase();
        //link was null - return null, indicating a miss
        if (x == null) {
            return null;
        }
//        if (key == null){
//            return null;
//        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length() || key == "") {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
//        if (c == 32){
//            return x;
//        }
        if (isValidInteger(c)){
            return this.get(x.links[c-22], key, d + 1);
        }
        return this.get(x.links[c-97], key, d + 1);
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (prefix == null){
            throw new IllegalArgumentException("prefix was null!");
        }
        if (comparator == null){
            throw new IllegalArgumentException("comparator was null!");
        }
        if (prefix == ""){
            return new ArrayList();
        }
        prefix= prefix.toLowerCase();
        List<String> prefixList = new ArrayList<>();
        Set<Value> setList = new HashSet<>();
        List<Value> returnList = new ArrayList<>();
        collect(get(this.root,prefix,0), prefix,prefixList);
        for (String key: prefixList){
            setList.addAll(this.getAllSorted(key, comparator));
        }
        returnList.addAll(setList);
        Collections.sort(returnList, comparator);
        return returnList;
    }
    private void collect(Node x, String prefix, List<String> prefixList){
        prefix= prefix.toLowerCase();
        if (x == null){
            return;
        }
        if(!x.valList.isEmpty()){
            if (!prefixList.contains(prefix) && prefix != null) {
                prefixList.add(prefix);
            }
        }
        for (char c = 97; c < 123; c++){
            collect(x.links[c-97],prefix + c,prefixList );
        }
        for (char c = 48; c < 58; c++){
            collect(x.links[c-22],prefix + c,prefixList );
        }
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set deleteAllWithPrefix(String prefix) {
        if (prefix == null){
            throw new IllegalArgumentException("prefix was null!");
        }
        if (prefix == ""){
            return new HashSet();
        }
        prefix= prefix.toLowerCase();
        List<String> prefixList = new ArrayList<>();
        Set<Value> returnList = new HashSet<>();
        collect(get(this.root, prefix,0), prefix,prefixList);
        for (String word : prefixList){
            returnList.addAll(this.deleteAll(word));
        }
        return returnList;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set deleteAll(String key) {
        if (key==null){
            throw new IllegalArgumentException("key was null");
        }
        key = key.toLowerCase();
        Set<Value> returnSet = new HashSet<>();
        Node node = get(this.root, key, 0);
        if (node == null){
            return returnSet;
        }
        Set<Value> loopSet = new HashSet<>();
        loopSet.addAll((ArrayList<Value>)node.valList);
        for(Value val : loopSet){
            Value returnVal = delete(key,val);
            if (returnVal != null) {
                returnSet.add(returnVal);
            }
        }
        return returnSet;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {
        if (key==null){
            throw new IllegalArgumentException("key was null");
        }
        if (val==null){
            throw new IllegalArgumentException("val was null");
        }
        if (key == ""){
            return null;
        }
        key=key.toLowerCase();
        Value valToReturn = null;
        for (Value value : (ArrayList<Value>)get(this.root,key,0).valList){
            if (val.equals(value)){
                valToReturn = value;
            }
        }
        Node deletedValNode = delete(this.root, key, val, 0);
        this.root = deletedValNode;
        if(deletedValNode==null){
            return null;
        } else{
            return valToReturn;
        }
    }
    private Node delete(Node x, String key, Value val, int d){
        key=key.toLowerCase();
        if (x == null) {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()) {
            x.valList.remove(val);
        } else { //continue down the trie to the target node
            char c = key.charAt(d);
            if (isValidInteger(c)){
                x.links[c-22] = this.delete(x.links[c-22], key, val, d + 1);
            } else {
                x.links[c - 97] = this.delete(x.links[c - 97], key, val, d + 1);
            }
        }
        //this node has a val – do nothing, return the node
        if (!x.valList.isEmpty()) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c <this.ALPHABET_SIZE; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        } //empty - set this link to null in the parent
        return null;
    }
    private boolean isValidInteger(char c){
        if (c > 47 && c < 58){
            return true;
        }
        return false;
    }

}
