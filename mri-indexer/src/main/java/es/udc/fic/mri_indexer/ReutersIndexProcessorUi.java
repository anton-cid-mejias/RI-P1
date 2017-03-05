package es.udc.fic.mri_indexer;

public class ReutersIndexProcessorUi {

    public static void main(String[] args) {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}
	
	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		
	    }
	    
	}

    }

    private static void print_usage_and_exit() {
	String usage = "Index proccessing options [-indexin indexfile] [-best_idfterms field n] "
		+ "[-poor_idfterms field n] [-best_tfidfterms field n] [-poor_tfidfterms field n] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
