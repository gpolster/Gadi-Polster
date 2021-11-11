package edu.yu.cs.com1320.project.stage2;


import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HashTableImplStage1Tests {

    @Test
    public void hashTableImplSimplePutAndGet() {
        HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
        hashTable.put(1,2);
        hashTable.put(3,6);
        hashTable.put(8,14);
        int x = hashTable.get(1);
        int y = hashTable.get(3);
        int z = hashTable.get(8);
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
        assertEquals(2, x);
        assertEquals(6, y);
        assertEquals(14, z);
    }

    @Test
    public void hashTableImplALotOfInfoTest() {
        HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
        for (int i = 0; i<1000; i++) {
            hashTable.put(i,2*i);
        }
        for (int i = 0; i<1000; i++) {
            int aa = hashTable.get(i);
            assertEquals(2*i, aa);
        }
    }
    @Test
    public void hashTableImplCollisionTest() {
        HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
        hashTable.put(1, 9);
        hashTable.put(6,12);
        hashTable.put(11,22);
        int a = hashTable.get(1);
        int b = hashTable.get(6);
        int c = hashTable.get(11);
        assertEquals(9, a);
        assertEquals(12, b);
        assertEquals(22, c);
    }

    @Test
    public void hashTableImplReplacementTest() {
        HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
        hashTable.put(1,2);
        int a = hashTable.put(1, 3);
        assertEquals(2, a);
        int b = hashTable.put(1, 4);
        assertEquals(3,b);
        int c = hashTable.put(1, 9);
        assertEquals(4, c);
    }
    @Test
    public void hashTableDelNullPut() {
        HashTable<String,Integer> hashTable = new HashTableImpl<String,Integer>();

        hashTable.put("Defied", (Integer)22345);
        Integer test1a = hashTable.get("Defied");
        assertEquals(test1a, (Integer)22345);
        hashTable.put("Defied", null);
        Integer test1b = hashTable.get("Defied");
        assertEquals(test1b,null);
        hashTable.put("Oakland", 87123);

        Integer test2a = hashTable.get("Oakland");
        assertEquals(test2a, (Integer)87123);
        hashTable.put("Oakland", null);
        hashTable.get("Oakland");
        Integer test2b = hashTable.get("Oakland");
        assertEquals(test2b,null);

        hashTable.put("Sanguine", (Integer)4682);
        Integer test3a = hashTable.get("Sanguine");
        assertEquals(test3a, (Integer)4682);
        hashTable.put("Sanguine", null);
        hashTable.get("Sanguine");
        Integer test3b = hashTable.get("Sanguine");
        assertEquals(test3b,null);
    }

    @Test
    public void HashEqualButNotEqual() {
        HashTable<String,Integer> hashTable = new HashTableImpl<String,Integer>();

        hashTable.put("tensada", 3521);
        hashTable.put("friabili", 1253);
        Integer test1a = hashTable.get("tensada");
        assertEquals(test1a, (Integer)3521);
        Integer test1b = hashTable.get("friabili");
        assertEquals(test1b, (Integer)1253);

        hashTable.put("abyz", 8948);
        hashTable.put("abzj", 84980);
        Integer test2a = hashTable.get("abyz");
        assertEquals(test2a, (Integer)8948);
        Integer test2b = hashTable.get("abzj");
        assertEquals(test2b, (Integer) 84980);

        hashTable.put("Siblings", 27128);
        hashTable.put("Teheran", 82172);
        Integer test3a = hashTable.get("Siblings");
        assertEquals(test3a, (Integer)27128);
        Integer test3b = hashTable.get("Teheran");
        assertEquals(test3b, (Integer)82172);

    }
    @Test
    public void hashTableImplReplacementTest2() {
        HashTable<Integer, Integer> hashTable = new HashTableImpl<Integer, Integer>();
        hashTable.put(1, 2);
        p("passed Test: hashTableImplReplacementTest2");
    }



    @Test
    public void basicCollision() {
        HashTable<Integer, String> hashTable = new HashTableImpl<Integer, String>();
        hashTable.put(1, "Avi");
        hashTable.put(5, "dinsky");
        hashTable.put(6, "Radinsky");
        hashTable.put(11, "gami");
        assertEquals("gami", hashTable.put(11, "gthir"));
        assertEquals("gthir", hashTable.get(11));
        assertEquals("Avi", hashTable.get(1));
        assertEquals("Radinsky", hashTable.get(6));
        p("passed Test: basicCollision");
    }
    private void p(String s) {
        System.out.println(s);
    }
    @Test
    public void basicGetAndPut() {
        HashTableImpl<Integer, Integer> table = new HashTableImpl<>();
        table.put(1, 1);
        assertEquals(1, (int)table.get(1));
        assertEquals(1, (int)table.put(1, 2));
        assertNull(table.put(2, 2));
        assertEquals(2, (int)table.get(2));
        assertNull(table.get(100));
    }
    @Test
    public void basicSameCell() {
        HashTableImpl<Integer, Integer> table = new HashTableImpl<>();
        table.put(0, 0);
        table.put(5, 5);
        assertEquals(0, (int)table.get(0));
        assertEquals(5, (int)table.get(5));
        assertEquals(5, (int)table.put(5, 10));
        assertEquals(10, (int)table.get(5));
        assertEquals(10, (int)table.put(5, 100));
        System.out.println("getting key 5: "+ (int)table.get(5));
        assertNull(table.put(10, 1000));
        assertEquals(0, (int)table.put(0, 1));
        assertEquals(1, (int)table.get(0));
        table.put(1, 1);
        System.out.println("getting key 5 before putting k 1: "+ (int)table.get(5));
        assertEquals(1, (int)table.put(1, 6));
        System.out.println("getting key 5 after i think the array doubles: "+ (int)table.get(5));
        assertEquals(6, (int)table.get(1));
        System.out.println("getting key 5: "+ (int)table.get(5));
        assertEquals(100, (int)table.put(5, 5));
    }
    @Test
    public void testGetAndPut() {
        HashTableImpl<Integer, Integer> table = new HashTableImpl<>();
        for (int i=0; i<1000; i++) {
            assertNull(table.put(i, i));
        }
        for (int i=0; i<1000; i++) {
            assertEquals(i, (int)table.get(i));
        }
        for (int i=0; i<1000; i++) {
            assertEquals(i, (int)table.put(i, i+1));
        }
        for (int i=0; i<100; i++) {
            assertEquals(i+1, (int)table.get(i));
        }
    }
    private HashTable<String,String> table;

    @BeforeEach
    public void initTable(){
        this.table = new HashTableImpl<>();
        this.table.put("Key1", "Value1");
        this.table.put("Key2","Value2");
        this.table.put("Key3","Value3");
        this.table.put("Key4","Value4");
        this.table.put("Key5","Value5");
        this.table.put("Key6","Value6");
    }
    @Test
    public void testGet() {
        assertEquals("Value1",this.table.get("Key1"));
        assertEquals("Value2",this.table.get("Key2"));
        assertEquals("Value3",this.table.get("Key3"));
        assertEquals("Value4",this.table.get("Key4"));
        assertEquals("Value5",this.table.get("Key5"));
    }
    @Test
    public void testGetChained() {
        //second node in chain
        assertEquals("Value6",this.table.get("Key6"));
        //second node in chain after being modified
        this.table.put("Key6","Value6+1");
        assertEquals("Value6+1",this.table.get("Key6"));
        //check that other values still come back correctly
        testGet();
    }
    @Test
    public void testGetMiss() {
        assertEquals(null,this.table.get("Key20"));
    }
    @Test
    public void testPutReturnValue() {
        assertEquals("Value3",this.table.put("Key3","Value3+1"));
        assertEquals("Value6",this.table.put("Key6", "Value6+1"));
        assertEquals(null,this.table.put("Key7","Value7"));
    }
    @Test
    public void testGetChangedValue () {
        HashTableImpl<String, String> table = new HashTableImpl<String, String>();
        String key1 = "hello";
        String value1 = "how are you today?";
        String value2 = "HI!!!";
        table.put(key1, value1);
        assertEquals(value1,table.get(key1));
        table.put(key1, value2);
        assertEquals(value2,table.get(key1));
    }
    @Test
    public void testDeleteViaPutNull() {
        HashTableImpl<String, String> table = new HashTableImpl<String, String>();
        String key1 = "hello";
        String value1 = "how are you today?";
        String value2 = null;
        table.put(key1, value1);
        table.put(key1, value2);
        assertEquals(value2,table.get(key1));
    }
    @Test
    public void testSeparateChaining () {
        HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
        for(int i = 0; i <= 23; i++) {
            table.put(i, "entry " + i);
        }
        assertEquals("entry 12",table.put(12, "entry 12+1"));
        assertEquals("entry 12+1",table.get(12));
        assertEquals("entry 23",table.get(23));
    }
}

