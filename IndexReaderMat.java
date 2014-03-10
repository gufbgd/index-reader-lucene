import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexReaderMat {

	static String indexPath = "index";
	public static final String ARTICLETEXT = "ArticleText";
	public static final String ARTICLENAME = "name";
	static String csvMatrix = "Matrix.csv";

	public static void main(String[] args) throws IOException {

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
				indexPath)));
		//indexLecture(reader);
		
		int numberSearch = 5;
		String queryString = "blabla";
		TopDocs results = indexSearch(queryString, reader,numberSearch);
		
		
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits);
		int end = Math.min(numTotalHits, numberSearch);
		
		for(int i = 0;i<end;i++){
			
			System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
			System.out.println(reader.document(hits[i].doc).getField(ARTICLENAME).stringValue());
			
		}
		
		reader.close();
	}

	public static TopDocs indexSearch(String textquery, IndexReader reader, int numberSearch){
		// Permet de récupérer les documents les plus proches
		
		TopDocs results = null;
		
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new FrenchAnalyzer(Version.LUCENE_46);
		QueryParser parser = new QueryParser(Version.LUCENE_46, ARTICLETEXT, analyzer);
		
		try {
			Query query = parser.parse(textquery);
			results = searcher.search(query, numberSearch);
			
			
			
		} catch (ParseException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
		}
		
		
		return results;
	}
	
	
	static void indexLecture(IndexReader reader) throws IOException {
		//Crée un fichier csv avec tous les documents et mots de l'index
		
		int max = reader.maxDoc();
		System.out.println(max);
		TermsEnum term = null;
		// iterate docs
		HashSet<String> termSpace = new HashSet<String>();
		
		ArrayList<ArrayList<String>> matrixTerme = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<Long>> matrixFreq = new ArrayList<ArrayList<Long>>();
		//HashMap<String, Long> vecteurTerme = new HashMap<String,Long>();
		
		
		for (int i = 0; i < max; ++i) {
			// get term vector for body field
			final Terms terms = reader.getTermVector(i, ARTICLETEXT);
			ArrayList<Long> freq = new ArrayList<Long>();
			ArrayList<String> vecteurTerme = new ArrayList<String>();
			freq.clear();
			vecteurTerme.clear();
			if (terms != null) {
				int k=0;
				term = terms.iterator(term);
				while (term.next() != null) {
					
					termSpace.add(term.term().utf8ToString());
					vecteurTerme.add(term.term().utf8ToString());
					freq.add(term.totalTermFreq());
				}
				
			}
			matrixTerme.add(vecteurTerme);
			matrixFreq.add(freq);
			for(int f = 0 ; f < vecteurTerme.size();++f){
				System.out.println(i + " / " + vecteurTerme.get(f)+ " - " + freq.get(f));
			}
			
			
		}
		long[][]matrix =new long[max][termSpace.size()]; 
		System.out.println("nb mots: " + termSpace.size());
		
		FileWriter writer = new FileWriter(csvMatrix);
		
		int j = 0;
		for (String s:termSpace){
			writer.append(s + '\t');
			for (int i = 0 ; i < max ; ++i){
				int ind = matrixTerme.get(i).indexOf(s);
				if(ind>-1){
					System.out.println(ind);
					matrix[i][j]=matrixFreq.get(i).get(ind);
				}
			}
			++j;
		}
		writer.append('\n');
		
		
		
		
		for(int i = 0 ; i < max ;++i){
			for(int k = 0; k < termSpace.size() ; ++k){
				writer.append("" + matrix[i][k] + '\t');
			}
			writer.append('\n');
			
		}
		
		writer.flush();
		writer.close();
		
		
		for (int i = 0; i < max; ++i) {
			// get term vector for body field
			final Terms terms = reader.getTermVector(i, ARTICLETEXT);

			

			if (terms != null) {
				// count terms in doc
				int numTerms = 0;
				term = terms.iterator(term);
				while (term.next() != null) {
					
					
					System.out.println("doc " + i + " - term '"
							+ term.term().utf8ToString() + "' "
							+ term.totalTermFreq());
					++numTerms;
				}
				System.out.println("doc " + i + " - " + numTerms + " terms");

			} else {
				System.err.println("doc " + i + " - aucun");
			}

		}

	}
}
