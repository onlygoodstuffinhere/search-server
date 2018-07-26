package chop.sanic.config;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import chop.sanic.model.ScoredDocument;

public class ScoredDocumentSerializer extends StdSerializer<ScoredDocument> {

	/*public ScoredDocumentSerializer() {
		super(null);
	}*/
	private ObjectMapper mapper;
	
	protected ScoredDocumentSerializer(Class<ScoredDocument> t, ObjectMapper mapper) {
		super(t);
		this.mapper = mapper;
	}
	@Override
	public void serialize(ScoredDocument sd, JsonGenerator jg, SerializerProvider sp) throws IOException {
		jg.writeStartObject();
		
		jg.writeNumberField("score", sd.getScore());
		//jg.writeObjectFieldStart("document");
		
		
		JsonNode node = mapper.readTree(sd.getDocument());
		jg.writeFieldName("document");
		jg.writeTree(node);
		//jg.writeEndObject();
		jg.writeEndObject();
	}

}
