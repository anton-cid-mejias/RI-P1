package es.udc.fic.mri_indexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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

    public void run() throws IOException {
	System.out.println("Indexing to directory '" + index + "'...");

	Directory dir = FSDirectory.open(Paths.get(index));
	Analyzer analyzer = new StandardAnalyzer();
	IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	iwc.setOpenMode(openmode);

	IndexWriter writer = new IndexWriter(dir, iwc);
    }

    private void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
	try (InputStream stream = Files.newInputStream(file)) {
	   List<List<String>> reuters = Reuters21578Parser
		    .parseString(new StringBuffer(this.toString(stream)));
	   
	   int number = 1; 
	   for (List<String> reuter : reuters){
	       Document doc = new Document();
	       //Path of the file indexed
	       Field pathsgmField = new StringField("Pathsgm", file.toString(), Field.Store.YES);
	       doc.add(pathsgmField);
	       //Order number in the document
	       Field seqDocNumberField = new IntPoint("SeqDocNumber",number);
	       doc.add(seqDocNumberField);
	       number ++;
	       //TITLE of the reuter
	       Field title = new TextField("TITLE", reuter.get(0), Field.Store.YES);
	       doc.add(title);
	       //BODY of the reuter
	       Field body = new TextField("BODY", reuter.get(1), Field.Store.YES);
	       doc.add(body);
	       //TOPICS of the reuter
	       Field topics = new TextField("TOPICS", reuter.get(2), Field.Store.YES);
	       doc.add(topics);
	       //DATELINE of the reuter
	       Field dateline = new TextField("DATELINE", reuter.get(3), Field.Store.YES);
	       doc.add(dateline);
	       //DATE of the reuter
	       Field date = new StringField("DATE", this.processDate(reuter.get(4)), Field.Store.YES);
	       doc.add(date);
	       
	       if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		   System.out.println("adding " + file);
		   writer.addDocument(doc);
	       } else {
		   System.out.println("updating " + file);
		   writer.updateDocument(new Term("path", file.toString()), doc);
	       }


	   }
	}
	
    }

    private String toString(InputStream stream) throws IOException {
	ByteArrayOutputStream result = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];
	int length;
	while ((length = stream.read(buffer)) != -1) {
	    result.write(buffer, 0, length);
	}
	return result.toString("UTF-8");
    }
    
    private String processDate(String date){
	return null;
    }
}
