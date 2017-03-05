package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    private OpenMode openmode;
    private String index;
    private String coll;

    public Indexer(OpenMode openmode, String index, String coll) {
	this.openmode = openmode;
	this.index = index;
	this.coll = coll;
    }

    public void run() throws IOException{
	System.out.println("Indexing to directory '" + index + "'...");
	
	Directory dir = FSDirectory.open(Paths.get(index));
	Analyzer analyzer = new StandardAnalyzer();
	IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	
	iwc.setOpenMode(openmode);
	
	IndexWriter writer = new IndexWriter(dir, iwc);
    }
}
