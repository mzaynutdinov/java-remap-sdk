package com.lognex.api.request;

import com.lognex.api.ApiClient;
import com.lognex.api.exception.ResponseException;
import com.lognex.api.response.ApiResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

@Getter
@Slf4j
public abstract class MSRequest {

    private String url;
    private ApiClient client;
    private Set<String> expand = new HashSet<>();
    protected Set<RequestOption> options = new HashSet<>();

    MSRequest(String url, ApiClient client){
        this.url = url;
        this.client = client;
    }

    public MSRequest addExpand(String expandParam) {
        checkState(expandParam.split("\\.").length <= 3, "max depth of expand equals 3");
        this.expand.add(expandParam);
        return this;
    }

    public MSRequest addOption(RequestOption option) {
        this.options.add(option);
        return this;
    }

    public ApiResponse execute() throws ResponseException {
        try (CloseableHttpClient httpclient = buildHttpClient(client.getLogin(), client.getPassword())) {
            HttpUriRequest request = buildRequest();
            CloseableHttpResponse response = httpclient.execute(request);
            return ResponseParser.parse(response, this);
        } catch (IOException e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
    }

    public HttpUriRequest buildRequest(){
        HttpUriRequest result = createRequest();
        fillHeaders(result);
        return result;
    }

    private void fillHeaders(HttpUriRequest request){
        options.stream()
                .filter(o-> o != null && (o.getHeader() != null && !o.getHeader().isEmpty()) && (o.getValue() != null && !o.getValue().isEmpty()))
                .forEach(o -> request.addHeader(o.getHeader(), o.getValue()));
    }

    protected abstract HttpUriRequest createRequest();

    void addExpandParameter(StringBuilder currentUrl){
        if (!expand.isEmpty()) {
            appendParam(currentUrl, "expand", buildSetParam(expand));
        }
    }

    private CloseableHttpClient buildHttpClient(String login, String password) {
        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        return HttpClients.custom()
                .setDefaultCredentialsProvider(credProvider)
                .build();
    }

    StringBuilder appendParam(final StringBuilder sb, String paramName, Object param) {
        if (sb.toString().contains("?")) {
            return sb.charAt(sb.length() - 1) != '?'
                    ? sb.append('&').append(paramName).append('=').append(param)
                    : sb.append(paramName).append('=').append(param);
        } else {
            return sb.append('?').append(paramName).append('=').append(param);
        }
    }

    private String buildSetParam(Set<String> params) {
        return params.stream()
                .collect(Collectors.joining(","));
    }

    protected boolean hasParameters(){
        return !expand.isEmpty();
    }

}
