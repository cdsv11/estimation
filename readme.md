# Amazon Search-Volume calculator

## Pre-notes
There are some variances in the algorithms provided. 3 very similar approaches were written with some minor changes just for the sake of testing. For this assignment, Strategy 1 should be validated. 

## Problem

The problem consists in finding a way to reverse-engineer the auto-completion API from amazon to find the search-volume of a keyword. 
The proposal is to create a REST microservice that serves an endpoint to calculate this search volume.

"This endpoint should receive a single keyword as an input and should return a score for that
same exact keyword. The score should be in the range [0 → 100] and represent the
estimated search-volume (how often Amazon customers search for that exact keyword). A
score of 0 means that the keyword is practically never searched for, 100 means that this is
one of the hottest keywords in all of amazon.com right now."


## Assumptions taken

- The order of the autocompletion results is irrelevant: Since the volume is comparatively insignificant and since the assignment is supposed to have a time-limit this order was not accounted.
- The words were scored independently: Following the algorithm logic, the dependence between words is not accounted for. For example, "iphone" is searched a lot, but "iphone+vegetables" is not searched. However, has there is word dependency, "iphone+vegetables" will still hold some score from the "iphone" prefix. This can be solved by scoring 0 if at some point the amazon API stops suggesting (This is the what EstimationStrategy3 does).
- The later a similar suggestion appears, the lower search-volume score it has. For example: if the word "iphone" is searched and the amazon API suggests "iphone" after only written the "i" it's because "iphone" is a very searched word. If amazon API only suggests "ipad" after typed "ipa", it means it isn't as searched.


##Algorithm

The core of the algorithm is based on the previous assumptions and starts by taking into account a maximum number of requests and splits the keyword in the number of requests possible. For example, if number of requests is 5 and our keyword is 10 characters. The algorithm will perform the search using a 2 character interval. In the case of the word "smartphone", it will be searched as "sm","smar","smartp","smartpho","smartphone".The bests scenario is when we have a higher limit of maximum number of requests, so there is the possibility of searching letter by letter. This helps to take into consideration the SLA response time but also decreases accuracy.

For each search on amazon API, the algorithm checks if there is any intersections between the searched keyword and the provided algorithms. If there are intersections, it gives a good score to the result if not it gives a lower score. It gives a score for each search based on the average of the scored of each result in that search. 
After that, it takes into account the order in which results are found. It means that if a search has a good score early on, that score has a larger and linear weight than the higher score later on the search (this was based on the assumption referred previously).

Note: The internal calculated scores are dependent on the length of the keyword. At the end this is linearly transposed to a range of [0->100]. The range of the searched keyword is from 0 to the sum of the sequence of Natural numbers from 0 to the length of the keyword and it's calculated by this piece of code:

```java
		int count = 1, sum = 0; //the sum represents the maximum score possible.
		while(count <= numRequests) {
			sum += count;
			count++;
		}
```

## the order of the 10 returned keywords is comparatively insignificant
	
It's easy to identify that the order of the results matters. Despite not being taken in consideration in this algorithm we can test if it's significant by searching for the score of a list of results. 
Due to the fact that the algorithm scores more in early correct results, there isn't much difference or it is not much significant the score difference between them. But later in the search, the difference between the scores of results is higher (not that high).
Early suggestions differ from around 3-4 points. And late suggestions may differ 10-15 points in score.

I would consider less than 10% difference insignificant and probably, in the cases where the difference is larger, it can be due to constraints of the algorithm. For example, the fact that it scores less the late results, making a larger difference in the end.


## Precision
The impression is that it can find differences in scores between keywords, but however, it has a problem of having a word dependency that is stronger at the beginning of the search. 

The "iphone" example is very good to show this. The "iphone 11 case" is more popular than "iphone charger" ,according to keywordtool.io , by 3 times. As the iphone is a common prefix and it has a lot of value, it will drag the keyword "iphone charger" higher than supposed.

In a simple test, the results of the search:
- iphone charger: 88
- iphone 11 case: 91
- charger iphone: 35
- case iphone 11: 11

This doesn't seem coherent, because the order of the searched word is taken into account. Maybe a process where there would be rotations of the sentence would bring an average score to (at least) a coherent result. 

On other note, we can see that both "iphone charger" and "iphone 11 case" have strong results, which means that we can probably lower down the weight that the "early on" importance using a constant (it was supposed to be the change on a strategy 4) but not implemented due to lack of time. 

Taken this into account, I think the strategy 3 is the one that has a best precision if we take into consideration the sentence as one, and the strategy 1 in the case where the words are independent. Nonetheless, both can have results very imprecise. There is a lot to improve and very business knowledge to apply to the solution and more parameters to implement and adjust to find the coherence with the amazon data. 

## Suggestions / limitations
- If possible take full advantage of the 10 seconds of SLA and run the same algorithm with different combinations of the sentences.
- With this algorithm we can have approximately 90 requests to amazon API until we cap the 10 seconds of SLA. This would only happen if the searched keyword was 90 characters long. Which suggests that there is room for more improvement.
- "iphone charger" and "iphone case" are different things and there is a possibility where the algorithm scores them very similarly because they have the "iphone" keyword hooked. Some solution to this would be to change the words order and influence the final score based on this new scores. Meaning that if "case iphone" has higher scores than "charger iphone" then probably "iphone case" should have a larger impact than "iphone charger".

## Development feedback

The assignment was done using a basis of a general idea written in the assumptions and it had some small evolutions that are discriminated using strategies. As it's, in part, a creative process, it was taken into account some time to make better suggestions, but tried not to over extend the suggested time of 3 hours. The assignment took me about just that to implement and I used an extra hour of improvements and exploratory tests.
