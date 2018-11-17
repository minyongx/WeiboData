package cn.edu.zjut.myong.nlp;

import org.apache.commons.collections4.CollectionUtils;

import java.io.*;
import java.util.*;

public class Dictionary implements Serializable {

    private static final long serialVersionUID = 7149500440339311066L;

    public int corpusSize;
    public List<String> termList;
    public List<Integer> termFrequency;
    public Map<String, Integer> termMap;

    public Dictionary(Corpus corpus, int threshold) {
        System.out.println("Building dictionary Count");
        Map<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < corpus.size(); i++) {
            Set<String> uniqueTerms = new HashSet<>(corpus.getCorpus(i));
            for (String term : uniqueTerms) {
                String t = term.trim().toLowerCase();
                if (!t.isEmpty()) {
                    if (!counter.containsKey(t)) {
                        counter.put(t, 0);
                    }
                    counter.put(t, counter.get(t) + 1);
                }
            }
        }

        System.out.println("Building dictionary TermList");
        corpusSize = corpus.size();
        termList = new ArrayList<>();
        for (String item : counter.keySet()) {
            if (counter.get(item) >= threshold) {
                termList.add(item);
            }
        }
        Collections.sort(termList);

        System.out.println("Building dictionary termFrequency");
        termFrequency = new ArrayList<>();
        termMap = new HashMap<>();
        for (int i = 0; i < termList.size(); i++) {
            termFrequency.add(i, counter.get(termList.get(i)));
            termMap.put(termList.get(i), i);
        }
    }

    public int size() {
        if (termList.size() != termMap.size() || termList.size() != termFrequency.size()) {
            return -1;
        } else {
            return termList.size();
        }
    }

    public int getTermIndex(String item) {
        return termMap.getOrDefault(item, -1);
    }

    public String getTerm(int index) {
        if (index >= 0 && index < termList.size()) {
            return termList.get(index);
        } else {
            return "";
        }
    }

    public double inverseDocumentFrequency(int index) {
        if (index >= 0 && index < termFrequency.size()) {
            return -Math.log((double) termFrequency.get(index) / corpusSize);
        } else {
            return -1.0;
        }
    }

    public Set<Integer> booleanExpression(List<String> terms) {
        Set<Integer> expr = new HashSet<>();
        for (String term : terms) {
            String key = term.trim().toLowerCase();
            if (termMap.containsKey(key)) {
                expr.add(termMap.get(key));
            }
        }
        return expr;
    }

    public Map<Integer, Integer> basicTFExpression(List<String> terms) {
        Map<Integer, Integer> expr = new HashMap<>();
        for (String term : terms) {
            String key = term.trim().toLowerCase();
            if (termMap.containsKey(key)) {
                int index = termMap.get(key);
                if (!expr.containsKey(index)) {
                    expr.put(index, 0);
                }
                expr.put(index, expr.get(index) + 1);
            }
        }
        return expr;
    }

    public Map<Integer, Double> basicTFIDFExpression(List<String> terms) {
        Map<Integer, Double> expr = new HashMap<>();
        for (String term : terms) {
            String key = term.trim().toLowerCase();
            if (termMap.containsKey(key)) {
                int index = termMap.get(key);
                if (!expr.containsKey(index)) {
                    expr.put(index, 0.0);
                }
                expr.put(index, expr.get(index) + 1);
            }
        }
        for (int index : expr.keySet()) {
            expr.put(index, expr.get(index) * inverseDocumentFrequency(index));
        }
        return expr;
    }

    public void save(String dictFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dictFile));
        oos.writeObject(this);
        oos.close();
    }

    public static Dictionary load(String dictFile) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dictFile));
        Dictionary dictionary = (Dictionary) ois.readObject();
        ois.close();
        return dictionary;
    }

    public void print() {
        for (int i = 0; i < termList.size(); i++) {
            System.out.println(i + " " + termList.get(i) + " " + termFrequency.get(i));
        }
    }
}
