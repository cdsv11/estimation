package com.sellics.estimation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sellics.estimation.service.EstimationService;

@RestController
@RequestMapping("/estimate")
public class EstimationController {

	@Autowired
	private EstimationService estimationService;
	
	
	@GetMapping
	public int getEstimation( @RequestParam String keyword) {
		System.out.println("keyworks:" + keyword);
		
		return this.estimationService.estimate(keyword); //maybe transform in list of strings? arguments, otherwise dont over engineer it
		}
	
}
