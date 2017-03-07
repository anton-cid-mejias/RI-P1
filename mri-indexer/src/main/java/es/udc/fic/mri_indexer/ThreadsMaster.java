package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import examples.ThreadPoolExample.WorkerThread;

public class ThreadsMaster {

    public static void main(final String[] args) {

	if (args.length != 1) {
	    System.out.println("Usage: java ThreadPool folder");
	    return;
	}

	/*
	 * Create a ExecutorService (ThreadPool is a subclass of
	 * ExecutorService) with so many thread as cores in the machine. This can
	 * be tuned according to the resources needed by the threads.
	 */
	final int numCores = Runtime.getRuntime().availableProcessors();
	final ExecutorService executor = Executors.newFixedThreadPool(numCores);

	/*
	 * We use Java 7 NIO.2 methods for input/output management. More info
	 * in: http://docs.oracle.com/javase/tutorial/essential/io/fileio.html
	 *
	 * We also use Java 7 try-with-resources syntax. More info in:
	 * https://docs.oracle.com/javase/tutorial/essential/exceptions/
	 * tryResourceClose.html
	 */
	try (DirectoryStream<Path> directoryStream = Files
		.newDirectoryStream(Paths.get(args[0]))) {

	    /* We process each subfolder in a new thread. */
	    for (final Path path : directoryStream) {
		if (Files.isDirectory(path)) {
		    final Runnable worker = new WorkerThread(path);
		    /*
		     * Send the thread to the ThreadPool. It will be processed
		     * eventually.
		     */
		    executor.execute(worker);
		}
	    }

	} catch (final IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}

	/*
	 * Close the ThreadPool; no more jobs will be accepted, but all the
	 * previously submitted jobs will be processed.
	 */
	executor.shutdown();

	/* Wait up to 1 hour to finish all the previously submitted jobs */
	try {
	    executor.awaitTermination(1, TimeUnit.HOURS);
	} catch (final InterruptedException e) {
	    e.printStackTrace();
	    System.exit(-2);
	}

	System.out.println("Finished all threads");

    }

}
