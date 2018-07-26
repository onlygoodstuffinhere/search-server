package chop.sanic.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)

public class DocumentNotFound extends RuntimeException {

	public DocumentNotFound(String docType, String id) {
		super("Document not found index : "+docType+" id : "+id);
	}
}
