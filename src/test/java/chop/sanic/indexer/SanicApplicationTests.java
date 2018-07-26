package chop.sanic.indexer;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//import org.assertj.core.util.Arrays;
//import org.assertj.core.util.Arrays;
import org.assertj.core.util.Files;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


import chop.sanic.dao.SearchIndexDao;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SanicApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private SearchIndexDao searchIndexDao;
	
	@Autowired
	RedisConnectionFactory redisConnectionFactory;
	
	private static String personSchema;
	
	@BeforeClass
	public static void loadResources() {
		File personSchemaFile = new File("src/test/resources/person-schema.json");
		personSchema = Files.contentOf(personSchemaFile, "UTF-8");
	}

    @Test
    public void indexStuffWorks() throws Exception {
    	mockMvc.perform(post("/src-index/person").
    			content(personSchema).contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$.indexedFields").value(
    				org.assertj.core.util.Arrays.asList(new String[] {"firstName", "lastName"})));
    	
    	assertTrue(searchIndexDao.existsById("person"));
    	
    	mockMvc.perform(get("/src-index/"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$").value(
    				org.assertj.core.util.Arrays.asList(new String[] {"person"})));
    	
    	mockMvc.perform(get("/src-index/person"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$.name").value("person"));
    	
    	mockMvc.perform(post("/src-index/person").
    			content(personSchema).contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(status().is(400));
    	
    	mockMvc.perform(delete("/src-index/person"))
    		.andExpect(status().isOk());
    	
    	assertTrue(! searchIndexDao.existsById("person"));
    }

    @Test
    public void searchStuffWorks() throws Exception {
    	mockMvc.perform(post("/src-index/person").
    			content(personSchema).contentType(MediaType.APPLICATION_JSON_UTF8));
    	String refFirstName = "bilbo";
    	String refLastName = "baggins";
    	
    	
    	ArrayList<String> firstNames = new ArrayList<>(Arrays.asList(
    			"bill", "billy", "brand", "robert", "jack", "michael", "pierre", "paul", "jacques",
    			"jacob", "jeremy", "henry", "manuel","emmanuel","antony","frodo","peregrin","meriadoc",
    			"boromir", "faramir", "bart", "lisa", "homer", "marjory"
    			));
    	
    	ArrayList<String> lastNames = new ArrayList<>(Arrays.asList(
    			"underhill", "simpson", "touque", "brandebouc", "johnsson", "bourdin"
    			));
    	
    	for ( String lName : lastNames ) {
    		for ( String fName: firstNames ) {
    			indexPerson(lName, fName);
    		}
    	}
    	
    	String refId = indexPerson(refLastName, refFirstName);
    	
    	firstNames.clear();
    	lastNames.clear();
    	firstNames = new ArrayList<>(Arrays.asList("bilbo", "balbo", 
    			"bulbo", "bilbbo", "bilbu", "balba", "bbilbo", "billbo",
    			"bbylbba", "balboa"));
    	lastNames = new ArrayList<>(Arrays.asList("baggins", "buggins", "boggins",
    			"bagins", "buhgins", "bagginz", "bagginch", "bagginski", "boginsk"));
    	

    	for ( String firstName : firstNames ) {
    		for ( String lastName : lastNames ) {
    			mockMvc.perform(get("/search/person").param("query", firstName + " " + lastName))
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("$").isArray())
    			.andExpect(jsonPath("$[0].document.firstName").value(refFirstName))
    			.andExpect(jsonPath("$[0].document.lastName").value(refLastName));
    			
    		}
    	}
    	
    	mockMvc.perform(get("/src-index/document/person").param("id", refId))
    	.andExpect(status().isOk())
    	.andExpect(jsonPath("$.indexedAttributes.firstName").value(refFirstName))
    	.andExpect(jsonPath("$.indexedAttributes.lastName").value(refLastName));
    	
    	mockMvc.perform(delete("/src-index/document/person").param("id", refId))
    	.andExpect(status().isOk());
    	
    	mockMvc.perform(get("/src-index/document/person").param("id", refId))
    	.andExpect(status().isNotFound());
    	
    	 redisConnectionFactory.getConnection().flushDb();
    }
    
    
    private String indexPerson (String lName, String fName ) throws Exception {
    	String content = "{\"firstName\": \"";
		content += fName;
		content += "\", \"lastName\": \"";
		content += lName;
		content += "\"}";
		return mockMvc.perform(post("/src-index/document/person").
    			content(content).contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(status().isOk())
    		.andReturn().getResponse().getContentAsString();
    }
 

}
