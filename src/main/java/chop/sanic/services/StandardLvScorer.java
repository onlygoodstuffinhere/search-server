package chop.sanic.services;

import java.util.Map.Entry;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import chop.sanic.model.IndexedDocument;
import chop.sanic.model.ScoredDocument;
import chop.sanic.utils.IndexUtils;

@Component
public class StandardLvScorer implements DocumentResultScorer {

	LevenshteinDistance lDist;
	private final Logger logger =  LoggerFactory.getLogger(getClass());
	
	
	public StandardLvScorer() {
		this.lDist = new LevenshteinDistance();
	}

	@Override
	public ScoredDocument scoreResult(String query, IndexedDocument doc) {
		//we just take normalized levenshtein dist for best scoring attribute
		double score = 0;
		String comparableQuery = IndexUtils.toComparable(query);
		for ( Entry<String, String> attributePair : doc.getIndexedAttributes().entrySet() ) {
			String normalizedAttribute = IndexUtils.toComparable(attributePair.getValue());
			double attScore = getNormalizedGLD(comparableQuery, normalizedAttribute);
			/*if ( attScore > score ) {
				score = attScore;
			}*/
			score += attScore;
		}
		ScoredDocument result = new ScoredDocument(score, doc.getDocument(), doc.getId());
		return result;
	}
	
	private double getNormalizedGLD(String a, String b) {
		double gld = (double)lDist.apply(a, b);
		double result = ( 2 * gld ) / ( a.length() + b.length() + gld );
		return 1-result;
	}

}
