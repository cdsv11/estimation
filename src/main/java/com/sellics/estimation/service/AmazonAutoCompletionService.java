package com.sellics.estimation.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class AmazonAutoCompletionService {

	@Value( "${amazon.autocomplete.url}" )
	private String amazonUrl;
	
	@Value( "${amazon.autocomplete.queryprefix}" )
	private String amazonQueryString;
	
	public List<String> getPrefixAutoCompletion(String prefix){
		System.out.println("prefix:" + prefix);
		URL url;
		List<String> results=new ArrayList<String>();
		try {
			url = new URL(amazonUrl+"?"+amazonQueryString+URLEncoder.encode(prefix, "UTF-8"));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			System.out.println("URL:"+con.getURL().toString());
			results= readAutoCompletionResponse(con.getInputStream());
			con.disconnect();
		} catch ( IOException e) {
			e.printStackTrace();
		}
		return results;
	}
	private List<String> readAutoCompletionResponse(InputStream input) throws IOException {
		BufferedReader in = new BufferedReader(
				  new InputStreamReader(input));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
				in.close();
				System.out.println("content:"+ content.toString());
				JSONArray resultJsonObject = new JSONArray(content.toString()).getJSONArray(1);
				List<String> list = new ArrayList<String>();
				for(int i = 0; i < resultJsonObject.length(); i++){
				    list.add(resultJsonObject.getString(i));
				}
				return list;
	}
	
}
