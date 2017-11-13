package com.example.bst.hcoder;

/**
 * Created by Bora on 11.11.2017.
 */

public class HuffmanLeaf extends HuffmanTree {
    public final char value; // the character this leaf represents

    public HuffmanLeaf(int freq, char val) {
        super(freq);
        value = val;
    }
}
