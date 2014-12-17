package com.oloftus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String DB_CHARSET = "ASCII";
    private static final String DB_PATH = "/Users/oloftus/Desktop/addresses.txt";
    private static final String[] PERMITTED_CHARS = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z 1 2 3 4 5 6 7 8 9 0"
            .split(" ");
    private static final int NGRAM_SIZE = 3;

    private static List<String> db = new ArrayList<>();
    private static List<String> origDb = new ArrayList<>();
    private static List<String> ngrams = new ArrayList<>();
    private static byte[][] cmm;

    public static int charIx(String chara) {

        for (int i = 0; i < PERMITTED_CHARS.length; i++) {
            if (PERMITTED_CHARS[i].equals(chara)) {
                return i;
            }
        }

        return -1;
    }

    public static void main(String[] args) throws IOException {

        makeNGrams();
        normaliseDb();
        makeCmm();
        populateCmm();

        Scanner scanner = new Scanner(System.in);
        String ngram = scanner.nextLine().replace(" ", "");

        int ngramId = 0;
        for (int j = 0; j < NGRAM_SIZE; j++) {
            int charIx = charIx(ngram.substring(j, j + 1));
            ngramId += charIx * Math.pow(PERMITTED_CHARS.length, (NGRAM_SIZE - j - 1));
        }

        byte[] cumul = new byte[db.size()];

        for (int i = 0; i < cmm[ngramId].length; i++) {
            cumul[i] += cmm[ngramId][i];
        }

        int maxSoFarCount = 0;
        List<Integer> maxSoFarNums = new ArrayList<>();
        for (int i = 0; i < cmm[ngramId].length; i++) {
            if (cumul[i] == maxSoFarCount) {
                maxSoFarNums.add(i);
            }
            else if (cumul[i] > maxSoFarCount) {
                maxSoFarNums.clear();
                maxSoFarNums.add(i);
                maxSoFarCount = cumul[i];
            }
        }

        Object[] dbArr = origDb.toArray();
        for (int address : maxSoFarNums) {
            System.out.println(dbArr[address]);
        }
    }

    private static void populateCmm() {

        int lineNum = 0;
        for (String line : db) {
            for (int i = 0; i + NGRAM_SIZE <= line.length(); i++) {
                String ngram = line.substring(i, i + NGRAM_SIZE);

                int ngramId = 0;
                for (int j = 0; j < NGRAM_SIZE; j++) {
                    int charIx = charIx(ngram.substring(j, j + 1));
                    ngramId += charIx * Math.pow(PERMITTED_CHARS.length, (NGRAM_SIZE - j - 1));
                }
                cmm[ngramId][lineNum] = 1;
                // System.out.println(ngram + "-" + ngramId);
            }

            lineNum++;
        }
    }

    private static void makeCmm() {

        // [Rows][Cols]
        cmm = new byte[ngrams.size()][db.size()]; // Rows: Ngrams Cols:
                                                  // Addresses
    }

    private static void normaliseDb() throws FileNotFoundException, IOException {

        File dbFile = new File(DB_PATH);
        FileInputStream fs = new FileInputStream(dbFile);
        Path dbPath = FileSystems.getDefault().getPath(DB_PATH);
        List<String> dbLines = Files.readAllLines(dbPath, Charset.forName(DB_CHARSET));
        for (String line : dbLines) {
            origDb.add(line);
            db.add(line.replace(" ", ""));
        }
    }

    private static void makeNGrams() {

        int pcl = PERMITTED_CHARS.length;

        for (int i = 0; i < pcl; i++) {
            String char1 = PERMITTED_CHARS[i];
            for (int j = 0; j < pcl; j++) {
                String char2 = PERMITTED_CHARS[j];
                for (int k = 0; k < pcl; k++) {
                    String char3 = PERMITTED_CHARS[k];
                    ngrams.add(char1 + char2 + char3);
                }
            }
        }
    }
}
