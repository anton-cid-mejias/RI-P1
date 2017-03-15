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

import examples.ThreadPoolExample.WorkerThread;

public class Indexes1Threading {

    // en el thread que queda pon Hostname y Thread

    private static class WorkerThread implements Runnable {

	private final String documentDirectory;
	private final String indexDirectory;

	public WorkerThread(String documentDirectory, String indexDirectory) {
	    this.documentDirectory = documentDirectory;
	    this.indexDirectory = documentDirectory;
	}

	@Override
	public void run() {
	    //Thread.currentThread().getName())
	    //InetAddress.getLocalHost().getHostName()


	    //do stuff
	}
    }

    public static void startThreads(String[] colls, String[] indexes) {
	final String finalIndex = indexes[0];
	// Removing first index, that way colls and indexes are directly
	// corresponding
	indexes = Arrays.copyOfRange(indexes, 1, indexes.length);
	int numThreads = colls.length;

	final ExecutorService executor = Executors
		.newFixedThreadPool(numThreads);

	for (int i = 0; i < numThreads; i++) {
	    final Runnable worker = new WorkerThread(colls[i], indexes[i]);
	    executor.execute(worker);
	}

	executor.shutdown();

	try {
	    executor.awaitTermination(1, TimeUnit.HOURS);
	} catch (final InterruptedException e) {
	    e.printStackTrace();
	    System.exit(-2);
	}

	// merge indexes
	try{
	    IndexMerger(finalIndex, indexes);
	} catch (final IOException e) {
		e.printStackTrace();
		System.exit(-1);
	}

    }

    @SuppressWarnings("null")
    private static void IndexMerger(String finalIndex, String[] indexes) throws IOException {

	Directory dir = FSDirectory.open(Paths.get(finalIndex));
	Directory[] dirs = null;
	IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
	IndexWriter writer = new IndexWriter(dir, iwc);
	
	for (int i = 0; i < indexes.length; i++){
	    dirs[i] = FSDirectory.open(Paths.get(indexes[i]));    
	}
	writer.addIndexes(dirs);

	writer.close();
    }

}
