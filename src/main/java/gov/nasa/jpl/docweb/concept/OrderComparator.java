package gov.nasa.jpl.docweb.concept;

import java.util.Comparator;

public class OrderComparator implements Comparator<Orderable> {

	@Override
	public int compare(Orderable a, Orderable b) {
		Integer ai = a.getIndex();
		Integer bi = b.getIndex();
		if (ai != null && bi != null)
			return ai.compareTo(bi);
		if (ai != null)
			return 1;
		if (bi != null)
			return -1;
		return 0;
	}
}
