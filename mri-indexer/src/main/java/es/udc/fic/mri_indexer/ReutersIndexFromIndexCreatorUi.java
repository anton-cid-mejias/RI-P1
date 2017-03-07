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
	String title = null;
	String body = null;
	Integer n = null;
	
	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexin = args[i+1];
		i++;
	    }else if ("-indexout".equals(args[i])){
		indexout = args[i+1];
		i++;
	    }else if ("-deldocsterm".equals(args[i])){
		deldocsField = args[i+1];
		deldocsTerm = args[i+2];
		i = i+2;
	    }else if("-deldocsquery".equals(args[i])){
		//NO sé aún como vamos a pasar la query
	
	    }else if ("-mostsimilardoc_title".equals(args[i])){ //debe ser concurrente
		title = args[i+1];
		i++;
	    }else if ("-mostsimilardoc_body".equals(args[i])){ //debe ser concurrente
		n = Integer.parseInt(args[i+1]);
		body = args[i+2];
		i = i+2;
	    }
	    
	}
	
	
	//OJO, si se hace deldocsterm o query no hace falta comprobr indexout, 
	//se aplica sobre indexin
	if (indexin == null || indexout == null){
	    print_usage_and_exit();
	}
	CheckIndexOrDocumentDirectories.check_directory(indexin, false);
	CheckIndexOrDocumentDirectories.check_directory(indexout, true);
	
	//sin acabar, no sé si puede hacer varias peticiones a la vez o no

    }

    private static void print_usage_and_exit() {
	String usage = "Creation of new Index from old one [-indexin indexfile] [-indexout indexfile] "
		+ "[-deldocsterm field term] [-deldocsquery “query”] [-mostsimilardoc_title h] "
		+ "[-mostsimilardoc_body n h] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
