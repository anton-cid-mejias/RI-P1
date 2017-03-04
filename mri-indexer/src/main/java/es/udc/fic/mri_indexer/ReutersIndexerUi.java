package es.udc.fic.mri_indexer;

public class ReutersIndexerUi {
    
    public static void main(String[] args) {
	    String usage =
		      "Usage: Indexing options [-openmode openmode] [-index pathname] [-coll pathname] "
		      + "[-colls pathname_1 ... pathname_n] [-indexes1 pathname_0 pathname_1 ... pathname_n] "
		      + "[-indexes2 pathname_0] \n"
		      + "Index proccessing options [-indexin indexfile] [-best_idfterms field n] "
		      + "[-poor_idfterms field n] [-best_tfidfterms field n] [-poor_tfidfterms field n] \n"
		      + "Creation of new Index from old one [-indexin indexfile] [-indexout indexfile] "
		      + "[-deldocsterm field term] [-mostsimilardoc_title h] [-mostsimilardoc_body n h] "
		      + "[-mostsimilardoc_title h y -mostsimilardoc_body n h d] \n";

	    if (args.length == 0){
		      System.out.println(usage);
		      System.exit(0);
	    }
	    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
		      System.out.println(usage);
		      System.exit(0);
	    }
		    
		    
		    
		    
    }

}
