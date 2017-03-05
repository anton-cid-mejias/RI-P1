package es.udc.fic.mri_indexer;

import java.util.ArrayList;
import java.util.List;

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

	    if ((args.length == 0) || (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0])))){
		      System.out.println(usage);
		      System.exit(0);
	    }
	    
	    String openmode = "openmode"; //no se si esto sera string
	    String index = "index";
	    String coll = "col";
	    List<String> colls = new ArrayList<>();
	    
	    for(int i = 0;i < args.length;i++) {
		if ("-openmode".equals(args[i])) {
		    openmode = args[i+1];
		    if ((! openmode.equals("append")) || (! openmode.equals("create"))
			    || (! openmode.equals("append_or_create"))){
			System.out.println(usage);
			System.exit(0);
		    }
		    i++;
		    
		}else if ("-index".equals(args[i])) {
		    index = args[i+1];
		    i++;
		}else if ("-coll".equals(args[i])) {
		    coll = args[i+1];
		    i++;
		}else if ("-colls".equals(args[i])) {
		    while ( !((args[i+1]).charAt(0) == '-')){
			colls.add(args[i+1]);
			i++;
		    }
		}
	    }
		    
		    
    }

}
