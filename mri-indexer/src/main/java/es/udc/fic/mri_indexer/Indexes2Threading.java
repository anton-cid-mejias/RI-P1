package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexes2Threading {

    private static class WorkerThread implements Runnable {

	private final String documentDirectory;
	private final IndexWriter writer;

	public WorkerThread(String documentDirectory, IndexWriter writer) {
	    this.documentDirectory = documentDirectory;
	    this.writer = writer;
	}

	@Override
	public void run() {

	    // USA EL WRITER PARA CADA COSA
	}

    }

    public static void startThreads(OpenMode openmode, List<String> colls, String index) {

	final ExecutorService executor = Executors
		.newFixedThreadPool(colls.size());
	Directory dir;
	try {
	    dir = FSDirectory.open(Paths.get(index));
	    IndexWriterConfig iwc = new IndexWriterConfig(
		    new StandardAnalyzer());
	    iwc.setOpenMode(openmode);
	    IndexWriter writer = new IndexWriter(dir, iwc);

	    for (String coll : colls) {
		final Runnable worker = new WorkerThread(coll, writer);
		executor.execute(worker);
	    }

	    executor.shutdown();
	    try {
		executor.awaitTermination(1, TimeUnit.HOURS);
	    } catch (final InterruptedException e) {
		e.printStackTrace();
		System.exit(-2);
	    }

	    writer.close();
	} catch (IOException e1) {
	    e1.printStackTrace();
	    System.exit(-1);
	}
    }
}
