package com.core.lambdaapp;

import androidx.collection.ArrayMap;
import androidx.collection.SimpleArrayMap;

import java.util.HashMap;

public class StructuresManager {
    private HashMap<String, Integer> littleHashMap;
    private HashMap<String, Integer> bigHashMap;
    private SimpleArrayMap<String, Integer> littleSimpleArrayMap;
    private SimpleArrayMap<String, Integer> bigSimpleArrayMap;
    private ArrayMap<String, Integer> littleArrayMap;
    private ArrayMap<String, Integer> bigArrayMap;

    public StructuresManager() {
        littleHashMap = new HashMap<String, Integer>();
        bigHashMap = new HashMap<String, Integer>();
        littleArrayMap = new ArrayMap<String, Integer>();
        bigArrayMap = new ArrayMap<String, Integer>();
        initializeStructures();
    }


    private void initializeStructures() {

            for (int i=0; i<100; i++) {
                littleHashMap.put(String.valueOf(i), i);
                littleArrayMap.put(String.valueOf(i), i);
            }

            for (int i=0; i<1000; i++) {
                bigHashMap.put(String.valueOf(i), i);
                bigArrayMap.put(String.valueOf(i), i);
            }
            System.out.println("Initialisations finies");
    }
}
