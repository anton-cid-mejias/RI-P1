package es.udc.fic.mri_indexer;

public class Indexer {

    private String openmode;
    private String index;
    private String coll;
    private String colls;
    private String indexes1;
    private String indexes2;

    public Indexer(String openmode, String index, String coll, String colls,
	    String indexes1, String indexes2) {
	this.openmode = openmode;
	this.index = index;
	this.coll = coll;
	this.colls = colls;
	this.indexes1 = indexes1;
	this.indexes2 = indexes2;
    }

    public void run(){
	
    }
}
