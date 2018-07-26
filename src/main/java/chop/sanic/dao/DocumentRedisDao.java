package chop.sanic.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;


import chop.sanic.model.IndexSearchResult;
import chop.sanic.model.IndexedDocument;
import chop.sanic.utils.IndexUtils;

@Component
public class DocumentRedisDao {

	private final Logger logger =  LoggerFactory.getLogger(getClass());
	RedisTemplate redisTemplate;
	SetOperations<String, String> setOperations;
	HashOperations<String, String, IndexedDocument> hashOperations;
	
	private final String INDEXED_ATTRIBUTES = "indexedattributes";
	private final String FULL_DOCUMENT = "fulldoc";
	private final String SEARCH = "search";
	
	@Autowired
	public DocumentRedisDao ( RedisTemplate redisTemplate ) {
		this.redisTemplate = redisTemplate;
		//redisTemplate.
	}
	
	@PostConstruct
	public void init() {
		this.setOperations = redisTemplate.opsForSet();
		this.hashOperations = redisTemplate.opsForHash();
	}
	
	public void indexDocument ( IndexedDocument document) {
		//1. index id-documentredisTempl
		this.hashOperations.put(FULL_DOCUMENT+"."+ document.getDocumentType()
			, document.getId(), document);
		//2. index attributes-id
		Map<String,String> indexedFields = document.getIndexedAttributes();
		for ( Entry<String, String> field : indexedFields.entrySet() ) {
			String attributeName =  field.getKey();
			String attributeValue = field.getValue();
			Set<String> keys = IndexUtils.toIndexable(attributeValue);
			for ( String key : keys ) {
				this.setOperations.add(SEARCH + "." + document.getDocumentType() 
				+ "." + attributeName + "." + key, 
				document.getId());
				if( logger.isDebugEnabled() ) {
					logger.debug("INDEXING : "+document.getId()+ " @ index : " 
							+SEARCH+"."+document.getDocumentType()+"."+attributeName+"."+key);
				}
				
			}
		}
	}
	
	public void registerIndexedAttributes ( String indexName, 
			List<String> attributeNames) {
		this.setOperations.add( INDEXED_ATTRIBUTES + "." + indexName, 
				attributeNames.toArray(new String[attributeNames.size()]));
	}
	
	public IndexSearchResult searchDocument ( String documentType, String query ) {
		IndexSearchResult finalResult = new IndexSearchResult(query, new HashMap<>());
		Set<String> indexedAttributes = this.setOperations.members(INDEXED_ATTRIBUTES 
				+ "." + documentType);
		Set<String> indexes = IndexUtils.toSearchable(query);
		for ( String attribute : indexedAttributes ) {
			String docPrefix = SEARCH + "." + documentType + "." + attribute;
			Set<String> resultIds = new HashSet<>();
			for ( String index : indexes ) {
				logger.debug("index : "+docPrefix + "." + index);
				resultIds.addAll(this.setOperations.members(docPrefix + "." + index));
			}
			for( String id : resultIds ) {
				finalResult.addResult(attribute, this.hashOperations.get(FULL_DOCUMENT+"."+documentType, id));
			}
			
		}
		return finalResult;
	}

}
