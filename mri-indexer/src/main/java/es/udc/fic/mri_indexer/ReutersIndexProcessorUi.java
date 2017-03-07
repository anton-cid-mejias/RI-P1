package es.udc.fic.mri_indexer;

import es.udc.fic.util.CheckIndexOrDocumentDirectories;

public class ReutersIndexProcessorUi {

    public static void main(String[] args) {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}
	
	String indexfile = null;
	String field = null;
	Integer n = null;
	Integer processing_type = null;
	
	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexfile = args[i + 1];
		i++;
	    }else if ("-best_idfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		processing_type = 0;
		i = i+2;
	    }else if ("-poor_idfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		processing_type = 1;
		i = i+2;
	    }else if ("-best_tfidfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		processing_type = 2;
		i = i+2;
	    }else if ("-poor_tfidfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		processing_type = 3;
		i = i+2;
	    }
	}

	if (indexfile == null || field == null || n == null || processing_type == null){
	    print_usage_and_exit();
	}
	CheckIndexOrDocumentDirectories.check_directory(indexfile, false);
	CheckIndexOrDocumentDirectories.check_directory(indexfile, true);
	
	switch (processing_type){
	case 0 : //do something;
	    	break;
	case 1 : //do something;
	    	break;
	case 2 : //do something;
	    	break;
	case 3 : //do something;
	    	break;
	}
	
    }

    private static void print_usage_and_exit() {
	String usage = "Index proccessing options [-indexin indexfile] [-best_idfterms field n] "
		+ "[-poor_idfterms field n] [-best_tfidfterms field n] [-poor_tfidfterms field n] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
