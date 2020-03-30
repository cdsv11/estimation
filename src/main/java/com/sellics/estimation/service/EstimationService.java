package com.sellics.estimation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sellics.estimation.strategy.EstimationStrategy;
import com.sellics.estimation.strategy.EstimationStrategy1;
import com.sellics.estimation.strategy.EstimationStrategy2;
import com.sellics.estimation.strategy.EstimationStrategy3;

@Service
public class EstimationService {

	@Value( "${amazon.strategy}" )
	private int strategyNumber;
	
	@Value( "${amazon.strategy1.nummaxreqs}" )
	private int numMaxRequests;
	
	@Autowired
	private AmazonAutoCompletionService autoCompletionService;

	public int estimate(String keyword) {
		EstimationStrategy strategy;
		switch(strategyNumber) {
		case 1:
			strategy = new EstimationStrategy1(numMaxRequests,autoCompletionService); break;
		case 2:
			strategy = new EstimationStrategy2(numMaxRequests,autoCompletionService); break;
		case 3:
			strategy = new EstimationStrategy3(numMaxRequests,autoCompletionService); break;
		default:
			System.out.println("Falling to default strategy");
			strategy = new EstimationStrategy1(numMaxRequests,autoCompletionService);
		}
		int score=strategy.calculate(keyword);
		System.out.println("Final Score:"+score);
		return score;
	}
	
	

}
