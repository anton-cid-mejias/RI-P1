package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class MostSimilarDoc_TitleThreading {

    private static class WorkerThread implements Runnable {

	// esta copiada y falta modificar
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
	    try {
		for (int i = initialDoc; i < finalDoc; i++) {
		    doc = reader.document(i);
		    
		    
		    /*
		    Field SimPathSgmField = new StringField("SimPathSgm", file.toString(),
			    Field.Store.YES);
		    doc.add(simPathSgmField);
		    */
		    
		}
	    } catch (IOException e) {
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

	    /*
	     * No sé si voy a tener que leer de indexReader e ir uno a uno
	     * title:"titulo" y otra con body"titulo" (ojo, las comillas ponlas)
	     */

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
	    for (int j = 0; j < title_threads; j++) {
		final Runnable worker = new WorkerThread(threadDocRange[j],
			threadDocRange[j + 1], reader, writer);
		executor.execute(worker);
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
