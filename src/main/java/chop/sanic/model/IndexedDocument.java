package chop.sanic.model;

import java.io.Serializable;
import java.util.Map;

public class IndexedDocument implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6701816472285099630L;
	private String id; //internal id
	private String documentType; //entity type
	private Map<String, String> indexedAttributes; //attribute name - attribute value
	private String document; //full doc as json
	
	public IndexedDocument(String id, String documentType, Map<String, String> indexedAttributes, String document) {
		super();
		this.id = id;
		this.documentType = documentType;
		this.indexedAttributes = indexedAttributes;
		this.document = document;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public Map<String, String> getIndexedAttributes() {
		return indexedAttributes;
	}

	public void setIndexedAttributes(Map<String, String> indexedAttributes) {
		this.indexedAttributes = indexedAttributes;
	}

	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		IndexedDocument other = (IndexedDocument) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	

}
