package chop.sanic.services;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import chop.sanic.config.StateImpacting;
import chop.sanic.dao.DocumentRedisDao;
import chop.sanic.dao.SearchIndexDao;
import chop.sanic.model.IndexedDocument;
import chop.sanic.model.SearchIndex;
import chop.sanic.services.exceptions.DocumentNotFound;
import chop.sanic.services.exceptions.IndexAlreadyExists;

@Service
public class SearchIndexService {

	SearchIndexDao searchIndexDao;
	DocumentRedisDao documentDao;
	
	private final JsonSchemaFactory JSON_SCHEMA_FACTORY = JsonSchemaFactory.byDefault();
	private final JsonSchema META_SCHEMA;
	
	@Autowired
	public SearchIndexService ( SearchIndexDao searchIndexDao, 
			DocumentRedisDao documentRedisDao, ResourceLoader resourceLoader )
			throws IOException, ProcessingException {
		this.searchIndexDao = searchIndexDao;
		this.documentDao = documentRedisDao;
		Resource metaschemaJson = resourceLoader.getResource("classpath:04-validation-metaschema.json");
		ObjectMapper oMapper = new ObjectMapper();
		META_SCHEMA = JSON_SCHEMA_FACTORY.getJsonSchema(
				oMapper.readValue(metaschemaJson.getInputStream(), JsonNode.class));
	}
	
	@StateImpacting
	public SearchIndex create ( String name, JsonNode schema ) {
		if ( this.searchIndexDao.existsById(name)) {
			throw new IndexAlreadyExists(name);
		}
		try {
			if (!META_SCHEMA.validInstance(schema)) {
				throw new RuntimeException("Invalid schema");
			}
		} catch (ProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid schema",e);
		}	
		ObjectMapper mapper = new ObjectMapper();
		StringWriter sw = new StringWriter();
		try {
			List<String> indexedFields = getIndexedFields(schema);
			mapper.writeValue(sw, schema);
			SearchIndex si = new SearchIndex(name, sw.toString(), indexedFields);
			this.documentDao.registerIndexedAttributes(name, indexedFields);
			return this.searchIndexDao.save(si);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Failed to deserialize schema somehow");
		}
		
	}
	
	private List<String> getIndexedFields ( JsonNode schema ){
		List<String> indexedFields = new ArrayList<>();
		JsonNode propertiesNode = schema.get("properties");
		Iterator<String> properties = propertiesNode.fieldNames();
		while (properties.hasNext()) {
			String propertyName = properties.next();
			JsonNode property = propertiesNode.get(propertyName);
			if( property.has("type") && property.get("type").asText().equals("string") && 
					property.has("indexed") && property.get("indexed").asBoolean()) {
				indexedFields.add(propertyName);
			}
		}
		return indexedFields;
	}
	
	public List<String> findAll(){
		List<String> si = new ArrayList<>();
		this.searchIndexDao.findAll().forEach(s -> si.add(s.getName()));
		return si;
	}
	
	@StateImpacting
	public boolean delete ( String name ) {
		if( ! this.searchIndexDao.existsById(name)) {
			System.out.println("brap");
			return false;
		}
		this.searchIndexDao.delete(this.searchIndexDao.findById(name).get());
		System.out.println("brop");
		return true;
	}
	public SearchIndex findOne(String name) {
		return this.searchIndexDao.findById(name).get();
	}
	
	@StateImpacting
	public String index(String name, JsonNode node ) {
		SearchIndex sIndex = this.searchIndexDao.findById(name).get();
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode schemaNode = mapper.readValue(sIndex.getSchema(), JsonNode.class);
			JsonSchema schema = this.JSON_SCHEMA_FACTORY.getJsonSchema(schemaNode);
			if ( schema.validInstance(node)) {
				String documentType = sIndex.getName();
				List<String> indexedFields = sIndex.getIndexedFields();
				Map<String, String> indexedFieldsValues = getIndexedValues(node, indexedFields);
				String id = getDocumentId( documentType, indexedFieldsValues.values());
				StringWriter sWriter = new StringWriter();
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.writeValue(sWriter,node);
				String fullDocument = sWriter.toString();
				IndexedDocument document = new IndexedDocument(id, documentType,
															indexedFieldsValues, fullDocument);
				this.documentDao.indexDocument(document);
				return id;
			}
			else {
				throw new RuntimeException("invalid data");
			}
			
		} catch (IOException | ProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Error occured parsing json",e);
		}
		//
	}
	
	public IndexedDocument getSingle ( String indexName, String docId ) {
		IndexedDocument result = this.documentDao.getSingle(indexName, docId);
		if ( result == null ){
			throw new DocumentNotFound(indexName, docId);
		}
		return result;
	}
	
	public void deleteSingle ( String indexName, String docId ) {
		boolean deleted = this.documentDao.deleteSingle(indexName, docId);
		if ( !deleted ) {
			throw new DocumentNotFound(indexName, docId);
		}
	}
	
	private Map<String, String> getIndexedValues ( JsonNode node, List<String> fields){
		Map<String, String> result = new HashMap<>();
		for ( String field : fields ) {
			if ( node.has(field)) {
				result.put(field, node.get(field).asText());
			}
		}
		if ( result.isEmpty()) {
			throw new RuntimeException("Document has no indexable attributes");
		}
		return result;
	}
	
	private String getDocumentId ( String docType, Collection<String> indexedAttributesValues ) {
		String toEncode = docType;
		for ( String attribute : indexedAttributesValues ) {
			toEncode +=attribute;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(toEncode.getBytes());
			byte[] digest = md.digest();
			String id = DatatypeConverter.printHexBinary(digest).toUpperCase();
			return id;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException ("Failed to generate id",e);
		}
	}

}
