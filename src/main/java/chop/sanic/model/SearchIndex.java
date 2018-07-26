package chop.sanic.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

@Entity
public class SearchIndex {

	@Id
	private String name;
	
	@Lob @Type(type = "text")
	private String schema;
	
	@ElementCollection
	private List<String> indexedFields;
	
	public SearchIndex() {
		this.indexedFields = new ArrayList<>();
	}

	public SearchIndex(String name, String schema, List<String> indexedFields) {
		super();
		this.name = name;
		this.schema = schema;
		this.indexedFields = indexedFields;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public List<String> getIndexedFields() {
		return indexedFields;
	}

	public void setIndexedFields(List<String> indexedFields) {
		this.indexedFields = indexedFields;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indexedFields == null) ? 0 : indexedFields.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
		SearchIndex other = (SearchIndex) obj;
		if (indexedFields == null) {
			if (other.indexedFields != null)
				return false;
		} else if (!indexedFields.equals(other.indexedFields))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SearchIndex [name=" + name + ", schema=" + schema + ", indexedFields=" + indexedFields + "]";
	}
	
	
	


}
