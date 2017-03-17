package es.udc.fic.mri_indexer;

public class TermTfIdf implements Comparable<TermTfIdf>{
    
    private final Double tfIdf;
    private final Integer tf;
    private final Double idf;
    private final String term;
    private final Integer docId;
    
    TermTfIdf(String term,int docId, double idf, int tf, double tfIdf){
	this.idf = idf;
	this.term = term;
	this.tf = tf;
	this.tfIdf = tfIdf;
	this.docId = docId;
    }

    public Double getTfIdf() {
        return tfIdf;
    }

    public Integer getTf() {
        return tf;
    }

    public Double getIdf() {
        return idf;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public int compareTo(TermTfIdf o) {
	int lastCmp = idf.compareTo(o.getTfIdf());
	return (lastCmp != 0 ? lastCmp : term.compareTo(o.getTerm()));
    }

    
    @Override
    public String toString() {
	return "tfIdf=" + tfIdf + " tf=" + tf + " idf=" + idf
		+ " term=" + term + " docId=" + docId;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((tfIdf == null) ? 0 : tfIdf.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TermTfIdf other = (TermTfIdf) obj;
	if (tfIdf == null) {
	    if (other.tfIdf != null)
		return false;
	} else if (!tfIdf.equals(other.tfIdf))
	    return false;
	return true;
    }

}
