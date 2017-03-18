package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class MostSimilarDoc_TitleThreading {

    private static class WorkerThread implements Runnable {

	private final int initialDoc;
	private final int finalDoc;
	private final DirectoryReader reader;
	private final IndexWriter writer;

	public WorkerThread(int initialDoc, int finalDoc,
		DirectoryReader reader, IndexWriter writer) {
	    this.initialDoc = initialDoc;
	    this.finalDoc = finalDoc;
	    this.reader = reader;
	    this.writer = writer;
	}

	@Override
	public void run() {
	    Document doc = null;
	    Document queryDoc = null;
	    String queryString = null;
	    BooleanQuery query = null;
	    BooleanQuery.Builder bqBuilder = null;
	    Query query1 = null;
	    Query query2 = null;
	    QueryParser parser = new QueryParser("TITLE",
		    new StandardAnalyzer());
	    IndexSearcher searcher = new IndexSearcher(reader);
	    //
	    MoreLikeThis mlt = new MoreLikeThis(reader);
	    mlt.setAnalyzer(new StandardAnalyzer());
	    String[] fieldNames = new String[2];
	    fieldNames[0] = "TITLE";
	    fieldNames[1] = "BODY";
	    mlt.setFieldNames(fieldNames);
	    //
	    TopDocs topDocs = null;
	    Field SimPathSgmField = null;
	    Field SimTitle = null;
	    Field SimBody = null;

	    try {
		for (int i = initialDoc; i <= finalDoc; i++) {
		    doc = reader.document(i);
		    /*
		     * Esto consigue que sea busqueda exacta queryString =
		     * "TITLE:" + "\"" + doc.get("TITLE") +"\"" + " AND BODY:" +
		     * "\"" + doc.get("TITLE") +"\""; query =
		     * parser.parse(queryString);
		     */

		    //
		    bqBuilder = new BooleanQuery.Builder();
		    StringTokenizer st = new StringTokenizer(doc.get("TITLE"));
		    while (st.hasMoreTokens()) {
			String token = st.nextToken();
			query1 = new TermQuery(new Term("TITLE", token));
			query2 = new TermQuery(new Term("BODY", token));
			bqBuilder.add(query1, Occur.SHOULD);
			bqBuilder.add(query2, Occur.SHOULD);
		    }
		    query = bqBuilder.build();
		    //

		    topDocs = searcher.search(query, 1);
		    System.out.println(topDocs.totalHits);

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
			System.out.println( queryDoc.get("BODY"));
			SimBody = new TextField("SimBody", queryDoc.get("BODY"),
				Field.Store.YES);
		    }
		    doc.add(SimPathSgmField);
		    doc.add(SimTitle);
		    doc.add(SimBody);

		    writer.addDocument(doc);
		}
	    } catch (IOException /* | ParseException */ e) {
		e.printStackTrace();
		System.exit(-1);
	    }

	}

    }

    public static void startThreads(String indexin, String indexout,
	    Integer title_threads) {
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
	    int threadDocs = numDocs / title_threads;
	    int lastThreadDocs = numDocs - (threadDocs * (title_threads - 1));
	    int[] threadDocRange = new int[title_threads + 1];

	    threadDocRange[0] = 0;
	    for (int i = 1; i < title_threads; i++) {
		threadDocRange[i] = threadDocRange[i - 1] + threadDocs;
	    }
	    threadDocRange[title_threads] = threadDocRange[title_threads - 1]
		    + lastThreadDocs;

	    final ExecutorService executor = Executors
		    .newFixedThreadPool(title_threads);
	    for (int j = 0; j < title_threads - 1; j++) {
		final Runnable worker = new WorkerThread(threadDocRange[j],
			threadDocRange[j + 1] - 1, reader, writer);
		executor.execute(worker);
	    }
	    final Runnable finalworker = new WorkerThread(
		    threadDocRange[title_threads - 1],
		    threadDocRange[title_threads], reader, writer);
	    executor.execute(finalworker);

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
