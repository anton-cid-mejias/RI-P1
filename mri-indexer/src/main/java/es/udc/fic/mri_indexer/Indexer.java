package es.udc.fic.mri_indexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
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
    private List<String> colls;

    public Indexer(OpenMode openmode, String index, List<String> colls) {
	this.openmode = openmode;
	this.index = index;
	this.colls = colls;
    }

    public void run() throws IOException {
	try {
	    System.out.println("Indexing to directory '" + index + "'...");

	    Directory dir = FSDirectory.open(Paths.get(index));
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	    iwc.setOpenMode(openmode);

	    IndexWriter writer = new IndexWriter(dir, iwc);

	    Path docDir = null;
	    for (String docsPath : colls) {
		docDir = Paths.get(docsPath);
		indexDocs(writer, docDir);
	    }

	    writer.close();
	} catch (IOException e) {
	    System.out.println(" caught a " + e.getClass() + "\n with message: "
		    + e.getMessage());
	}
    }



    private void indexDocs(final IndexWriter writer, Path path)
	    throws IOException {
	if (Files.isDirectory(path)) {
	    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
		@Override
		public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) throws IOException {
		    try {
			if (check_sgm(file)) {
			    indexDoc(writer, file,
				    attrs.lastModifiedTime().toMillis());
			}
		    } catch (IOException ignore) {
			// don't index files that can't be read.
		    }
		    return FileVisitResult.CONTINUE;
		}
	    });
	} else {
	    indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	}
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
	Date parsedDate = null;
	SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SS");
	try {
	    parsedDate = format.parse(date);
	} catch (ParseException e) {
	    System.out.println("Date field was not correct");
	    e.printStackTrace();
	}
	String luceneDateString = DateTools.dateToString(parsedDate, DateTools.Resolution.MILLISECOND);
	return luceneDateString;
    }
    
    static boolean check_sgm(Path file) {
	// reut2-xxx.sgm? de momento acepta cualquier .sgm
	
	String extension = "";
	String fileName = file.toString();
	int i = fileName.lastIndexOf('.');
	int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
	if (i > p) {
	    extension = fileName.substring(i + 1);
	}
	if (extension.equalsIgnoreCase("sgm")) {
	    return true;
	}
	return false;
    }

}
