package es.udc.fic.mri_indexer;

public class TermIdf implements Comparable<TermIdf>{
    
    private final Double idf;
    private final String term;
    
    TermIdf(String term, double idf){
	this.idf = idf;
	this.term = term;
    }

    public double getIdf() {
        return idf;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public int compareTo(TermIdf o) {
	int lastCmp = idf.compareTo(o.getIdf());
	return (lastCmp != 0 ? lastCmp : term.compareTo(o.getTerm()));
    }

    @Override
    public String toString() {
	return "idf=" + idf + "	term=" + term;
    }
}
