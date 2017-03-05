package es.udc.fic.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CheckIndexOrDocumentDirectories {

    public static void check_directory(String directory,
	    boolean isIndexNotDocument) {
	final Path docDir = Paths.get(directory);
	if (isIndexNotDocument) {
	    if (!(Files.isDirectory(docDir) && Files.isWritable(docDir))) {
		System.out.println("Index directory '" + docDir.toAbsolutePath()
			+ "' does not exist or is not readable, please check the path");
		System.exit(1);
	    }
	} else {
	    if (!(Files.isDirectory(docDir) && Files.isReadable(docDir))) {
		System.out.println("Document directory '"
			+ docDir.toAbsolutePath()
			+ "' does not exist or is not readable, please check the path");
		System.exit(1);
	    }
	}
    }

    public static void check_directories(List<String> directories,
	    boolean isIndexNotDocument) {
	for (String directory : directories) {
	    check_directory(directory, isIndexNotDocument);
	}
    }
    
}
