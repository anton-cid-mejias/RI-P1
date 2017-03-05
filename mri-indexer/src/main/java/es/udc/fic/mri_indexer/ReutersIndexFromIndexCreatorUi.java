package es.udc.fic.mri_indexer;

public class ReutersIndexFromIndexCreatorUi {

    public static void main(String[] args) {
	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

    }

    private static void print_usage_and_exit() {
	String usage = "Creation of new Index from old one [-indexin indexfile] [-indexout indexfile] "
		+ "[-deldocsterm field term] [-mostsimilardoc_title h] [-mostsimilardoc_body n h] "
		+ "[-mostsimilardoc_title h y -mostsimilardoc_body n h d] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
