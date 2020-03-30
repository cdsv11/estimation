package com.sellics.estimation.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sellics.estimation.service.AmazonAutoCompletionService;


/**
 * Initial strategy similar to strategy1 but is not good as it's almost impossible
 * to get 100
 * @author cdsvi
 *
 */
public class EstimationStrategy2 implements EstimationStrategy{

	private int numMaxRequests;
	private double minSimilarityConstant;
	
	private AmazonAutoCompletionService amazonService;
	
	public EstimationStrategy2(int numMaxRequests,AmazonAutoCompletionService amazonService) {
		this.numMaxRequests=numMaxRequests;
		this.amazonService = amazonService;
		this.minSimilarityConstant=0.8; //by default, if it's similar, should be at least similar. It's good to avoid low scores in big sentence results.
	}
	
	
	@Override
	public int calculate(String keyword) {
		int letterStep= keyword.length() / numMaxRequests; 
		System.out.println("length: "+keyword.length());
		letterStep += keyword.length() % numMaxRequests == 0 ? 0:1;
		List<String> words = Arrays.asList(keyword.split(" "));
		int wordCounter; //to avoid scoring words already "completed"
		double score=0;
		int numRequests = keyword.length()/letterStep;
		
		System.out.println("numRequests: "+ numRequests);
		for(int i= 0; i<numRequests;i++) {
			String substring= keyword.substring(0,(i+1)*letterStep);
			wordCounter = (int) substring.chars().filter(ch -> ch == ' ').count();
			List<String> results= this.amazonService.getPrefixAutoCompletion(substring);
			
			double similarity=0; //value between 1 and 0 that represents the similarity between one result and searched keyword
			int similarRes=0;
			for(String result : results){
				similarity += this.getResultSimilarity(result, words, wordCounter);//max is 1 when it represents the same sentence
				if(similarity>0)similarRes++;
			}
			
			double averageSimilarity = similarity/ results.size();
			score =score + averageSimilarity*(numRequests-i); //sooner the similarity, higher the score
			System.out.println("score: "+score);
		}
		return percentualScore(score, numRequests);
	}

	private double getResultSimilarity(String result, List<String> words, int wordCounter) {
		
		List<String> intersections= Arrays.asList(result.split(" "))
									.stream()
									.distinct()
									.filter(words::contains)
									.collect(Collectors.toList());
		
		int numberOfIntersections= intersections.size() - wordCounter; //because we don't count what was already written 
		int numberOfWordsOnResult = result.split(" ").length - wordCounter;
		double similarity =(double)numberOfIntersections/numberOfWordsOnResult;//max is 1 when it represents the same sentence
		if(similarity==0) {
			return 0;
		}else{ //don't penalize so much similarities on longer lines
			return similarity<minSimilarityConstant? minSimilarityConstant:similarity;
		}
	}
	
	
	private int percentualScore(double score,int numRequests) {
		int count = 1, sum = 0; //the sum represents the maximum score possible.
		
		while(count <= numRequests) {
			sum += count;
			count++;
		}
		
		return (int) (100*score/sum);//linear scaling
	}
	


}
