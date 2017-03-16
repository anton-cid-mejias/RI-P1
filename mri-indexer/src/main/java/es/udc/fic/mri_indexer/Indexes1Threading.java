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

public class Indexes1Threading {

    private static class WorkerThread implements Runnable {

	private final OpenMode openmode;
	private final String documentDirectory;
	private final String indexDirectory;

	public WorkerThread(OpenMode openmode, String indexDirectory,
		String documentDirectory) {
	    this.openmode = openmode;
	    this.documentDirectory = documentDirectory;
	    this.indexDirectory = indexDirectory;
	}

	@Override
	public void run() {
	    try {
		Indexer.run(openmode, indexDirectory, documentDirectory);
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(1);
	    }
	}
    }

    public static void startThreads(OpenMode openmode, List<String> indexes,
	    List<String> colls) {
	final String finalIndex = indexes.get(0);
	// Removing first index, that way colls and indexes are directly
	// corresponding
	indexes.remove(0);
	int numThreads = colls.size();

	final ExecutorService executor = Executors
		.newFixedThreadPool(numThreads);

	for (int i = 0; i < numThreads; i++) {
	    final Runnable worker = new WorkerThread(openmode, indexes.get(i),
		    colls.get(i));
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
	try {
	    IndexMerger(openmode, finalIndex, indexes);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	
    }

    private static void IndexMerger(OpenMode openmode, String finalIndex, List<String> indexes) throws IOException {

	Directory dir = null;
	Directory[] dirs = null;
	IndexWriter writer = null;

	try {
	    dir = FSDirectory.open(Paths.get(finalIndex));
	    dirs = new Directory[indexes.size()];
	    IndexWriterConfig iwc = new IndexWriterConfig(
		    new StandardAnalyzer());
	    iwc.setOpenMode(openmode);
	    writer = new IndexWriter(dir, iwc);
	    
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}

	for (int i = 0; i < indexes.size(); i++) {
	    try {
		dirs[i] = FSDirectory.open(Paths.get(indexes.get(i)));
	    } catch (IOException e) {
		writer.close();
		e.printStackTrace();
		System.exit(-1);
	    }
	}
	writer.addIndexes(dirs);

	writer.close();
    }

}
