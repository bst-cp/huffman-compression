package com.example.bst.hcoder;

/**
 * Created by Taha on 11.11.2017.
 */
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HuffmanDecode {
    static Node root = new Node();

    public void generateTree(String codebook){

        String[] lines = codebook.split("(\\.\\.)");
        Node tmpNode = root;
        for(int j = 0; j < lines.length; j++ ) {
            for(int i = 2; i < lines[j].length(); i++){
                if(lines[j].charAt(i) == '0'){
                    if(tmpNode.left == null)
                        tmpNode.left = new Node();
                    if(tmpNode.right == null)
                        tmpNode.right = new Node();
                    tmpNode = tmpNode.left;
                }
                else{
                    if(tmpNode.right == null)
                        tmpNode.right = new Node();
                    if(tmpNode.left == null)
                        tmpNode.left = new Node();
                    tmpNode = tmpNode.right;
                }
            }
            tmpNode.data = lines[j].charAt(0);
            tmpNode = root;
        }
    }
    public String decode(String encoded, String sender){
        String decoded = "";
        Node tmpNode;
        for (int i = 0; i < encoded.length(); ) {
            tmpNode = root;
            while (tmpNode.left != null && tmpNode.right != null && i < encoded.length()) {
                if (encoded.charAt(i) == '1')
                    tmpNode = tmpNode.right;
                else tmpNode = tmpNode.left;
                i++;
            }
            if (tmpNode != null)
                decoded += tmpNode.data;
        }
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }
        if (isPresent) {
            try {
                File file = new File(docsFolder.getAbsolutePath(),sender + ".txt");
                FileWriter writer = new FileWriter(file);
                writer.append(decoded);
                writer.flush();
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }

        } else {
            // Failure
        }
        return decoded;
    }
}
class Node{
    public Node left, right;
    public char data;

    public Node(){
        left = null;
        right = null;
    }
}