package cn.zongkuiy.grafana4tb.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.kv.Aggregation;
import org.thingsboard.server.common.data.kv.BaseReadTsKvQuery;
import org.thingsboard.server.common.data.kv.ReadTsKvQuery;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.dao.device.DeviceCredentialsService;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.dao.timeseries.TimeseriesService;

import com.google.common.util.concurrent.ListenableFuture;

import lombok.Data;


@Data
class Series {
	String target;
	String[][] datapoints;
}

@Data
class Annotation {
	String annotation;
	Long time;
	String title;
	String tags;
	String text;
}

@Data
class QueryResponse {
	String result;
}


@RestController
@RequestMapping("/grafana")
public class GrafanaController {
	

	@Autowired
	protected DeviceService deviceService;
	
	@Autowired
    private DeviceCredentialsService deviceCredentialsService;
	
	@Autowired
	private TimeseriesService tsService;

	@RequestMapping(value = "/{deviceToken}")
	@ResponseBody
	public QueryResponse list(@PathVariable String deviceToken, HttpServletResponse response) throws IOException {
		
		DeviceCredentials credentials = deviceCredentialsService.findDeviceCredentialsByCredentialsId(deviceToken);
		
		if(credentials == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		} else {
			QueryResponse res = new QueryResponse();
			res.setResult("200 ok");
			return res;
		}
	}

	@RequestMapping(value = "/{deviceToken}/search")
	@ResponseBody
	public List<String> search(@PathVariable String deviceToken, HttpServletResponse response) throws IOException, InterruptedException, ExecutionException {
		
		DeviceCredentials credentials = deviceCredentialsService.findDeviceCredentialsByCredentialsId(deviceToken);
		
		if(credentials == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		} else {
			ListenableFuture<List<TsKvEntry>> future = tsService.findAllLatest(null, credentials.getDeviceId());
			List<TsKvEntry> res = future.get();
			
			List<String> result = new ArrayList<String>();
			res.forEach(kv -> {
				result.add(kv.getKey());
			});
			return result;
		}
	}

	@RequestMapping(value = "/{deviceToken}/query")
	@ResponseBody
	public Series[] query(@PathVariable String deviceToken, @RequestBody Map<String, Object> params, HttpServletRequest request,
			HttpServletResponse response) throws IOException, InterruptedException, ExecutionException, ParseException {
		DeviceCredentials credentials = deviceCredentialsService.findDeviceCredentialsByCredentialsId(deviceToken);
		
		if(credentials == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		
		List<Map> targetList = (List) params.get("targets");
		Map range = (Map) params.get("range");
		
		
		
		String start = (String) range.get("from");
		String end = (String) range.get("to");
		Integer intervalMs = (Integer) params.get("intervalMs");
		Integer maxDataPoints = (Integer) params.get("maxDataPoints");
		
		
		ArrayList<String> keys = new ArrayList<String>();
		for (Map targetMap : targetList) {
			String target = (String) targetMap.get("target");
			keys.add(target);
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); 
		Long startTs = dateFormat.parse(start).getTime();
		Long endTs = dateFormat.parse(end).getTime();

		Aggregation agg = intervalMs == 0L ? Aggregation.valueOf(Aggregation.NONE.name()) : Aggregation.NONE;
        List<ReadTsKvQuery> queries = keys.stream().map(key -> new BaseReadTsKvQuery(key, startTs, endTs, intervalMs, maxDataPoints.intValue(), agg))
                .collect(Collectors.toList());

        ListenableFuture<List<TsKvEntry>> future = tsService.findAll(null, credentials.getDeviceId(), queries);
        List<TsKvEntry> ts = future.get();
        
        Series[] result = new Series[targetList.size()];
        
        HashMap<String, ArrayList<ArrayList<String>>> tsmap = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		for (TsKvEntry tsKvEntry : ts) {
			String key = tsKvEntry.getKey();
			if(tsmap.containsKey(key) == false) {
				tsmap.put(key, new ArrayList<ArrayList<String>>());
			}
			ArrayList<String> tkv = new ArrayList<String>();
			
			Series map = new Series();
			map.setTarget(tsKvEntry.getKey());
			String v = tsKvEntry.getValueAsString();
			tkv.add(v);
			tkv.add(String.valueOf(tsKvEntry.getTs()));
			
			tsmap.get(key).add(tkv);
		}
		
		Iterator<String> iterator = tsmap.keySet().iterator();
		int i = 0;
		while(iterator.hasNext()) {
			String key = iterator.next();
			result[i] = new Series();
			result[i].setTarget(key);
			String[][] datapoints = new String[tsmap.get(key).size()][];
			Iterator<ArrayList<String>> ite = tsmap.get(key).iterator();
			int j = 0;
			while(ite.hasNext()) {
				ArrayList<String> vs = ite.next();
				datapoints[j] = vs.toArray(new String[vs.size()]);
				j ++;
			}
			result[i].setDatapoints(datapoints );
			i ++;
		}
		return result;
	}

	@RequestMapping(value = "/{deviceToken}/annotations")
	@ResponseBody
	public Annotation[] Annotations(@PathVariable String deviceToken) {
		// NOT SUPPORT ANNOTATION YET
		Annotation[] annotations = new Annotation[0]; 
//		annotations[0] = new Annotation();
//		annotations[0].setAnnotation("aaa");
//		annotations[0].setTags("tags");
//		annotations[0].setText("txt");
//		annotations[0].setTime(System.currentTimeMillis());
//		annotations[0].setTitle("title");
		return annotations;
	}
}

