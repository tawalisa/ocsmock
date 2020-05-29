package com.example.ocsmock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class OcsmockApplication {
	private static Map<String, String> data = new HashMap();

	final static LongAdder count = new LongAdder();
	public static void main(String[] args) throws IOException {
		String mockdata = "C:\\code\\tawalisa\\ocsmock\\data\\mock.json";
		if(args != null){
			mockdata = args[0];
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					System.out.println("count----->>>>>>>>"+count.sum());
					count.reset();
					try {
						Thread.sleep(1000 * 10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}
		}).start();
		ObjectMapper mapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Map<String, Object> mockDate = mapper.readValue(FileUtils.readFileToString(new File(mockdata), "utf-8"), new TypeReference<Map<String, Object>>() {
		});
		List<Pair> list = (List) ((Map) getValFromMAp(getValFromMAp(getValFromMAp(mockDate, "extensions"), "debug"), "dataSources")).
				values().stream().map(value -> {

			Object request = getValFromMAp(value, "request");
			String uri = (String) getValFromMAp(request, "uri");
			try {
				uri = new URI(uri).getPath();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			String body = (String) getValFromMAp(request, "body");
			String response = (String) getValFromMAp(getValFromMAp(value, "response"), "body");
			return Pair.of(uri + "-" + body.replaceFirst("current_to[\"][:][\"]([^\"]*)[\"]","current_to:\"2045-01-01T00:00:00Z\""), response);
		}).collect(Collectors.toList());
		for(Pair<String, String> pair:list){
			data.put(pair.getKey(),pair.getValue());
		}
		SpringApplication.run(OcsmockApplication.class, args);
	}

	private static Object getValFromMAp(Object map, String key) {
		return ((Map)map).get(key);
	}

	@RequestMapping("/**")
	public String home(final HttpServletRequest request,  @RequestBody String requestBody) throws InterruptedException {
		String retu = data.get(request.getRequestURI()+"-"+requestBody.replaceFirst("current_to[\"][:][\"]([^\"]*)[\"]","current_to:\"2045-01-01T00:00:00Z\""));
//		if( retu == null){
//			System.out.println(request.getRequestURI()+"-"+requestBody);
//		}
//		Thread.sleep(1000L);
		count.increment();
		return retu;
	}



}
