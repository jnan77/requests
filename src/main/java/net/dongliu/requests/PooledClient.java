package net.dongliu.requests;

import net.dongliu.requests.exception.RequestException;
import net.dongliu.requests.struct.Host;
import net.dongliu.requests.struct.Pair;
import net.dongliu.requests.struct.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Pooled http client use connection pool, for reusing http connection across http requests.
 * This class is thread-safe, can service connection requests from multiple execution threads.
 *
 * @author Dong Liu dongliu@live.cn
 */
public class PooledClient implements Closeable {

    // the wrapped http client
    private final CloseableHttpClient client;

    public PooledClient(CloseableHttpClient client, Proxy proxy) {
        this.client = client;
        this.proxy = proxy;
    }

    /**
     * create new ConnectionPool Builder.
     */
    public static PooledClientBuilder custom() {
        return new PooledClientBuilder();
    }

    private final Proxy proxy;

    @Override
    public void close() throws IOException {
        client.close();
    }

    Proxy getProxy() {
        return proxy;
    }

    /**
     * get method
     */
    public BaseRequestBuilder get(String url) throws RequestException {
        return Requests.get(url).executedBy(this);
    }

    /**
     * head method
     */
    public BaseRequestBuilder head(String url) throws RequestException {
        return Requests.head(url).executedBy(this);
    }

    /**
     * get url, and return content
     */
    public PostRequestBuilder post(String url) throws RequestException {
        return Requests.post(url).executedBy(this);
    }

    /**
     * put method
     */
    public BodyRequestBuilder put(String url) throws RequestException {
        return Requests.put(url).executedBy(this);
    }

    /**
     * delete method
     */
    public BaseRequestBuilder delete(String url) throws RequestException {
        return Requests.delete(url).executedBy(this);
    }

    /**
     * options method
     */
    public BaseRequestBuilder options(String url) throws RequestException {
        return Requests.options(url).executedBy(this);
    }

    /**
     * patch method
     */
    public BodyRequestBuilder patch(String url) throws RequestException {
        return Requests.patch(url).executedBy(this);
    }

    /**
     * create a session. session can do request as Requests do, and keep cookies to maintain a http session
     */
    public Session session() {
        return new Session(this);
    }

    CloseableHttpClient getHttpClient() {
        return client;
    }

    /**
     * trace method
     */
    public BaseRequestBuilder trace(String url) throws RequestException {
        return Requests.trace(url).executedBy(this);
    }

    public static class PooledClientBuilder {
        // how long http connection keep, in milliseconds. default -1, get from server response
        private long timeToLive = -1;
        // the max total http connection count
        private int maxTotal = 20;
        // the max connection count for each host
        private int maxPerRoute = 2;
        // set max count for specified host
        private List<Pair<Host, Integer>> perRouteCount;

        private Proxy proxy;

        // settings for client level, can not set/override in request level
        // if verify http certificate
        private boolean verify = true;
        // if enable gzip response
        private boolean gzip = true;
        private boolean allowRedirects = true;
        private boolean allowPostRedirects = false;
        private String userAgent = Utils.defaultUserAgent;

        PooledClientBuilder() {
        }

        public PooledClient build() {
            Registry<ConnectionSocketFactory> r = Utils.getConnectionSocketFactoryRegistry(proxy,
                    verify);
            PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(r,
                    null, null, null, timeToLive, TimeUnit.MILLISECONDS);

            manager.setMaxTotal(maxTotal);
            manager.setDefaultMaxPerRoute(maxPerRoute);
            if (perRouteCount != null) {
                for (Pair<Host, Integer> pair : perRouteCount) {
                    Host host = pair.getName();
                    manager.setMaxPerRoute(
                            new HttpRoute(new HttpHost(host.getDomain(), host.getPort())),
                            pair.getValue());
                }
            }

            HttpClientBuilder clientBuilder = HttpClients.custom().setUserAgent(userAgent);

            clientBuilder.setConnectionManager(manager);

            // disable gzip
            if (!gzip) {
                clientBuilder.disableContentCompression();
            }

            if (!allowRedirects) {
                clientBuilder.disableRedirectHandling();
            }

            if (allowPostRedirects) {
                clientBuilder.setRedirectStrategy(new AllRedirectStrategy());
            }

            return new PooledClient(clientBuilder.build(), proxy);
        }


        /**
         * how long http connection keep, in milliseconds. default -1, get from server response
         */
        public PooledClientBuilder timeToLive(long timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        /**
         * the max total http connection count. default 20
         */
        public PooledClientBuilder maxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        /**
         * set default max connection count for each host, default 2
         */
        public PooledClientBuilder maxPerRoute(int maxPerRoute) {
            this.maxPerRoute = maxPerRoute;
            return this;
        }

        /**
         * set specified max connection count for the host, default 2
         */
        public PooledClientBuilder maxPerRoute(Host host, int maxPerRoute) {
            ensurePerRouteCount();
            this.perRouteCount.add(new Pair<>(host, maxPerRoute));
            return this;
        }

        /**
         * set userAgent
         */
        public PooledClientBuilder userAgent(String userAgent) {
            Objects.requireNonNull(userAgent);
            this.userAgent = userAgent;
            return this;
        }

        /**
         * if verify http certificate, default true
         */
        public PooledClientBuilder verify(boolean verify) {
            this.verify = verify;
            return this;
        }

        /**
         * If follow get/head redirect, default true.
         * This method not set following redirect for post/put/delete method, use {@code allowPostRedirects} if you want this
         */
        public PooledClientBuilder allowRedirects(boolean allowRedirects) {
            this.allowRedirects = allowRedirects;
            return this;
        }

        /**
         * If follow POST/PUT/DELETE redirect, default false. This method work for post method.
         */
        public PooledClientBuilder allowPostRedirects(boolean allowPostRedirects) {
            this.allowPostRedirects = allowPostRedirects;
            return this;
        }

        /**
         * if send gzip requests. default true
         */
        public PooledClientBuilder gzip(boolean gzip) {
            this.gzip = gzip;
            return this;
        }

        private void ensurePerRouteCount() {
            if (this.perRouteCount == null) {
                this.perRouteCount = new ArrayList<>();
            }
        }

        private PooledClientBuilder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }
    }
}
