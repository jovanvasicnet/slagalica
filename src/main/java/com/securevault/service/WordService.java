package com.securevault.service;

import java.io.*;
import java.util.*;

public class WordService {

    public static Map<Integer,List<String>> wordsByLength = new HashMap<>();

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

                int len = word.length();

                if(len >= 3 && len <= 12){

                    wordsByLength
                            .computeIfAbsent(len,k->new ArrayList<>())
                            .add(word);

                }

            }

            System.out.println("Dictionary loaded");

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static boolean exists(String word){

        List<String> list = wordsByLength.get(word.length());

        if(list == null) return false;

        return list.contains(word);
    }

}