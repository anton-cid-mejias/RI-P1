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
	
	for (int i = 0; i < args.length; i++) {
	    if ("-indexin".equals(args[i])) {
		indexin = args[i + 1];
		i++;
	    }else if ("-indexout".equals(args[i])){
		indexout = args[i + 1];
		i++;
	    }
	    
	}
	
	if (indexin == null || indexout == null){
	    print_usage_and_exit();
	}
	CheckIndexOrDocumentDirectories.check_directory(indexin, false);
	CheckIndexOrDocumentDirectories.check_directory(indexout, true);

    }

    private static void print_usage_and_exit() {
	String usage = "Creation of new Index from old one [-indexin indexfile] [-indexout indexfile] "
		+ "[-deldocsterm field term] [-mostsimilardoc_title h] [-mostsimilardoc_body n h] "
		+ "[-mostsimilardoc_title h y -mostsimilardoc_body n h d] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
