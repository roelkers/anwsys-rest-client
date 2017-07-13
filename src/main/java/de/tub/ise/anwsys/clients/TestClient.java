package de.tub.ise.anwsys.clients;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class TestClient {
	
	public static void main(String[] args) throws IOException, UnirestException {
		
		if(args.length < 1){
			System.out.println("Please specify the ports in the command line arguments where the SMEmu container and the REST interface is running.");
			return;
		}
		
		int portSMEmu = Integer.parseInt(args[0]);
		int portREST  = Integer.parseInt(args[1]);
		
		for(int k =0; k < 1000 ; k++){
			
			HttpResponse<JsonNode> responseMeters = Unirest.get("http://localhost:"+portSMEmu+"/meters").asJson();
			//System.out.println(String.format("Response was: " + responseMeters.getBody()));
			
			JSONObject jsonObj =  responseMeters.getBody().getObject();
			JSONArray jsonArray = jsonObj.getJSONArray("meters");
			
			int timeStamp;
			for(int i = 0; i < jsonArray.length(); i ++){
				String meterId = jsonArray.getString(i);
				
				HttpResponse<JsonNode> responseData = Unirest.get("http://localhost:"+portSMEmu+"/meters/"+meterId+"/data").asJson();
				
				System.out.println(String.format("Data was: " + responseData.getBody()));
				
				JSONObject dataObj = responseData.getBody().getObject();
				//System.out.println(dataObj.keySet().toString());
				timeStamp = dataObj.getInt("unixTimestamp");
				
				//JSONObject timeStamp = dataObj.getJSONObject("unixTimestamp");
				JSONArray jsonMeasurements = dataObj.getJSONArray("measurements");
				System.out.println(jsonMeasurements.toString());
				
				for(int j = 0; j < jsonMeasurements.length(); j++){
					JSONObject metric = jsonMeasurements.getJSONObject(j);
					
					//System.out.println("metricId"+metric.getString("metricId"));
					
					HttpResponse<JsonNode> responseRestMeters = Unirest.post("http://localhost:"+portREST+"/meters/"+meterId+"/metrics/"
					+metric.getString("metricId")+"/measurements")
					.field("timeMillis", timeStamp)
	        		.field("value", metric.getDouble("value"))
					.asJson();
					//System.out.println(String.format("Response was: " + responseRestMeters.getBody()));
				}
				
				HttpResponse<String> responseRestMetric = Unirest.get("http://localhost:"+portREST+"/meters/"+meterId+"/metrics/MX-11460-01").queryString("timeMillisMeasurement",timeStamp).asString();
				System.out.println(String.format("ResponseBody was: " + responseRestMetric.getBody()));
			}	
		}
	}
}

