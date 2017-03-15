package es.udc.fic.mri_indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import es.udc.fic.util.CheckIndexOrDocumentDirectories;

public class ReutersIndexerUi {

    public static void main(String[] args) throws IOException {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

	String openmodeString = null;
	OpenMode openmode = OpenMode.CREATE_OR_APPEND;
	String index = null;
	String coll = null;
	List<String> colls = new ArrayList<>();
	List<String> indexes1 = new ArrayList<>();
	String indexes2 = null;

	for (int i = 0; i < args.length; i++) {
	    if ("-openmode".equals(args[i])) {
		openmodeString = args[i + 1];
		if ((!openmodeString.equals("append"))
			&& (!openmodeString.equals("create"))
			&& (!openmodeString.equals("create_or_append"))) {
		    System.err.println(
			    "Openmode must be: append, create or append_or_create.");
		    System.exit(1);
		}
		i++;
	    } else if ("-index".equals(args[i])) {
		index = args[i + 1];
		i++;
	    } else if ("-coll".equals(args[i])) {
		coll = args[i + 1];
		i++;
	    } else if ("-colls".equals(args[i])) {
		while ((i+1 < args.length) && !((args[i + 1]).charAt(0) == '-')) {
		    colls.add(args[i + 1]);
		    i++;
		}
	    } else if ("-indexes1".equals(args[i])) {
		while ((i+1 < args.length) && !((args[i + 1]).charAt(0) == '-')) {
		    indexes1.add(args[i + 1]);
		    i++;
		}
		if (indexes1.size() < 2) {
		    print_usage_and_exit();
		}
	    } else if ("-indexes2".equals(args[i])) {
		indexes2 = args[i + 1];
		i++;
	    }
	}

	// Openmode will be create_or_append by default
	if (openmodeString != null) {
	    if (openmodeString.equals("create")) {
		openmode = OpenMode.CREATE;
	    } else if (openmodeString.equals("append")) {
		openmode = OpenMode.APPEND;
	    }
	}

	// Check if parameters are correct
	if (((coll != null) && (!colls.isEmpty()))
		|| ((index != null) && !indexes1.isEmpty())
		|| ((index != null) && (indexes2 != null))
		|| (!indexes1.isEmpty() && (indexes2 != null))) {
	    print_usage_and_exit();
	} else if (index != null) {
	    CheckIndexOrDocumentDirectories.check_directory(index, true);
	    if (coll != null) {
		CheckIndexOrDocumentDirectories.check_directory(coll, false);
		Indexer.run(openmode, index, coll);
	    } else if (!colls.isEmpty()) {
		CheckIndexOrDocumentDirectories.check_directories(colls, false);
		Indexer.run(openmode, index, colls);
	    } else {
		print_usage_and_exit();
	    }

	} else if (!indexes1.isEmpty()) {
	    CheckIndexOrDocumentDirectories.check_directories(indexes1, true);
	    if (!colls.isEmpty()) {
		if (colls.size() != (indexes1.size() -1)){
		    System.out.println("There must be as many indexes as document folders");
		    System.exit(1);
		}
		CheckIndexOrDocumentDirectories.check_directories(colls, false);
		// call to indexing function
	    } else {
		print_usage_and_exit();
	    }

	} else if (indexes2 != null) {
	    CheckIndexOrDocumentDirectories.check_directory(indexes2, true);
	    if (!colls.isEmpty()) {
		CheckIndexOrDocumentDirectories.check_directories(colls, false);
		// call to indexing function
	    } else {
		print_usage_and_exit();
	    }
	} else {
	    print_usage_and_exit();
	}

    }

    private static void print_usage_and_exit() {
	String usage = "Usage: Indexing options [-openmode openmode] [-index pathname] [-coll pathname] "
		+ "[-colls pathname_1 ... pathname_n] [-indexes1 pathname_0 pathname_1 ... pathname_n] "
		+ "[-indexes2 pathname_0] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
