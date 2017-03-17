package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexFromIndexCreator {

    public static void deldocsTerm(String indexin, String indexout,
	    String deldocsField, String deldocsTerm) {
	
	Term term = new Term(deldocsField, deldocsTerm);

	try {
	    Directory dir = null;
	    if (indexout != null){
		dir = FSDirectory.open(Paths.get(indexout));
	    } else {
		dir = FSDirectory.open(Paths.get(indexin));
	    }
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	    iwc.setOpenMode(OpenMode.CREATE);
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    if (indexout != null) {
	    writer.addIndexes(FSDirectory.open(Paths.get(indexin)));
	    }
	    writer.deleteDocuments(term);
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
    
    public static void deldocsQuery(String indexin, String indexout,
	    String queryString) {
	
	Term term = new Term("HOLA");
	//Query query = 

	try {
	    Directory dir = null;
	    if (indexout != null){
		dir = FSDirectory.open(Paths.get(indexout));
	    } else {
		dir = FSDirectory.open(Paths.get(indexin));
	    }
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	    iwc.setOpenMode(OpenMode.CREATE);
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    if (indexout != null) {
	    writer.addIndexes(FSDirectory.open(Paths.get(indexin)));
	    }
	    writer.deleteDocuments(term);
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

}
