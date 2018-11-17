package cn.edu.zjut.myong.nlp;

import cn.edu.zjut.myong.com.weibo.util.SimHash;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Corpus implements Serializable {

    private static final long serialVersionUID = 3906769363641694894L;

    private List<String> document;
    private List<List<String>> corpus;
    private List<Long> corpusHash;

    public Corpus(List<String> docs) {
        this.document = new LinkedList<>();
        this.corpus = new LinkedList<>();
        this.corpusHash = new LinkedList<>();
        this.addDocument(docs);
    }

    public void addDocument(List<String> docs) {
        System.out.println("Adding document in Corpus");
        for (String line : docs) {
            List<String> doc = ChineseSegmenter.parse(line, ChineseSegmenter.Segmenter.Stanford);

            // System.out.println(doc);

            if (doc.isEmpty()) {
                continue;
            }

            // long hash1 = SimHash.simHash64(doc);

            boolean isSimilar = false;
            /*
            for (int i = corpusHash.size()-1; i >= Math.max(corpusHash.size()-5, 0); i--) {
                long hash2 = corpusHash.get(i);
                if (SimHash.similarity(hash1, hash2) > 0.8) {
                    isSimilar = true;
                    break;
                }
            }
            */

            if (!isSimilar) {
                this.document.add(line);
                this.corpus.add(doc);
                this.corpusHash.add(0L);
                // System.out.println(corpus.size());
            }
        }
    }

    public int size() {
        if (document.size() == corpus.size()) {
            return corpus.size();
        } else {
            return -1;
        }
    }

    public String getDocument(int i) {
        if (i >= 0 && i < document.size()) {
            return document.get(i);
        } else {
            return "";
        }
    }

    public List<String> getCorpus(int i) {
        if (i >= 0 && i < corpus.size()) {
            return corpus.get(i);
        } else {
            return new ArrayList<>();
        }
    }

    public void save(String dictFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dictFile));
        oos.writeObject(this);
        oos.close();
    }

    public static Corpus load(String dictFile) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dictFile));
        Corpus corpus = (Corpus) ois.readObject();
        ois.close();
        return corpus;
    }

    public void print() {
        for (int i = 0; i < corpus.size(); i++) {
            System.out.println(i + " " + corpus.get(i));
        }
    }
}
