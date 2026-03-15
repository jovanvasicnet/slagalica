package com.securevault.service;

import java.io.*;
import java.util.*;

public class WordService {

    public static List<String> words = new ArrayList<>();

    public static void loadWords(){

        try{

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            WordService.class.getResourceAsStream("/sr_50k.txt")
                    )
            );

            String line;

            while((line = br.readLine()) != null){

                String[] parts = line.split("\\s+");

                String word = parts[0].toUpperCase();

                if(word.length() >= 3 && word.length() <= 12){

                    words.add(word);

                }

            }

            System.out.println("Loaded words: " + words.size());

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}