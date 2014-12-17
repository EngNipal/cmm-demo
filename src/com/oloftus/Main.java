package com.oloftus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String DB_FILENAME = "paf.txt";
    private static final String DB_CHARSET = "ASCII";
    private static final Map<String, Integer> PERMITTED_CHAR_INDEXES;
    private static final String PERMITTED_CHARS_STR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String[] PERMITTED_CHARS = PERMITTED_CHARS_STR.split("");
    private static final int NGRAM_SIZE = 3;
    private static final int NUM_NGRAMS = (int) Math.pow(PERMITTED_CHARS.length, NGRAM_SIZE);

    private static boolean[][] cmm;
    private static String[] pafDb;

    static {

        PERMITTED_CHAR_INDEXES = new HashMap<>();

        for (int i = 0; i < PERMITTED_CHARS.length; i++) {
            PERMITTED_CHAR_INDEXES.put(PERMITTED_CHARS[i], i);
        }
    }

    public static void main(String[] args) throws Exception {

        processPaf();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Ready for input: ");

                String ngram = scanner.nextLine().replace(" ", "").toUpperCase();
                processInput(ngram);
            }
        }
    }

    private static void processInput(String ngram) {

        if (ngram.length() < NGRAM_SIZE) {
            System.out.println("Please enter at least " + NGRAM_SIZE + " characters\n");
            return;
        }

        int ngramId;
        try {
            ngramId = ngramId(ngram);
        }
        catch (IllegalArgumentException e) {
            System.out.println("Illegal character entered. Legal characters are: " + PERMITTED_CHARS_STR + "\n");
            return;
        }

        findMatchingAddresses(ngramId);
    }

    private static void findMatchingAddresses(int ngramId) {

        byte[] addressNgramSum = new byte[pafDb.length];
        for (int i = 0; i < cmm[ngramId].length; i++) {
            addressNgramSum[i] += cmm[ngramId][i] ? 1 : 0;
        }

        int maxAddressNgramCount = 0;
        List<Integer> mostLikelyAddresses = new ArrayList<>();
        for (int i = 0; i < cmm[ngramId].length; i++) {
            if (addressNgramSum[i] == maxAddressNgramCount) {
                mostLikelyAddresses.add(i);
            }
            else if (addressNgramSum[i] > maxAddressNgramCount) {
                mostLikelyAddresses.clear();
                mostLikelyAddresses.add(i);
                maxAddressNgramCount = addressNgramSum[i];
            }
        }

        if (maxAddressNgramCount == 0) {
            System.out.println("No addresses found\n");
            return;
        }

        System.out.println(mostLikelyAddresses.size() + " addresses found:");
        for (int address : mostLikelyAddresses) {
            System.out.println(pafDb[address]);
        }
        System.out.println("");
    }

    private static void processPaf() throws FileNotFoundException, IOException, Exception {

        System.out.print("Processing PAF... ");
        readPaf();
        populateCmm();
        System.out.println("DONE");
    }

    private static void populateCmm() throws Exception {

        cmm = new boolean[NUM_NGRAMS][pafDb.length];

        for (int addressId = 0; addressId < pafDb.length; addressId++) {
            String address = pafDb[addressId].replace(" ", "");

            for (int ngramWindow = 0; ngramWindow + NGRAM_SIZE <= address.length(); ngramWindow++) {
                String ngram = address.substring(ngramWindow, ngramWindow + NGRAM_SIZE);

                int ngramId;
                try {
                    ngramId = ngramId(ngram);
                }
                catch (IllegalArgumentException e) {
                    System.out.println(ngram);
                    throw new Exception("Illegal character in the PAF. Legal characters are: " + PERMITTED_CHARS_STR);
                }

                cmm[ngramId][addressId] = true;
            }
        }
    }

    private static int ngramId(String ngram) {

        int ngramId = 0;
        for (int pos = 0; pos < NGRAM_SIZE; pos++) {
            String character = ngram.substring(pos, pos + 1);
            Integer charIndex = PERMITTED_CHAR_INDEXES.get(character);
            if (charIndex == null) {
                throw new IllegalArgumentException("Illegal character");
            }
            ngramId += charIndex * Math.pow(PERMITTED_CHARS.length, (NGRAM_SIZE - pos - 1));
        }

        return ngramId;
    }

    private static void readPaf() throws FileNotFoundException, IOException {

        Path dbPath = FileSystems.getDefault().getPath(DB_FILENAME);
        List<String> lines = Files.readAllLines(dbPath, Charset.forName(DB_CHARSET));
        pafDb = new String[lines.size()];
        lines.toArray(pafDb);
    }
}
