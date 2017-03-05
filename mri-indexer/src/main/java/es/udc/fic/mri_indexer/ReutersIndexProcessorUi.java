package es.udc.fic.mri_indexer;

import es.udc.fic.util.CheckIndexOrDocumentDirectories;

public class ReutersIndexProcessorUi {

    public static void main(String[] args) {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}
	
	String indexin = null;
	String field = null;
	Integer n = null; 
	
	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexin = args[i + 1];
		i++;
	    }else if ("-best_idfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		i = i+2;
	    }else if ("-poor_idfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		i = i+2;
	    }else if ("-best_tfidfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		i = i+2;
	    }else if ("-poor_tfidfterms".equals(args[i])) {
		field = args[i + 1];
		n = Integer.getInteger(args[i + 2]);
		i = i+2;
	    }
	}

	if (indexin == null || field == null || n == null){
	    print_usage_and_exit();
	}
	CheckIndexOrDocumentDirectories.check_directory(indexin, true);
	
	
    }

    private static void print_usage_and_exit() {
	String usage = "Index proccessing options [-indexin indexfile] [-best_idfterms field n] "
		+ "[-poor_idfterms field n] [-best_tfidfterms field n] [-poor_tfidfterms field n] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
