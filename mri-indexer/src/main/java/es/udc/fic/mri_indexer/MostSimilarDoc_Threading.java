package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class MostSimilarDoc_Threading {

    private static class TitleThread implements Runnable {

	private final int initialDoc;
	private final int finalDoc;
	private final DirectoryReader reader;
	private final IndexWriter writer;

	public TitleThread(int initialDoc, int finalDoc, DirectoryReader reader,
		IndexWriter writer) {
	    this.initialDoc = initialDoc;
	    this.finalDoc = finalDoc;
	    this.reader = reader;
	    this.writer = writer;
	}

	@Override
	public void run() {
	    Document doc = null;
	    int hits = 0;
	    Document queryDoc = null;
	    Query query = null;
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TopDocs topDocs = null;
	    Field SimPathSgmField = null;
	    Field SimTitle = null;
	    Field SimBody = null;
	    String SimPathSgmString = null;
	    String SimTitleString = null;
	    String SimBodyString = null;
	    MultiFieldQueryParser parser = new MultiFieldQueryParser(
		    new String[] { "TITLE", "BODY" }, new StandardAnalyzer());

	    try {
		for (int i = initialDoc; i < finalDoc; i++) {
		    doc = reader.document(i);

		    try {
			query = parser
				.parse(QueryParser.escape(doc.get("TITLE")));
			topDocs = searcher.search(query, 2);
			hits = topDocs.totalHits;
		    } catch (ParseException e) {
			hits = 0;
		    }

		    if (hits == 0) {
			SimPathSgmString = "";
			SimTitleString = "";
			SimBodyString = "";
		    } else {
			queryDoc = reader.document(topDocs.scoreDocs[0].doc);
			// Test if the answer is the original document
			if ((doc.get("PathSgm").equals(queryDoc.get("PathSgm")))
				&& (doc.get("SeqDocNumber").equals(
					queryDoc.get("SeqDocNumber")))) {
			    if (topDocs.totalHits == 1) {
				SimPathSgmString = "";
				SimTitleString = "";
				SimBodyString = "";
			    } else {
				queryDoc = reader
					.document(topDocs.scoreDocs[1].doc);
				SimPathSgmString = queryDoc.get("PathSgm");
				SimTitleString = queryDoc.get("TITLE");
				SimBodyString = queryDoc.get("BODY");
			    }
			} else {
			    SimPathSgmString = queryDoc.get("PathSgm");
			    SimTitleString = queryDoc.get("TITLE");
			    SimBodyString = queryDoc.get("BODY");
			}

		    }
		    SimPathSgmField = new StringField("SimPathSgm",
			    SimPathSgmString, Field.Store.YES);
		    SimTitle = new TextField("SimTitle", SimTitleString,
			    Field.Store.YES);
		    SimBody = new TextField("SimBody", SimBodyString,
			    Field.Store.YES);
		    doc.add(SimPathSgmField);
		    doc.add(SimTitle);
		    doc.add(SimBody);

		    writer.addDocument(doc);
		    System.out.println("Processed Document number: " + i);
		}
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
    }

    private static class BodyThread implements Runnable {

	private final int initialDoc;
	private final int finalDoc;
	private final DirectoryReader reader;
	private final IndexWriter writer;
	private final int n_best_terms;
	private final Map<String, Integer> termMap;

	public BodyThread(int initialDoc, int finalDoc, DirectoryReader reader,
		IndexWriter writer, int n_best_terms,
		Map<String, Integer> termMap) {
	    this.initialDoc = initialDoc;
	    this.finalDoc = finalDoc;
	    this.reader = reader;
	    this.writer = writer;
	    this.n_best_terms = n_best_terms;
	    this.termMap = termMap;
	}

	@Override
	public void run() {
	    Document doc = null;
	    Document queryDoc = null;
	    Query query = null;
	    String queryString = null;
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TopDocs topDocs = null;
	    Field SimPathSgmField = null;
	    Field SimTitle = null;
	    Field SimBody = null;
	    Field SimQuery = null;
	    String SimPathSgmString = null;
	    String SimTitleString = null;
	    String SimBodyString = null;
	    String terms = null;
	    int hits = 0;
	    MultiFieldQueryParser parser = new MultiFieldQueryParser(
		    new String[] { "TITLE", "BODY" }, new StandardAnalyzer());

	    try {
		for (int i = initialDoc; i < finalDoc; i++) {
		    doc = reader.document(i);
		    terms = Processor.getBestTerms(reader, i, "BODY",
			    n_best_terms, termMap);
		    
		    try {
			query = parser
				.parse(QueryParser.escape(terms));
			queryString = query.toString();
			topDocs = searcher.search(query, 2);
			hits = topDocs.totalHits;
		    } catch (ParseException e) {
			queryString = "EMPTY BODY";
			hits = 0;
		    }

		    if (hits == 0) {
			SimPathSgmString = "";
			SimTitleString = "";
			SimBodyString = "";
		    } else {
			queryDoc = reader.document(topDocs.scoreDocs[0].doc);
			// Test if the answer is the original document
			if ((doc.get("PathSgm").equals(queryDoc.get("PathSgm")))
				&& (doc.get("SeqDocNumber").equals(
					queryDoc.get("SeqDocNumber")))) {
			    if (topDocs.totalHits == 1) {
				SimPathSgmString = "";
				SimTitleString = "";
				SimBodyString = "";
			    } else {
				queryDoc = reader
					.document(topDocs.scoreDocs[1].doc);
				SimPathSgmString = queryDoc.get("PathSgm");
				SimTitleString = queryDoc.get("TITLE");
				SimBodyString = queryDoc.get("BODY");
			    }
			} else {
			    SimPathSgmString = queryDoc.get("PathSgm");
			    SimTitleString = queryDoc.get("TITLE");
			    SimBodyString = queryDoc.get("BODY");
			}

		    }
		    SimPathSgmField = new StringField("SimPathSgm",
			    SimPathSgmString, Field.Store.YES);
		    SimTitle = new TextField("SimTitle", SimTitleString,
			    Field.Store.YES);
		    SimBody = new TextField("SimBody", SimBodyString,
			    Field.Store.YES);
		    SimQuery = new StringField("SimQuery", queryString,
			    Field.Store.YES);

		    doc.add(SimPathSgmField);
		    doc.add(SimTitle);
		    doc.add(SimBody);
		    doc.add(SimQuery);

		    writer.addDocument(doc);
		    System.out.println("Processed Document number: " + i);
		}
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
    }

    public static void startThreads(String indexin, String indexout,
	    Integer n_best_terms, Integer number_threads) {
	try {
	    Directory indir = FSDirectory.open(Paths.get(indexin));
	    DirectoryReader reader = DirectoryReader.open(indir);

	    Directory outdir = FSDirectory.open(Paths.get(indexout));
	    IndexWriterConfig iwc = new IndexWriterConfig(
		    new StandardAnalyzer());
	    iwc.setOpenMode(OpenMode.CREATE);
	    IndexWriter writer = new IndexWriter(outdir, iwc);

	    // Each thread will have the first and last document they need
	    // to
	    // process
	    int numDocs = reader.numDocs();
	    int threadDocs = numDocs / number_threads;
	    int lastThreadDocs = numDocs - (threadDocs * (number_threads - 1));
	    int[] threadDocRange = new int[number_threads + 1];

	    threadDocRange[0] = 0;
	    for (int i = 1; i <= number_threads; i++) {
		threadDocRange[i] = threadDocRange[i - 1] + threadDocs;
	    }
	    threadDocRange[number_threads] = threadDocRange[number_threads - 1]
		    + lastThreadDocs;

	    final ExecutorService executor = Executors
		    .newFixedThreadPool(number_threads);

	    // If n_best_terms is null we are using most similart title,
	    // else,
	    // most similar body
	    if (n_best_terms == null) {
		for (int j = 0; j <= number_threads - 1; j++) {
		    final Runnable worker = new TitleThread(threadDocRange[j],
			    threadDocRange[j + 1], reader, writer);
		    executor.execute(worker);
		}
	    } else {
		if (n_best_terms > 512) {
		    // Boolean Query cannot accept more than 1024 terms
		    // each term is used two times
		    n_best_terms = 512;
		}
		Map<String, Integer> termMap = Processor
			.getTermFrequencies(reader, "BODY");
		for (int j = 0; j <= number_threads - 1; j++) {
		    final Runnable worker = new BodyThread(threadDocRange[j],
			    threadDocRange[j + 1], reader, writer, n_best_terms,
			    termMap);
		    executor.execute(worker);
		}
	    }

	    executor.shutdown();
	    try {
		executor.awaitTermination(1, TimeUnit.HOURS);
	    } catch (final InterruptedException e) {
		e.printStackTrace();
		System.exit(-1);
	    }

	    reader.close();
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

}
