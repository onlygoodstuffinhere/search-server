#### Sanic Indexer ####

This runs as a server and allows to dynamically create 
search indexes for arbitrary document types, to index
documents and search for them in a fast and typo tolerant way.

This is still in shitty snapshot version and not very polished.


##### Dependencies #####

You need java 8 and maven (build tool).
https://maven.apache.org/


##### Running it #####

You can get this running with the mvn spring-boot:run command
or generate jar by running mvn install.


##### Api #####

You interact with this through a rest api, data is serialized 
as json.

You want to :

1. Create a search index by uploading a document schema for the kind
of documents you're going to index in it.

2. Upload documents as defined by the schema from 1. for the server
to index.

3. Search the documents you indexed.

* Search index stuff

** Create an index : 

[POST] src-index/{name}

 - {name} is the name of your search index
 - Request body is a json schema draft-04 (hehe sry) to define the documents you index.
   http://json-schema.org/
   Add a boolean attribute to string fields you want to get indexed (there can be multiple
   fields indexed). Due to this being a mess atm, indexed fields can not be nested and can only
   be of string type, sorry.
   Here's a simple schema example
   
   {
	    "title": "Movie",
	    "type": "object",
	    "properties": {
	        "title": {
	            "type": "string",
		    "indexed": true
	        },
	        "genres": {
	            "type": "string"
	        },
	        "url": {
		    "type": "string"
		}
	    },
	    "required": ["title", "url"]
	}
 - Returns json representation of the search index created.	
	
** List indexes

[GET] src-index/
 
 - Returns a list of existing search index names.
 
** Get index

[GET] src-index/{name}

 - Returns json representation of search index called {name}
 
** Delete index 

[DELETE] src-index/{name}

 - Delete search index called name, returns HTTP-200 empty response.
 
* Document stuff :

** Index a document

[POST] src-index/document/{name}

 - {name} is the search index where you want to index your document.
 - Request body is a json object respecting the specified index's schema
  Keep in mind all this is indexed in memory so you might want to use some kind 
  of external id to refer to some other system the documents are coming from
  and not index the entire thing, only what you would need in search results.
  Example document using the schema from above : 
  {
  	"title": "The Shawshank Redemption",
  	"genres": "comedy",
  	"url": "http://www.imdb.com/title/tt0111161/"
  } 

* Search

[GET] search/{name}?query={query}

 - {name} is search index name
 - {query} is the query string
 - This returns an array of 10 (max) search results.
 example :
 [
  {
    "score": 1,
    "document": "{\"title\":\"Life of Pi\",\"genres\":\"Adventure,Drama,Fantasy\",\"url\":\"http://www.imdb.com/title/tt0454876\"}"
  },
  {
    "score": 0.8888888888888888,
    "document": "{\"title\":\"Life of Pie\",\"genres\":\"Drama\",\"url\":\"http://www.imdb.com/title/tt6272956\"}"
  },
  {
    "score": 0.7142857142857143,
    "document": "{\"title\":\"A Life of Sin\",\"genres\":\"Drama\",\"url\":\"http://www.imdb.com/title/tt0100026\"}"
  },
   [ ... ]
  ]


##### Configuration #####

Configuration is done in src/main/resources/application.properties file.
Just comment out stuff that you don't need.

Server port is changed in server.port property.

Relational database connection is configured with 
spring.datasource.{url | password | user} properties.

Redis connection configured with 
spring.redis.{host | port | password | database} properties.