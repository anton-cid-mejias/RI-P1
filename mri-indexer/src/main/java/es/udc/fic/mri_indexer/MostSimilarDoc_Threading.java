package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
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
	    Document queryDoc = null;
	    BooleanQuery query = null;
	    BooleanQuery.Builder bqBuilder = null;
	    Query query1 = null;
	    Query query2 = null;
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TopDocs topDocs = null;
	    Field SimPathSgmField = null;
	    Field SimTitle = null;
	    Field SimBody = null;
	    StringTokenizer st = null;
	    String token = null;

	    try {
		for (int i = initialDoc; i < finalDoc; i++) {
		    doc = reader.document(i);

		    bqBuilder = new BooleanQuery.Builder();
		    st = new StringTokenizer(doc.get("TITLE"));
		    while (st.hasMoreTokens()) {
			token = st.nextToken();
			query1 = new TermQuery(new Term("TITLE", token));
			query2 = new TermQuery(new Term("BODY", token));
			bqBuilder.add(query1, Occur.SHOULD);
			bqBuilder.add(query2, Occur.SHOULD);
		    }
		    query = bqBuilder.build();

		    topDocs = searcher.search(query, 1);

		    if (topDocs.totalHits == 0) {
			SimPathSgmField = new StringField("SimPathSgm", "",
				Field.Store.YES);
			SimTitle = new TextField("SimTitle", "",
				Field.Store.YES);
			SimBody = new TextField("SimBody", "", Field.Store.YES);
		    } else {
			queryDoc = reader.document(topDocs.scoreDocs[0].doc);
			SimPathSgmField = new StringField("SimPathSgm",
				queryDoc.get("PathSgm"), Field.Store.YES);
			SimTitle = new TextField("SimTitle",
				queryDoc.get("TITLE"), Field.Store.YES);
			SimBody = new TextField("SimBody", queryDoc.get("BODY"),
				Field.Store.YES);
		    }
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
		IndexWriter writer, int n_best_terms, Map<String, Integer> termMap) {
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
	    BooleanQuery query = null;
	    BooleanQuery.Builder bqBuilder = null;
	    Query query1 = null;
	    Query query2 = null;
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TopDocs topDocs = null;
	    Field SimPathSgmField = null;
	    Field SimTitle = null;
	    Field SimBody = null;
	    Field SimQuery = null;
	    List<String> terms = null;

	    try {
		for (int i = initialDoc; i < finalDoc; i++) {
		    doc = reader.document(i);

		    bqBuilder = new BooleanQuery.Builder();
		    terms = Processor.getBestTerms(reader, i, "BODY", n_best_terms, termMap);
		    for(String token : terms){
			query1 = new TermQuery(new Term("TITLE", token));
			query2 = new TermQuery(new Term("BODY", token));
			bqBuilder.add(query1, Occur.SHOULD);
			bqBuilder.add(query2, Occur.SHOULD);
		    }

		    query = bqBuilder.build();

		    topDocs = searcher.search(query, 1);

		    if (topDocs.totalHits == 0) {
			SimPathSgmField = new StringField("SimPathSgm", "",
				Field.Store.YES);
			SimTitle = new TextField("SimTitle", "",
				Field.Store.YES);
			SimBody = new TextField("SimBody", "", Field.Store.YES);
		    } else {
			queryDoc = reader.document(topDocs.scoreDocs[0].doc);
			SimPathSgmField = new StringField("SimPathSgm",
				queryDoc.get("PathSgm"), Field.Store.YES);
			SimTitle = new TextField("SimTitle",
				queryDoc.get("TITLE"), Field.Store.YES);
			SimBody = new TextField("SimBody", queryDoc.get("BODY"),
				Field.Store.YES);
		    }
		    SimQuery = new StringField("SimQuery", query.toString(),
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

	    // Each thread will have the first and last document they need to
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
	    
	    //If n_best_terms is null we are using most similart title, else, most similar body
	    if (n_best_terms == null) {
		for (int j = 0; j <= number_threads - 1; j++) {
		    final Runnable worker = new TitleThread(threadDocRange[j],
			    threadDocRange[j + 1], reader, writer);
		    executor.execute(worker);
		}
	    } else {
		Map<String, Integer> termMap = Processor.getTermFrequencies(reader, "BODY");
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
		System.exit(-2);
	    }

	    reader.close();
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

}
