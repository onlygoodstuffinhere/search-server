package chop.sanic.utils;

import java.util.Comparator;

import chop.sanic.model.ScoredDocument;

public class ScoredDocumentComparator implements Comparator<ScoredDocument> {

	public ScoredDocumentComparator() {
	}

	@Override
	public int compare(ScoredDocument sd1, ScoredDocument sd2) {
		if ( sd1.getScore() > sd2.getScore() ) {
			return - 1;
		}
		else if ( sd2.getScore() > sd1.getScore() ) {
			return 1;
		}
		else {
			return 0;
		}		
	}

}
