package net.dongliu.requests;

import net.dongliu.requests.struct.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Http request
 *
 * @author Dong Liu dongliu@live.cn
 */
public class Request {

    private final Method method;
    private final URI url;
    private final String userAgent;
    private final List<Header> headers;
    private final List<Cookie> cookies;
    private final List<Parameter> parameters;
    private HttpBody httpBody;

    private final AuthInfo authInfo;
    // if enable gzip response
    private final boolean gzip;
    // if verify certificate of https site
    private final boolean verify;
    private final boolean allowRedirects;
    private final boolean allowPostRedirects;

    private final int connectTimeout;
    private final int socketTimeout;

    private final Proxy proxy;
    private final Charset charset;

    Request(Method method, URI url, List<Parameter> parameters, String userAgent, List<Header> headers,
            HttpBody httpBody,
            Charset charset, AuthInfo authInfo, boolean gzip, boolean verify, List<Cookie> cookies,
            boolean allowRedirects, boolean allowPostRedirects, int connectTimeout, int socketTimeout, Proxy proxy) {
        this.method = method;
        this.url = url;
        this.parameters = parameters;
        this.userAgent = userAgent;
        this.httpBody = httpBody;
        this.charset = charset;
        this.headers = headers;
        this.authInfo = authInfo;
        this.gzip = gzip;
        this.verify = verify;
        this.cookies = cookies;
        this.allowRedirects = allowRedirects;
        this.allowPostRedirects = allowPostRedirects;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.proxy = proxy;
    }

    public Method getMethod() {
        return method;
    }

    public URI getUrl() {
        return url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public AuthInfo getAuthInfo() {
        return authInfo;
    }

    public boolean isGzip() {
        return gzip;
    }

    public boolean isVerify() {
        return verify;
    }

    public boolean isAllowRedirects() {
        return allowRedirects;
    }

    public boolean isAllowPostRedirects() {
        return allowPostRedirects;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public HttpBody getHttpBody() {
        return httpBody;
    }

    public Charset getCharset() {
        return charset;
    }
}
