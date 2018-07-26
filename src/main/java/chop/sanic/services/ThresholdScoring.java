package chop.sanic.services;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import chop.sanic.model.IndexedDocument;
import chop.sanic.model.ScoredDocument;
import chop.sanic.utils.IndexUtils;

@Component
public class ThresholdScoring implements DocumentResultScorer {

	private final int TRESHOLD = 2;
	LevenshteinDistance lDist;
	
	public ThresholdScoring() {
		this.lDist = new LevenshteinDistance();
	}

	@Override
	public ScoredDocument scoreResult(String query, IndexedDocument doc) {
		ScoredDocument result = new ScoredDocument();
		//1. lowercase and tokenize query and attribute values
		//2. lv dist for each token
		//3. if dist <= treshold, score +1
		double score = 0;
		String lcQuery = IndexUtils.toLowerCase(query);
		Set<String> qTokens = IndexUtils.tokenize(lcQuery);
		qTokens.removeIf((String s) -> {return s.length()<3;});
		for ( Entry<String, String> attribute : doc.getIndexedAttributes().entrySet() ) {
			String lcAttribute = IndexUtils.toLowerCase(attribute.getValue());
			Set<String> attTokens = IndexUtils.tokenize(lcAttribute);
			attTokens.removeIf((String s) -> {return s.length()<3;});
			for ( String attToken : attTokens ) {
				for ( String qToken : qTokens ) {
					int dist = this.lDist.apply(attToken, qToken);
					if ( dist <= TRESHOLD ) {
						score ++;
					}
				}
			}
		}
		result.setDocument(doc.getDocument());
		result.setId(doc.getId());
		result.setScore(score);
		return result;
	}

	

	

}
