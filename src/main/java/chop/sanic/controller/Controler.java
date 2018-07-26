package chop.sanic.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;

import chop.sanic.model.IndexedDocument;
import chop.sanic.model.ScoredDocument;
import chop.sanic.model.SearchIndex;
import chop.sanic.services.SearchIndexService;
import chop.sanic.services.SearchService;

@Controller
@CrossOrigin
public class Controler {

	@Autowired
	SearchIndexService searchIndexService;
	
	@Autowired
	SearchService searchService;
	
	@RequestMapping(method = RequestMethod.GET, value = "src-index")
	public @ResponseBody List<String> list () {
		return this.searchIndexService.findAll();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="src-index/{name}")
	public @ResponseBody SearchIndex getIndex(@PathVariable String name) {
		return this.searchIndexService.findOne(name);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "src-index/{name}")
	@ResponseStatus(value = HttpStatus.OK)
	public void delete(@PathVariable(value="name") String name) {
		this.searchIndexService.delete(name);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "src-index/{name}")
	public @ResponseBody SearchIndex create ( @PathVariable String name, @RequestBody JsonNode schema) {
		return this.searchIndexService.create(name,schema);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "src-index/document/{name}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String index(@PathVariable String name, @RequestBody JsonNode object) {
		return this.searchIndexService.index(name, object);
	}

	@RequestMapping(method = RequestMethod.GET, value= "search/{name}")
	public @ResponseBody List<ScoredDocument> search ( @PathVariable String name, @RequestParam String query){
		return this.searchService.search(name, query);
	}
	
	@RequestMapping(method = RequestMethod.GET, value="src-index/document/{name}")
	public @ResponseBody IndexedDocument getSingle (@PathVariable String name, @RequestParam String id) {
		return this.searchIndexService.getSingle(name, id);
	}
	
	@RequestMapping ( method = RequestMethod.DELETE , value = "src-index/document/{name}")
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteSingle (@PathVariable String name, @RequestParam String id ) {
		this.searchIndexService.deleteSingle(name, id);
	}
	
}
