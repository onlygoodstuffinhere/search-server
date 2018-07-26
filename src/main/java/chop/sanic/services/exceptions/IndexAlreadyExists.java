package chop.sanic.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IndexAlreadyExists extends RuntimeException {

	public IndexAlreadyExists() {
		super("Search index already exists");
	}

	public IndexAlreadyExists(String indexName) {
		super("Search index already exists - "+indexName);
	}

	
}
