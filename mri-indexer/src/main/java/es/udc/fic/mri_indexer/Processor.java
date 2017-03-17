package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Processor {

    public static void IdfTerms(String indexFile, String field, int n,
	    boolean asc) throws IOException {

	Directory dir = null;
	DirectoryReader indexReader = null;

	dir = FSDirectory.open(Paths.get(indexFile));
	indexReader = DirectoryReader.open(dir);
	int numberDocuments = indexReader.numDocs();
	List<TermIdf> listTerms = new ArrayList<>();
	Map<String, Integer> termMap = new HashMap<>();

	termMap = getTermFrequencies(indexReader, field);

	@SuppressWarnings("rawtypes")
	Iterator it = termMap.entrySet().iterator();
	while (it.hasNext()) {
	    @SuppressWarnings("rawtypes")
	    Map.Entry pair = (Map.Entry) it.next();
	    final double idf = Math
		    .log((float) numberDocuments / ((int) pair.getValue()));
	    listTerms.add(new TermIdf((String) pair.getKey(), idf));
	}
	printIdfTerms(listTerms, n, field, asc);
    }

    public static void TfIdfTerms(String indexFile, String field, int n,
	    boolean asc) throws IOException {
	Directory dir = null;
	DirectoryReader indexReader = null;

	dir = FSDirectory.open(Paths.get(indexFile));
	indexReader = DirectoryReader.open(dir);
	int numberDocuments = indexReader.numDocs();
	List<TermTfIdf> listTerms = new ArrayList<>();
	Map<String, Integer> termMap = new HashMap<>();
	Map<String, Integer> termDocMap = new HashMap<>();

	termMap = getTermFrequencies(indexReader, field);

	for (int i = 0; i < numberDocuments; i++) {
	    
	    indexReader = DirectoryReader.open(dir);

	    
	    termDocMap = getTermDocFrequencies(indexReader, i, field);
	    
	    @SuppressWarnings("rawtypes")
	    Iterator it = termMap.entrySet().iterator();
	    while (it.hasNext()) {
		@SuppressWarnings("rawtypes")
		Map.Entry pair = (Map.Entry) it.next();
		final double idf = Math
			.log((float) numberDocuments / ((int) pair.getValue()));
		String termName = (String) pair.getKey();
		final int tf = termDocMap.get(termName);
		listTerms.add(new TermTfIdf(termName,i,idf,tf,(tf*idf)));
	    }
	}
	printTfIdfTerms(listTerms, n, field, asc);
    }

    private static Map<String, Integer> getTermFrequencies(
	    IndexReader indexReader, String field) throws IOException {

	Map<String, Integer> termMap = new HashMap<>();

	for (final LeafReaderContext leaf : indexReader.leaves()) {
	    try (LeafReader leafReader = leaf.reader()) {

		final Fields fields = leafReader.fields();

		final Terms terms = fields.terms(field);
		final TermsEnum termsEnum = terms.iterator();

		while (termsEnum.next() != null) {
		    final String tt = termsEnum.term().utf8ToString();
		    final int f = termsEnum.docFreq();
		    if (termMap.containsKey(tt)) {
			int lastF = termMap.get(tt);
			termMap.put(tt, (lastF + f));
		    } else {
			termMap.put(tt, f);
		    }
		}

	    }

	}
	return termMap;
    }

    private static Map<String, Integer> getTermDocFrequencies(
	    IndexReader reader, int docId, String field) throws IOException {
	Terms vector = reader.getTermVector(docId, field);

	TermsEnum termsEnum = null;
	termsEnum = vector.iterator();
	Map<String, Integer> frequencies = new HashMap<>();

	while (termsEnum.next() != null) {
	    String term = termsEnum.term().utf8ToString();
	    int freq = (int) termsEnum.totalTermFreq();
	    frequencies.put(term, freq);
	}
	return frequencies;
    }

    private static void printIdfTerms(List<TermIdf> list, int n, String field,
	    boolean asc) {

	if (asc) {
	    Collections.sort(list);
	    System.out.println("\nBest_idf of " + field + ":");
	} else {
	    Collections.sort(list, new Comparator<TermIdf>() {
		@Override
		public int compare(TermIdf a, TermIdf b) {
		    return b.compareTo(a);
		}
	    });
	    System.out.println("\nPoor_idf of " + field + ":");
	}

	int size = list.size();
	if (n > size) {
	    n = size;
	}

	for (int i = 0; i < n; i++) {
	    System.out.println(
		    "Nº=" + (i + 1) + "	" + list.get(i).toString());
	}

    }
    
    private static void printTfIdfTerms(List<TermTfIdf> list, int n, String field,
	    boolean asc) {

	if (asc) {
	    Collections.sort(list);
	    System.out.println("\nBest_tfidf of " + field + ":");
	} else {
	    Collections.sort(list, new Comparator<TermTfIdf>() {
		@Override
		public int compare(TermTfIdf a, TermTfIdf b) {
		    return b.compareTo(a);
		}
	    });
	    System.out.println("\nPoor_tfidf of " + field + ":");
	}

	int size = list.size();
	if (n > size) {
	    n = size;
	}

	for (int i = 0; i < n; i++) {
	    System.out.println(
		    "Nº=" + (i + 1) + "	" + list.get(i).toString());
	}

    }

}
