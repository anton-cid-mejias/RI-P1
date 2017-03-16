package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Processor {

    public static void IdfTerms(String indexFile, String field, int n, boolean asc)
	    throws IOException {

	Directory dir = null;
	DirectoryReader indexReader = null;

	dir = FSDirectory.open(Paths.get(indexFile));
	indexReader = DirectoryReader.open(dir);
	int numberDocuments = indexReader.numDocs();
	List<TermIdf> listTerms = new ArrayList<>();

	for (final LeafReaderContext leaf : indexReader.leaves()) {
	    try (LeafReader leafReader = leaf.reader()) {

		final Fields fields = leafReader.fields();

		final Terms terms = fields.terms(field);
		final TermsEnum termsEnum = terms.iterator();
		
		while (termsEnum.next() != null) {
			final String tt = termsEnum.term().utf8ToString();
			final int f = termsEnum.docFreq();
			final double idf= Math.log(numberDocuments/f);
			
			listTerms.add(new TermIdf(tt,idf));

		}
	    }
	 }
	printIdfTerms(listTerms,n,asc);
    }

    public static void bestTfIdfTerms(String indexFile, String field, int n) {

    }

    public static void poorTfIdfTerms(String indexFile, String field, int n) {

    }
    
    private static void printIdfTerms(List<TermIdf> list, int n, boolean asc){
	
	if (asc){
	    Collections.sort(list);
	}else{
	    Collections.sort(list,new Comparator<TermIdf>() {
		    @Override
		    public int compare(TermIdf a, TermIdf b) {
		        return b.compareTo(a);
		    }
		});
	}
	
	int size = list.size();
	if (n>size){
	    n = size;
	}
	
	for (int i=0; i < n ; i++){
	    System.out.println("Nº=" +(i+1)+ "	" + list.get(i).toString());
	}
	
    }

}
