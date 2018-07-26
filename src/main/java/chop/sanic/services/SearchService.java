package chop.sanic.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import chop.sanic.dao.DocumentRedisDao;
import chop.sanic.model.IndexSearchResult;
import chop.sanic.model.IndexedDocument;
import chop.sanic.model.ScoredDocument;
import chop.sanic.utils.ScoredDocumentComparator;

@Service
public class SearchService {

	private DocumentRedisDao documentDao;
	private ThresholdScoring tresholdScoring;
	private StandardLvScorer standardScoring;
	private final Logger logger =  LoggerFactory.getLogger(getClass());

	
	@Autowired
	public SearchService(DocumentRedisDao documentRedisDao,
						ThresholdScoring tresholdScoring,
						StandardLvScorer standardScoring) {
		this.documentDao = documentRedisDao;
		this.tresholdScoring = tresholdScoring;
		this.standardScoring = standardScoring;
	}
	
	public List<ScoredDocument> search ( String documentType , String query ){
		long redisTimerStart = System.currentTimeMillis();
		IndexSearchResult isr = this.documentDao.searchDocument(documentType, query);
		long redisTimerEnd = System.currentTimeMillis();
		List<ScoredDocument> scoredResults = new ArrayList<>();
		Set<IndexedDocument> results = new HashSet<>();
		for ( Entry<String, Set<IndexedDocument>> doc : isr.getResults().entrySet()) {
			results.addAll(doc.getValue());
		}
		logger.debug("QUERY : "+query +" - REDIS LOOKUP : "+(redisTimerEnd - redisTimerStart)
				+" millis - NB RESULTS : "+ results.size());
		long scoreTimerStart = System.currentTimeMillis();
		for( IndexedDocument doc : results ) {
			logger.debug(doc.getDocument());
			scoredResults.add(this.standardScoring.scoreResult(query, doc));
		}
		long scoreTimerEnd = System.currentTimeMillis();
		logger.debug("QUERY : "+query+" - SCORE TIMER : "+(scoreTimerEnd - scoreTimerStart) + " millis");
		scoredResults.sort(new ScoredDocumentComparator());
		if ( scoredResults.size() > 10 ) {
			scoredResults = scoredResults.subList(0, 10);
		}		
		return scoredResults;
		
	}

}
