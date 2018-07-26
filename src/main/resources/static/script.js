var model = {
		"indexname": "gutenberg",
		"search": {
			"query": "ayy",
			"results": []
		}
		
};

var searchThreshold = 3;
var searchTimeout = 3000; //3s
var searchTrigger;
var searching = false;

document.addEventListener("DOMContentLoaded", function(event) {
	registerListResultListeners();
	registerIndexNameHandlers();
	registerSearchTriggers();
});

function registerListResultListeners(){
	let resultItems = document.querySelectorAll(".result-block");
	//console.log(resultItems.length());
	resultItems.forEach(function(i){
		i.addEventListener("click", function(e){
			i.classList.toggle("expanded");
		});
		
	});
}

function registerIndexNameHandlers(){
	document.getElementById("index-name-button").addEventListener("click", function(e){
		let indexName = document.getElementById("index-name-input").value;
		document.getElementById("index-name-input").value = "";
		document.getElementById("index-name-display").innerHTML = indexName;
	});	
}

function registerSearchTriggers(){
	document.getElementById("src-button").addEventListener("click", doSearch);
	document.getElementById("search-input").addEventListener("input", onSearchInputChange );
}

function onSearchInputChange (){
	let query = document.getElementById("search-input").value;
	if ( query.length <= 4 ){
		return;
	}
	if ( (query.startsWith(model.search.query) && 
			( Math.abs( query.length - model.search.query.length) <= searchThreshold )) || searching === true){
		//only fire search after a while
		window.clearTimeout(searchTrigger);
		searchTrigger = window.setTimeout(doSearch, searchTimeout);
		return;
	}
	else{
		window.clearTimeout(searchTrigger);
		doSearch();
	}
}

function doSearch(){
	let query = document.getElementById("search-input").value;
	searching = true;
	fetch('/search/' + model.indexname + "?query="+encodeURI(query))
	  .then(function(response) {
		model.search.query = query;
		searching = false;
		return response.json();
		//displayResults();
	  })
	  .then(function(responseJson){
		  model.search.results = responseJson;
		  displayResults();
	  });
}

function displayResults(){
	var resultDiv = document.getElementById("search-result");
	while(resultDiv.hasChildNodes()){
		resultDiv.removeChild( resultDiv.firstChild );
	}
	var rTemplate = document.getElementById("result-template");
	//console.log(rTemplate);
	//let prevNode = null;
	model.search.results.forEach(function (r){
		
		//get template
		let rDiv = document.importNode(rTemplate.content, true);
		let rHeaders = rDiv.querySelectorAll(".result-header");
		rHeaders[0].innerHTML = "Title";
		rHeaders[1].innerHTML = "Author";
		rHeaders[2].innerHTML = "Link";
		
		let rVals = rDiv.querySelectorAll(".result-value");
		rVals[0].innerHTML = r.document.title;
		rVals[1].innerHTML = r.document.author;
		rVals[2].innerHTML = r.document.formaturi[0];
		
		resultDiv.appendChild(rDiv);
		
	});
	registerListResultListeners();
}


