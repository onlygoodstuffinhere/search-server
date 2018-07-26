package chop.sanic.utils;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexUtils {
	private static DoubleMetaphone dblMetaphone = initMetaphone();
	private static Set<String> englishStopWords = new HashSet<>(Arrays.asList("a", "about",
			"above", "above", "across", "after", "afterwards",
			"again", "against", "all", "almost", "alone", "along",
			"already", "also","although","always","am","among",
			"amongst", "amoungst", "amount",  "an", "and", "another",
			"any","anyhow","anyone","anything","anyway", "anywhere",
			"are", "around", "as",  "at", "back","be","became",
			"because","become","becomes", "becoming", "been", "before", 
			"beforehand", "behind", "being", "below", "beside",
			"besides", "between", "beyond", "bill", "both", "bottom",
			"but", "by", "call", "can", "cannot", "cant", "co", 
			"con", "could", "couldnt", "cry", "de", "describe", "detail", 
			"do", "done", "down", "due", "during", "each", "eg", "eight",
			"either", "eleven","else", "elsewhere", "empty", "enough",
			"etc", "even", "ever", "every", "everyone", "everything",
			"everywhere", "except", "few", "fifteen", "fify", "fill",
			"find", "fire", "first", "five", "for", "former", "formerly",
			"forty", "found", "four", "from", "front", "full", "further",
			"get", "give", "go", "had", "has", "hasnt", "have", "he",
			"hence", "her", "here", "hereafter", "hereby", "herein",
			"hereupon", "hers", "herself", "him", "himself", "his",
			"how", "however", "hundred", "ie", "if", "in", "inc", "indeed",
			"interest", "into", "is", "it", "its", "itself", "keep", "last",
			"latter", "latterly", "least", "less", "ltd", "made", "many", "may",
			"me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most",
			"mostly", "move", "much", "must", "my", "myself", "name", "namely",
			"neither", "never", "nevertheless", "next", "nine", "no", "nobody",
			"none", "noone", "nor", "not", "nothing", "now", "nowhere", "of",
			"off", "often", "on", "once", "one", "only", "onto", "or", "other",
			"others", "otherwise", "our", "ours", "ourselves", "out", "over",
			"own","part", "per", "perhaps", "please", "put", "rather", "re",
			"same", "see", "seem", "seemed", "seeming", "seems", "serious",
			"several", "she", "should", "show", "side", "since", "sincere",
			"six", "sixty", "so", "some", "somehow", "someone", "something",
			"sometime", "sometimes", "somewhere", "still", "such", "system",
			"take", "ten", "than", "that", "the", "their", "them",
			"themselves", "then", "thence", "there", "thereafter", "thereby",
			"therefore", "therein", "thereupon", "these", "they", "thickv", 
			"thin", "third", "this", "those", "though", "three", "through", 
			"throughout", "thru", "thus", "to", "together", "too", "top",
			"toward", "towards", "twelve", "twenty", "two", "un", "under",
			"until", "up", "upon", "us", "very", "via", "was", "we", "well",
			"were", "what", "whatever", "when", "whence", "whenever", "where",
			"whereafter", "whereas", "whereby", "wherein", "whereupon",
			"wherever", "whether", "which", "while", "whither", "who", 
			"whoever", "whole", "whom", "whose", "why", "will", "with",
			"within", "without", "would", "yet", "you", "your", "yours", 
			"yourself", "yourselves", "the"));
	
	private static final int MIN_PREFIX_LENGTH = 2;
	private static final Logger logger = LoggerFactory.getLogger(IndexUtils.class);
	private IndexUtils () {}
	
	private static Set<String> removeStopWords( Set<String> input ){
		int inputSize = input.size();
		//input.removeIf(s -> englishStopWords.contains(s));
		Set<String> result = new HashSet<>();
		for ( String s : input ) {
			if ( ! englishStopWords.contains(s)) {
				result.add(s);
			}
		}
		logger.debug("REMOVE STOPWORDS - input size : "+inputSize+" - output size : "+result.size());
		return result;
	}
	
	private static DoubleMetaphone  initMetaphone () {
		DoubleMetaphone metaphone = new DoubleMetaphone();
		metaphone.setMaxCodeLen(12);
		return metaphone;
	}

	public static String toLowerCase ( String input ) {
		return input.toLowerCase(Locale.ENGLISH);
	}
	
	public static Set<String> tokenize(String input ) {
		Set<String> result =  new HashSet<>(Arrays.asList(input.split("[\\P{IsAlphabetic}&&[^\\p{Digit}]]+")));
		result.remove(null);
		result.remove("");
		return result;
	}
	
	public static Set<String> metaphone (String input){
		Set<String> result = new HashSet<>();
		String dblMetaphone1 = IndexUtils.dblMetaphone.doubleMetaphone(input, false);
		String dblMetaphone2 = IndexUtils.dblMetaphone.doubleMetaphone(input, true);
		if ( dblMetaphone1 != null && ! dblMetaphone1.isEmpty()) {
			result.add(dblMetaphone1);
		}
		if( dblMetaphone2 != null && ! dblMetaphone2.isEmpty()) {
			result.add(dblMetaphone2);
		}
		if ( input.matches("\\p{Digit}+")) {
			result.add(input);
		}
		return result;
	}
	
	public static Set<String> prefixes ( String input ){
		Set<String> result = new HashSet<>();
		if ( input.length() >= IndexUtils.MIN_PREFIX_LENGTH ) {
			for ( int i = IndexUtils.MIN_PREFIX_LENGTH ; i<= input.length(); i++ ) {
				result.add(input.substring(0, i));
			}
		}
		return result;
	}
	
	public static Set<String> toIndexable (String input ){
		String lowerCased = IndexUtils.toLowerCase(input);
		Set<String> tokenized = IndexUtils.tokenize(lowerCased);
		Set<String> cleaned = removeStopWords( tokenized); //experimental
		Set<String> metaphoned = new HashSet<>();
		for ( String token : cleaned ) {
			metaphoned.addAll(IndexUtils.metaphone(token));
		}
		Set<String> prefixed = new HashSet<>();
		for ( String phonetic : metaphoned ) {
			prefixed.addAll(IndexUtils.prefixes(phonetic));
		}
		return prefixed;
	}
	
	public static Set<String> toSearchable ( String input){
		String lowerCased = IndexUtils.toLowerCase(input);
		Set<String> tokenized = IndexUtils.tokenize(lowerCased);
		Set<String> cleaned = removeStopWords( tokenized); //experimental
		Set<String> metaphoned = new HashSet<>();
		for ( String token : cleaned ) {
			metaphoned.addAll(IndexUtils.metaphone(token));
		}
		
		if ( logger.isDebugEnabled() ) {
			StringWriter sw = new StringWriter();
			sw.append("TO SEARCHABLE : query : [ ");
			sw.append(input + " ] tokens : [ ");
		
			for ( String index : metaphoned) {
				sw.append(index + ", ");
			}
			sw.append(" ]");
			logger.debug(sw.toString());
		}
		
		return metaphoned;
	}
	
	public static String toComparable (String input ) {
		String lowercased= toLowerCase(input);
		return lowercased.replaceAll("[\\P{IsAlphabetic}&&[^\\p{Digit}]]+", "");
	}

}
