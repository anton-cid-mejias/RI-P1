package es.udc.fic.mri_indexer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriterConfig;

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
		print_usage_and_exit();
	    }
	    
	    String openmodeString = null;
	    IndexWriterConfig.OpenMode openmode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
	    String index = null;
	    String coll = null;
	    List<String> colls = new ArrayList<>();
	    List<String> indexes1 = new ArrayList<>();
	    String indexes2 = null;
	    
	    for(int i = 0;i < args.length;i++) {
		if ("-openmode".equals(args[i])) {
		    openmodeString = args[i+1];
		    if ((! openmodeString.equals("append")) || (! openmodeString.equals("create"))
			    || (! openmodeString.equals("append_or_create"))){
			System.err.println("Openmode must be: append, create or append_or_create.");
		        System.exit(1);
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
		}else if ("-indexes1".equals(args[i])) {
		    while ( !((args[i+1]).charAt(0) == '-')){
			indexes1.add(args[i+1]);
			i++;
		    }
		}else if ("-indexes2".equals(args[i])) {
		    indexes2 = args[i+1];
		    i++;
		}
	    }
	    
	    //Openmode will be create_or_append by default
	    if (openmodeString != null){
		if(openmodeString.equals("create")){
		    openmode = IndexWriterConfig.OpenMode.CREATE;
		} else if (openmodeString.equals("append")){
		    openmode = IndexWriterConfig.OpenMode.APPEND;
		}
	    }
	    
	    //Check if parametters are correct
	    if (    ((coll != null) && (!colls.isEmpty())) || ((index != null) && !indexes1.isEmpty()) 
		    || ((index != null) && (indexes2 != null)) || ((index != null) && (indexes2 != null)) 
		    ){
		print_usage_and_exit();
	    } else if (index != null){
		check_directory(index, true);
		if (coll != null){
		    check_directory(coll, false);
		    //call to indexing function
		} else if (! colls.isEmpty()){
		    check_directories(colls, false);
		    //call to indexing function
		} else {
		    print_usage_and_exit();
		}
		
	    } else if (!indexes1.isEmpty()){
		check_directories(indexes1, true);
		if (! colls.isEmpty()){
		    check_directories(colls, false);
		    //call to indexing function
		} else {
		    print_usage_and_exit();
		}
		
	    } else if (indexes2 != null){
		check_directory(indexes2, true);
		if (! colls.isEmpty()){
		    check_directories(colls, false);
		    //call to indexing function
		} else {
		    print_usage_and_exit();
		}
	    } else {
		print_usage_and_exit();
	    }	    
	    
    }
    
    private static void check_directory (String directory, boolean isIndexNotDocument){
	final Path docDir = Paths.get(directory);
	if (isIndexNotDocument) {
	    if (!(Files.isDirectory(docDir) && Files.isWritable(docDir))) {
		System.out.println("Index directory '" +docDir.toAbsolutePath()+ 
		      "' does not exist or is not readable, please check the path");
		System.exit(1);
	    }
	} else {
	    if (! (Files.isDirectory(docDir) && Files.isReadable(docDir))) {
		System.out.println("Document directory '" +docDir.toAbsolutePath()+ 
		      "' does not exist or is not readable, please check the path");
		System.exit(1);
	    }
	}
    }
    
    private static void check_directories (List<String> directories, boolean isIndexNotDocument){
	for (String directory : directories){
	    check_directory(directory, isIndexNotDocument);
	}
    }
    
    private static void print_usage_and_exit() {
	String usage =
		      "Usage: Indexing options [-openmode openmode] [-index pathname] [-coll pathname] "
		      + "[-colls pathname_1 ... pathname_n] [-indexes1 pathname_0 pathname_1 ... pathname_n] "
		      + "[-indexes2 pathname_0] \n";
	System.out.println(usage);
	System.exit(0);
    }
    

}
