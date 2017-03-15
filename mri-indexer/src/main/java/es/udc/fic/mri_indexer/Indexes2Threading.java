package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexes2Threading {

    // en el thread que queda pon Hostname y Thread

    private static class WorkerThread implements Runnable {

	private final String documentDirectory;
	private final IndexWriter writer;

	public WorkerThread(String documentDirectory, IndexWriter writer) {
	    this.documentDirectory = documentDirectory;
	    this.writer = writer;
	}

	@Override
	public void run() {
	    //Thread.currentThread().getName())
	    //InetAddress.getLocalHost().getHostName()

	    // USA EL WRITER PARA CADA COSA
	}

    }

    public static void startThreads(String[] colls, String index) {
	int numThreads = colls.length;

	final ExecutorService executor = Executors
		.newFixedThreadPool(numThreads);
	Directory dir;
	try {
	    dir = FSDirectory.open(Paths.get(index));
	    IndexWriterConfig iwc = new IndexWriterConfig(
		    new StandardAnalyzer());
	    IndexWriter writer = new IndexWriter(dir, iwc);

	    for (int i = 0; i < numThreads; i++) {
		final Runnable worker = new WorkerThread(colls[i], writer);
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
