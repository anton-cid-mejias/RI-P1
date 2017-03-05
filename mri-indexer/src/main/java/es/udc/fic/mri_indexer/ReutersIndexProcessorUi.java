package es.udc.fic.mri_indexer;

public class ReutersIndexProcessorUi {

    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }
    
    private static void print_usage_and_exit() {
	String usage =
		"Index proccessing options [-indexin indexfile] [-best_idfterms field n] "
		+ "[-poor_idfterms field n] [-best_tfidfterms field n] [-poor_tfidfterms field n] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
