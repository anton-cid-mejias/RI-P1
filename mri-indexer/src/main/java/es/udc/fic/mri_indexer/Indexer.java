package es.udc.fic.mri_indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
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

    public void run() throws IOException{
	try {
	    System.out.println("Indexing to directory '" + index + "'...");
        	
	    Directory dir = FSDirectory.open(Paths.get(index));
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        	
	    iwc.setOpenMode(openmode);
        
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    
	    Path docDir = null;
	    for (String docsPath : colls){
		docDir = Paths.get(docsPath);
		indexDocs(writer, docDir);
	    }
	    
        	
	    writer.close();
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
		"\n with message: " + e.getMessage());
	}
    }
    
    
    //
    
    static void indexDocs(final IndexWriter writer, Path path)
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

    static boolean check_sgm(Path file) {
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

    /** Indexes a single document */
    static void indexDoc(IndexWriter writer, Path file, long lastModified)
	    throws IOException {
	
	//ESTO HACE FALTA EL PARSER DE ANTON
	
	try (InputStream stream = Files.newInputStream(file)) {
	    Document doc = new Document();

	    // Add the path of the file as a field named "path". Use a
	    // field that is indexed (i.e. searchable), but don't tokenize
	    // the field into separate words and don't index term frequency
	    // or positional information:
	    Field pathSgm = new StringField("PathSgm", file.toString(),
		    Field.Store.YES);
	    doc.add(pathSgm);

	    // Add the last modified date of the file a field named "modified".
	    // Use a LongPoint that is indexed (i.e. efficiently filterable with
	    // PointRangeQuery). This indexes to milli-second resolution, which
	    // is often too fine. You could instead create a number based on
	    // year/month/day/hour/minutes/seconds, down the resolution you
	    // require.
	    // For example the long value 2011021714 would mean
	    // February 17, 2011, 2-3 PM.
	    doc.add(new LongPoint("modified", lastModified));

	    // Add the contents of the file to a field named "contents". Specify
	    // a Reader,
	    // so that the text of the file is tokenized and indexed, but not
	    // stored.
	    // Note that FileReader expects the file to be in UTF-8 encoding.
	    // If that's not the case searching for special characters will
	    // fail.
	    doc.add(new TextField("contents", new BufferedReader(
		    new InputStreamReader(stream, StandardCharsets.UTF_8))));

	    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		// New index, so we just add the document (no old document can
		// be there):
		System.out.println("adding " + file);
		writer.addDocument(doc);
	    } else {
		// Existing index (an old copy of this document may have been
		// indexed) so
		// we use updateDocument instead to replace the old one matching
		// the exact
		// path, if present:
		System.out.println("updating " + file);
		writer.updateDocument(new Term("path", file.toString()), doc);
	    }
	}
    }
    
    
    
    //
    
    
}
