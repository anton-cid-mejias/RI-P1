package es.udc.fic.mri_indexer;

import java.nio.file.Path;

public class ThreadSlave implements Runnable {

    private final Path folder;

    public ThreadSlave(final Path folder) {
			this.folder = folder;
		}

    @Override
    public void run() {
	System.out.println(String.format(
		"I am the thread '%s' and I am responsible for folder '%s'",
		Thread.currentThread().getName(), folder));
	
    }

}
