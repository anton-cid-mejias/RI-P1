package es.udc.fic.mri_indexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
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
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    public static void run(OpenMode openmode, String index, List<String> colls)
	    throws IOException {
	try {
	    System.out.println("Indexing to directory '" + index + "'...");

	    Directory dir = FSDirectory.open(Paths.get(index));
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	    iwc.setOpenMode(openmode);

	    IndexWriter writer = new IndexWriter(dir, iwc);

	    for (String coll : colls) {
		indexDocs(writer, Paths.get(coll));
	    }

	    writer.close();
	} catch (IOException e) {
	    System.out.println(" caught a " + e.getClass() + "\n with message: "
		    + e.getMessage());
	}
    }

    public static void run(OpenMode openmode, String index, String coll)
	    throws IOException {
	try {
	    System.out.println("Indexing to directory '" + index + "'...");

	    Directory dir = FSDirectory.open(Paths.get(index));
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	    iwc.setOpenMode(openmode);

	    IndexWriter writer = new IndexWriter(dir, iwc);

	    indexDocs(writer, Paths.get(coll));

	    writer.close();
	} catch (IOException e) {
	    System.out.println(" caught a " + e.getClass() + "\n with message: "
		    + e.getMessage());
	}
    }

    // This method is public for Indexes2 to use
    public static void indexDocs(final IndexWriter writer, Path path)
	    throws IOException {
	if (Files.isDirectory(path)) {
	    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
		@Override
		public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) throws IOException {
		    try {
			if (checkSgm(file)) {
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
	    if (checkSgm(path)) {
		indexDoc(writer, path,
			Files.getLastModifiedTime(path).toMillis());
	    }
	}
    }

    private static void indexDoc(IndexWriter writer, Path file,
	    long lastModified) throws IOException {
	try (InputStream stream = Files.newInputStream(file)) {
	    List<List<String>> reuters = Reuters21578Parser
		    .parseString(new StringBuffer(toString(stream)));

	    /*
	     * FieldType t1 = new FieldType();
	     * t1.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
	     * t1.setTokenized(true); t1.setStored(true);
	     * t1.setStoreTermVectors(true); t1.freeze();
	     * 
	     * FieldType t2 = new FieldType(); t2.setStored(true);
	     * t2.setStoreTermVectors(true);
	     * t2.setIndexOptions(IndexOptions.DOCS_AND_FREQS); t2.freeze();
	     */

	    int number = 1;
	    for (List<String> reuter : reuters) {
		Document doc = new Document();

		// Path of the file indexed
		Field pathsgmField = new StringField("PathSgm", file.toString(),
			Field.Store.YES);
		// Field pathsgmField = new Field("PathSgm", file.toString(),
		// t1);
		doc.add(pathsgmField);
		// Order number in the document
		Field seqDocNumberField = new StringField("SeqDocNumber",
			Integer.toString(number), Field.Store.YES);
		doc.add(seqDocNumberField);
		// Field seqDocNumberField = new IntPoint("SeqDocNumber",
		// number);

		// doc.add(new StoredField("StoredSeqDocNumber", number));
		number++;
		// TITLE of the reuter
		Field title = new TextField("TITLE", reuter.get(0),
			Field.Store.YES);
		// Field title = new Field("TITLE", reuter.get(0), t1);
		doc.add(title);
		// BODY of the reuter
		Field body = new TextField("BODY", reuter.get(1),
			Field.Store.YES);
		// Field body = new Field("BODY", reuter.get(1), t1);
		doc.add(body);
		// TOPICS of the reuter
		Field topics = new TextField("TOPICS", reuter.get(2),
			Field.Store.YES);
		// Field topics = new Field("TOPICS", reuter.get(2), t1);
		doc.add(topics);
		// DATELINE of the reuter
		Field dateline = new TextField("DATELINE", reuter.get(3),
			Field.Store.YES);
		// Field dateline = new Field("DATELINE", reuter.get(3), t1);
		doc.add(dateline);
		// DATE of the reuter
		Field date = new StringField("DATE", processDate(reuter.get(4)),
			Field.Store.YES);
		// Field date = new Field("DATE", processDate(reuter.get(4)),
		// t2);
		doc.add(date);
		// Hostname who execute the thread
		Field hostname = new StringField("Hostname",
			InetAddress.getLocalHost().getHostName(),
			Field.Store.YES);
		// Field hostname = new Field("Hostname",
		// InetAddress.getLocalHost().getHostName(), t2);
		doc.add(hostname);
		// Thread executed
		Field thread = new StringField("Thread",
			Thread.currentThread().getName(), Field.Store.YES);
		// Field thread = new Field("Thread",
		// Thread.currentThread().getName(), t2);
		doc.add(thread);

		System.out.println(
			"adding " + file + " : REUTER " + (number - 1));
		writer.addDocument(doc);

	    }
	}

    }

    private static String toString(InputStream stream) throws IOException {
	ByteArrayOutputStream result = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];
	int length;
	while ((length = stream.read(buffer)) != -1) {
	    result.write(buffer, 0, length);
	}
	return result.toString("UTF-8");
    }

    private static String processDate(String date) {
	Date parsedDate = null;
	final SimpleDateFormat format = new SimpleDateFormat(
		"dd-MMM-yyyy HH:mm:ss.SS", Locale.US);

	try {
	    parsedDate = format.parse(date);
	} catch (ParseException e) {
	    System.out.println("Date field was not correct");
	    e.printStackTrace();
	}
	String luceneDateString = DateTools.dateToString(parsedDate,
		DateTools.Resolution.MILLISECOND);
	return luceneDateString;
    }

    private static boolean checkSgm(Path file) {
	//Check if the file is like reut2-xxx.sgm, being x a number

	String extension = "";
	String fileName = file.toString();
	int i = fileName.lastIndexOf('.');
	int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
	if (i > p) {
	    extension = fileName.substring(i + 1);
	}
	//Check if file extension is sgm
	if (extension.equalsIgnoreCase("sgm")) {
	    fileName = fileName.substring(p + 1);
	    if (fileName.length() != 13) {
		return false;
	    }
	    char[] charArray = fileName.toCharArray();
	    if ((charArray[0] != 'r') || (charArray[1] != 'e')
		    || (charArray[2] != 'u') || (charArray[3] != 't')
		    || (charArray[4] != '2') || (charArray[5] != '-')) {
		return false;
	    }
	    //Check the xxx numbers
	    try  {
		Integer.parseInt(String.valueOf(charArray[6]));
		Integer.parseInt(String.valueOf(charArray[7]));
		Integer.parseInt(String.valueOf(charArray[8]));
	    } catch (NumberFormatException e){
		return false;
	    }
	    return true;
	}
	return false;
    }

}
