package chop.sanic.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import chop.sanic.model.SearchIndex;

@Repository
public interface SearchIndexDao extends CrudRepository<SearchIndex, String>{

}
