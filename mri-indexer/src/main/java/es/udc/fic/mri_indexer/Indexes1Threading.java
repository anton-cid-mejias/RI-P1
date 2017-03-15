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

import examples.ThreadPoolExample.WorkerThread;

public class Indexes1Threading {

    // en el thread que queda pon Hostname y Thread

    public static class WorkerThread implements Runnable {

	private final String documentDirectory;
	private final String indexDirectory;

	public WorkerThread(String documentDirectory, String indexDirectory) {
	    this.documentDirectory = documentDirectory;
	    this.indexDirectory = documentDirectory;
	}

	@Override
	public void run() {
	    System.out.println(String.format(
		    "I am the thread '%s' and I am responsible for folder none",
		    Thread.currentThread().getName()));

	    //

	    try (DirectoryStream<Path> directoryStream = Files
		    .newDirectoryStream(Paths.get(documentDirectory))) {

		for (final Path path : directoryStream) {
		    if (Files.isDirectory(path)) {
			// do stuff
		    }
		}

	    } catch (final IOException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    //
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
	
	//usar finalIndex para mezclar todos los índices de index en uno

    }

}
