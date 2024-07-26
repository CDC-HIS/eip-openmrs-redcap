package org.ephi.eip.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RedcapClient {

    @Autowired
    private RedcapConfig redcapConfig;

    private final HttpClient client;

    public RedcapClient() {
        this.client = HttpClientBuilder.create().build();
    }

    public String post(JSONObject record) {
        this.redcapConfig.validateRedcapConfig();
        String result = "";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", redcapConfig.getRedcapToken()));
        params.add(new BasicNameValuePair("content", "record"));
        params.add(new BasicNameValuePair("format", "json"));
        params.add(new BasicNameValuePair("type", "flat"));

        JSONArray data = new JSONArray();
        data.put(record);
        params.add(new BasicNameValuePair("data", data.toString()));

        HttpPost post = new HttpPost(redcapConfig.getRedcapApiUrl());
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        try {
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(post);
            int responseCode = response.getStatusLine().getStatusCode();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                StringBuilder resultBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultBuilder.append(line);
                }
                result = resultBuilder.toString();
            }

            log.info("Response Code: {}", responseCode);
            log.info("Result: {}", result);

        } catch (Exception e) {
            log.error("Error posting to REDCap", e);
        }
        return result;
    }
}
