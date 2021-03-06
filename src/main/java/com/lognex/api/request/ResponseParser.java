package com.lognex.api.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.lognex.api.converter.ConverterFactory;
import com.lognex.api.exception.ResponseException;
import com.lognex.api.model.base.Entity;
import com.lognex.api.model.entity.Employee;
import com.lognex.api.response.ApiError;
import com.lognex.api.response.ApiResponse;
import com.lognex.api.response.Context;
import com.lognex.api.util.Constants;
import com.lognex.api.util.MetaHrefUtils;
import com.lognex.api.util.Type;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ResponseParser {

    static ApiResponse parse(CloseableHttpResponse response, MSRequest msRequest) throws ResponseException {
        int statusCode = response.getStatusLine().getStatusCode();
        List<Header> headers = Arrays.asList(response.getAllHeaders());

        HttpEntity entity = response.getEntity();
        switch (entity.getContentType().getValue()) {
            case Constants.APPLICATION_OCTET_STREAM:
                return getResponse(statusCode, headers, entity);
            case Constants.APPLICATION_JSON_UTF8:
            default:
                return getResponse(msRequest, statusCode, headers, entity);
        }
    }

    private static ApiResponse getResponse(int statusCode, List<Header> headers, HttpEntity entity) {
        try {
            return new ApiResponse(statusCode, headers, getContent(entity));
        } catch (IOException e) {
            log.error("Error while reading content from response from server: ", e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] getContent(HttpEntity entity) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            entity.writeTo(stream);
            return stream.toByteArray();
        }
    }

    private static ApiResponse getResponse(MSRequest msRequest, int statusCode, List<Header> headers, HttpEntity entity) throws ResponseException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
            String body = reader.lines().reduce((a, b) -> a+b).orElse("");
            Type type = typeFromUrl(msRequest.getUrl());
            parseErrors(body);
            return new ApiResponse(body.getBytes(), statusCode,
                    parseEntities(body, type),
                    headers,
                    parseContext(body));
        } catch (IOException e) {
            log.error("Error while reading response from server: ", e);
            throw new RuntimeException(e);
        }
    }

    private static void parseErrors(String body) throws IOException, ResponseException {
        if (body != null && !body.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);
            if (json.has("errors")) {
                Set<ApiError> errors = new HashSet<>();
                ArrayNode errorsNode = (ArrayNode) json.get("errors");
                for (int i = 0; i < errorsNode.size(); ++i) {
                    JsonNode error = errorsNode.get(i);
                    ApiError parsed = mapper.readValue(error.toString(), ApiError.class);
                    errors.add(parsed);
                }
                throw new ResponseException(errors);
            }
        }
    }

    private static Context parseContext(String body) throws IOException {
        if (body != null && !body.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);
            if (json.has("context")) {
                Context context = new Context();
                JsonNode employee = json.get("context").get("employee");
                context.setEmployee(new Employee(MetaHrefUtils.getId(employee.get("meta").get("href").asText())));
                return context;
            }
        }
        return null;
    }


    private static List<Entity> parseEntities(String body, Type type) throws IOException {
        List<Entity> result = new ArrayList<>();
        if (body != null && !body.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);
            if (json.has("errors")){
                return result;
            }
            if (json.has("rows")){
                //map multiple
                ArrayNode rows = (ArrayNode) json.get("rows");
                for (int i = 0; i < rows.size(); ++i){
                    result.add(parseJsonObject(rows.get(i)));
                }

            } else if (json.isObject()) {
                // single value
                String entityType = json.has("meta") && json.get("meta").has("type")?
                        json.get("meta").get("type").asText() : type.getApiName();
                Entity entity = ConverterFactory.getConverter(Type.find(entityType).getModelClass()).convert(json.toString());
                result.add(entity);
            }
            return result;
        }
        return result;
    }

    private static Entity parseJsonObject(JsonNode json){
        String type = json.get("meta").get("type").asText();
        return ConverterFactory.getConverter(Type.find(type).getModelClass()).convert(json.toString());
    }

    private static Type typeFromUrl(String url){
        String[] split = url.split("/");
        if (split.length > 9 && split[8].equals("metadata")) {
            return Type.find(split[9]);
        } else if (split[6].equals("entity")){
            return Type.find(split[7]);
        }
        return null;
    }

}
