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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    	
    	String[] chars = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r",
    			"s","t","u","v","w","x","y","z"}; 
    	
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
    	
    	indexPerson(refLastName, refFirstName);
    	
    	firstNames.clear();
    	lastNames.clear();
    	/*for ( int eDist = 1 ; eDist <= 2 ; eDist ++ ) {
    		for ( int fl = 3 ; fl < refFirstName.length() ; fl ++ ) {
				String firstName = refFirstName;
				for ( int i = 0 ; i < eDist ; i ++ ) {
					int cn = (int) Math.floor(Math.random() * chars.length);
    				firstName = firstName.substring(0, fl) 
    						+ chars[cn] 
    						+ firstName.substring(fl, firstName.length());
    			}
				firstNames.add(firstName);
    			
    		}
    		for ( int ll = 3 ; ll < refLastName.length() ; ll ++ ) {
				String lastName = refLastName;
				for ( int i = 0 ; i < eDist ; i ++ ) {
					int cn = (int) Math.floor(Math.random() * chars.length);
    				lastName = lastName.substring(0, ll) 
    						+ chars[cn] 
    						+ lastName.substring(ll, lastName.length());
    			}
				lastNames.add(lastName);
    		}
    	}*/
    	
    	
    	firstNames = new ArrayList<>(Arrays.asList("bilbo", "balbo", 
    			"bulbo", "bilbbo", "bilbu", "balba", "bbilbo", "billbo",
    			"bbylbba", "balboa"));
    	lastNames = new ArrayList<>(Arrays.asList("baggins", "buggins", "boggins",
    			"bagins", "buhgins", "bagginz", "bagginch", "bagginski", "boginsk"));
    	
    	/*ObjectMapper mapper = new ObjectMapper();
    	int totalCount = 0;
    	int hitCount = 0;*/
    	for ( String firstName : firstNames ) {
    		for ( String lastName : lastNames ) {
    			//System.out.println(firstName + " - " + lastName);
    			//totalCount ++;
    			mockMvc.perform(get("/search/person").param("query", firstName + " " + lastName))
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("$").isArray())
    			.andExpect(jsonPath("$[0].document.firstName").value(refFirstName))
    			.andExpect(jsonPath("$[0].document.lastName").value(refLastName));
    			/*.andReturn().getResponse().getContentAsString();
    			
    			JsonNode resultNode = mapper.readValue(result, JsonNode.class);
    			if ( resultNode.has(0)) {
    				String fNameResult = resultNode.get(0).get("document").get("firstName").asText();
    				String lNameResult = resultNode.get(0).get("document").get("lastName").asText();
			
    				if ( fNameResult.equals(refFirstName) && lNameResult.equals(refLastName)) {
    					hitCount ++;
    				}
    			}
    			else {
    				System.out.println("lname : "+lastName);
    				System.out.println("first name : "+firstName);
    			}*/
    			
    			
    			/*.andExpect(jsonPath("$[0].document.firstName").value(refFirstName))
    			.andExpect(jsonPath("$[0].document.lastName").value(refLastName));
    			//.andDo(print());*/
    		}
    	}
    	/*System.out.println("total : "+totalCount);
    	System.out.println("hits : "+hitCount);*/
    	
    	 redisConnectionFactory.getConnection().flushDb();
    }
    
    
    private void indexPerson (String lName, String fName ) throws Exception {
    	String content = "{\"firstName\": \"";
		content += fName;
		content += "\", \"lastName\": \"";
		content += lName;
		content += "\"}";
		//System.out.println(content);
		mockMvc.perform(post("/src-index/document/person").
    			content(content).contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(status().isOk());
    }
    
	
	
	/*@Test
	public void contextLoads() {
	}*/
	
	

}
