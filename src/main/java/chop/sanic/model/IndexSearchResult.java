package chop.sanic.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexSearchResult {

	private String searchQuery;
	private Map<String, Set<IndexedDocument>> results;
	
	public IndexSearchResult() {
		this.results = new HashMap<>();
	}

	public IndexSearchResult(String searchQuery, Map<String, Set<IndexedDocument>> results) {
		super();
		this.searchQuery = searchQuery;
		this.results = results;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public Map<String, Set<IndexedDocument>> getResults() {
		return results;
	}

	public void setResults(Map<String, Set<IndexedDocument>> results) {
		this.results = results;
	}
	
	public void addResult(String attributeName, IndexedDocument document ) {
		if ( ! this.results.containsKey(attributeName)) {
			this.results.put(attributeName, new HashSet<>());
		}
		
		this.results.get(attributeName).add(document);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		result = prime * result + ((searchQuery == null) ? 0 : searchQuery.hashCode());
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
		IndexSearchResult other = (IndexSearchResult) obj;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		if (searchQuery == null) {
			if (other.searchQuery != null)
				return false;
		} else if (!searchQuery.equals(other.searchQuery))
			return false;
		return true;
	}
	
	
	
}
