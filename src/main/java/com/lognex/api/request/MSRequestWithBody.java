package com.lognex.api.request;

import com.lognex.api.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.lognex.api.util.Constants.APPLICATION_JSON_UTF8;

@Slf4j
public abstract class MSRequestWithBody extends MSRequest {

    MSRequestWithBody(String url, ApiClient client) {
        super(url, client);
    }

    @Override
    protected HttpUriRequest createRequest() {
        StringBuilder urlBuilder = new StringBuilder(getUrl());
        addExpandParameter(urlBuilder);
        HttpEntityEnclosingRequest request = produceHttpUriRequest(urlBuilder.toString());
        request.setHeader("ContentType", APPLICATION_JSON_UTF8);
        StringEntity entity;
        try {
            entity = new StringEntity(convertToJsonBody());
            request.setEntity(entity);
            entity.setContentType(APPLICATION_JSON_UTF8);
        } catch (UnsupportedEncodingException ignored) {
            log.error("Error while composing create request: ", ignored);
        } catch (IOException e) {
            log.error("Error while serializing entity to json", e);
        }
        return (HttpUriRequest) request;
    }

    protected abstract HttpEntityEnclosingRequest produceHttpUriRequest(String url);

    protected abstract String convertToJsonBody() throws IOException;
}
