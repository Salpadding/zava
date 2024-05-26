package com.github.zava.core.net;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.NullEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Getter
public class HttpUtil {
    public static HttpUtil DEFAULT = new HttpUtil();

    public static RequestConfig.Builder configBuilder() {
        return RequestConfig.custom();
    }

    public static HttpEntity createEntity(String data) {
        return data == null ? NullEntity.INSTANCE : new StringEntity(data);
    }

    @SneakyThrows
    public static URI encodeQuery(String uri, Map<String, String> query) {
        if (query == null) return new URI(uri);
        NameValuePair[] pairs = new NameValuePair[query.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : query.entrySet()) {
            pairs[i] = new BasicHeader(entry.getKey(), entry.getValue());
            i++;
        }
        return new URIBuilder(uri).addParameters(Arrays.asList(pairs)).build();
    }

    private final CloseableHttpClient client;

    public HttpUtil() {
        this(HttpClientBuilder.create().build());
    }

    public HttpUtil(CloseableHttpClient client) {
        this.client = client;
    }

    @SneakyThrows
    public static byte[] respToBytes(ClassicHttpResponse resp) {
        return IOUtils.toByteArray(resp.getEntity().getContent());
    }

    @SneakyThrows
    public static String respToString(ClassicHttpResponse resp) {
        Charset ch = StandardCharsets.UTF_8;
        try {
            ch = ContentType.parse(resp.getEntity().getContentType()).getCharset();
        } catch (Exception ignored) {

        }
        return IOUtils.toString(resp.getEntity().getContent(), ch);
    }

    public String getForString(String uri) {
        return getForString(uri, Collections.emptyMap());
    }

    public String getForString(String uri, Map<String, String> query) {
        uri = encodeQuery(uri, query).toString();
        return request(
            "GET", uri,
            "", null,
            HttpUtil::respToString
        );
    }

    public String reqForJson(String method, String uri, String requestJson) {
        return request(
            method, uri,
            StringUtils.isBlank(requestJson) ? null : ContentType.APPLICATION_JSON.toString(),
            new StringEntity(requestJson), HttpUtil::respToString
        );
    }

    @SneakyThrows
    public <T> T request(
        String method, String uri, String contentType,
        HttpEntity entity, HttpClientResponseHandler<? extends T> responseHandler
    ) {
        HttpUriRequestBase req = new HttpUriRequestBase(method, new URI(uri));
        if (!StringUtils.isBlank(contentType))
            req.setHeader("Content-Type", contentType);
        req.setEntity(entity == null ? NullEntity.INSTANCE : entity);
        return this.client.execute(req, null, responseHandler);
    }
}
