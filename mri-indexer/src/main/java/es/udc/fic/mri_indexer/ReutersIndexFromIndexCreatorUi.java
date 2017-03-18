package es.udc.fic.mri_indexer;

import es.udc.fic.util.CheckIndexOrDocumentDirectories;

public class ReutersIndexFromIndexCreatorUi {

    public static void main(String[] args) {
	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

	String indexin = null;
	String indexout = null;
	String deldocsField = null;
	String deldocsTerm = null;
	String query = null;
	Integer title_threads = null;
	Integer n_best_terms = null;
	Integer body_threads = null;
	// We don't allow the use of more than one option, except for
	// indexin/out
	Integer option_number = 0;

	// Parsing
	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexin = args[i + 1];
		i++;
	    } else if ("-indexout".equals(args[i])) {
		indexout = args[i + 1];
		i++;
	    } else if ("-deldocsterm".equals(args[i])) {
		deldocsField = args[i + 1];
		deldocsTerm = args[i + 2];
		i = i + 2;
		option_number++;
	    } else if ("-deldocsquery".equals(args[i])) {
		query = args[i + 1];
		i++;
		option_number++;
	    } else if ("-mostsimilardoc_title".equals(args[i])) {
		title_threads = Integer.parseInt(args[i + 1]);
		i++;
		option_number++;
	    } else if ("-mostsimilardoc_body".equals(args[i])) {
		n_best_terms = Integer.parseInt(args[i + 1]);
		body_threads = Integer.parseInt(args[i + 2]);
		i = i + 2;
		option_number++;
	    }
	}

	// Option choosing
	if ((option_number != 1) || (indexin == null)) {
	    print_usage_and_exit();
	} else if (indexout == null) {
	    CheckIndexOrDocumentDirectories.check_directory(indexin, false);
	    CheckIndexOrDocumentDirectories.check_directory(indexin, true);
	    if (deldocsTerm != null) {
		IndexFromIndexCreator.deldocsTerm(indexin, indexout,
			deldocsField, deldocsTerm);
	    } else if (query != null) {
		IndexFromIndexCreator.deldocsQuery(indexin, indexout, query);
	    } else {
		print_usage_and_exit();
	    }

	} else if (indexout != null) {
	    CheckIndexOrDocumentDirectories.check_directory(indexin, false);
	    CheckIndexOrDocumentDirectories.check_directory(indexout, true);
	    if (deldocsTerm != null) {
		IndexFromIndexCreator.deldocsTerm(indexin, indexout,
			deldocsField, deldocsTerm);
	    } else if (query != null) {
		IndexFromIndexCreator.deldocsQuery(indexin, indexout, query);
	    } else if (title_threads != null) {
		if (title_threads < 1){
		    System.out.println("You must input at least 1 thread");
		    System.exit(1);
		}
		MostSimilarDoc_TitleThreading.startThreads(indexin, indexout, title_threads);
	    } else if (body_threads != null) {
		if (body_threads < 1){
		    System.out.println("You must input at least 1 thread");
		    System.exit(1);
		}
		// call to function
	    }
	}

    }

    private static void print_usage_and_exit() {
	String usage = "Creation of new Index from old one [-indexin indexfile] [-indexout indexfile] "
		+ "[-deldocsterm field term] [-deldocsquery “query”] [-mostsimilardoc_title h] "
		+ "[-mostsimilardoc_body n h] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
