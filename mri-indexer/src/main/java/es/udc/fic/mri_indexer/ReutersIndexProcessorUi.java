package es.udc.fic.mri_indexer;

import java.io.IOException;

import es.udc.fic.util.CheckIndexOrDocumentDirectories;

public class ReutersIndexProcessorUi {

    public static void main(String[] args) {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

	String indexfile = null;
	String[] field = new String[4];
	int[] n = new int[4];

	boolean best_idf = false;// 0 on arrays
	boolean poor_idf = false;// 1 on arrays
	boolean best_tf = false;// 2 on arrays
	boolean poor_tf = false;// 3 on arrays

	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexfile = args[i + 1];
		i++;
	    } else if ("-best_idfterms".equals(args[i])) {
		field[0] = args[i + 1];
		n[0] = Integer.parseInt(args[i + 2]);
		best_idf = true;
		i = i + 2;
	    } else if ("-poor_idfterms".equals(args[i])) {
		field[1] = args[i + 1];
		n[1] = Integer.parseInt(args[i + 2]);
		poor_idf = true;
		i = i + 2;
	    } else if ("-best_tfidfterms".equals(args[i])) {
		field[2] = args[i + 1];
		n[2] = Integer.parseInt(args[i + 2]);
		best_tf = true;
		i = i + 2;
	    } else if ("-poor_tfidfterms".equals(args[i])) {
		field[3] = args[i + 1];
		n[3] = Integer.parseInt(args[i + 2]);
		poor_tf = true;
		i = i + 2;
	    }
	}

	if (indexfile == null || (best_idf == false && best_tf == false
		&& poor_idf == false && poor_tf == false)) {
	    print_usage_and_exit();
	}

	CheckIndexOrDocumentDirectories.check_directory(indexfile, false);
	CheckIndexOrDocumentDirectories.check_directory(indexfile, true);

	if (best_idf){
	    try {
		Processor.IdfTerms(indexfile, field[0], n[0], true);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	if (poor_idf){
	    try {
		Processor.IdfTerms(indexfile, field[1], n[1], false);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	if (best_tf){
	    
	}
	if (poor_tf){
	    
	}

    }

    private static void print_usage_and_exit() {
	String usage = "Index proccessing options [-indexin indexfile] [-best_idfterms field n] "
		+ "[-poor_idfterms field n] [-best_tfidfterms field n] [-poor_tfidfterms field n] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
