package chop.sanic.services;

import chop.sanic.model.IndexedDocument;
import chop.sanic.model.ScoredDocument;

public interface DocumentResultScorer {

	public ScoredDocument scoreResult(String query, IndexedDocument doc );
}
