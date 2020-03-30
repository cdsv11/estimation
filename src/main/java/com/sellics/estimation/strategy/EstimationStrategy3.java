package com.sellics.estimation.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sellics.estimation.service.AmazonAutoCompletionService;


/**
 * A variance from StrategyEstimation1 where it gives out 0 score if there is no result for the full sentence.
 * @author cdsvi
 *
 */
public class EstimationStrategy3 implements EstimationStrategy{

	private int numMaxRequests;
	
	private AmazonAutoCompletionService amazonService;
	
	public EstimationStrategy3(int numMaxRequests,AmazonAutoCompletionService amazonService) {
		this.numMaxRequests=numMaxRequests;
		this.amazonService = amazonService;
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
			if(results.isEmpty()) {
				score=0;break;
			}
			double similarity=0; //value between 1 and 0 that represents the similarity between one result and searched keyword
			for(String result : results){
				similarity += this.getResultSimilarity(result, words, wordCounter);//max is 1 when it represents the same sentence
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
		return numberOfIntersections>0?1:0;
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
