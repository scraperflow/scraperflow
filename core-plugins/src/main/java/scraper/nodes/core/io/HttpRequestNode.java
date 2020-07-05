package scraper.nodes.core.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.*;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.service.HttpService;
import scraper.api.service.HttpService.RequestType;
import scraper.api.service.proxy.ProxyMode;
import scraper.api.service.proxy.ReservationToken;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;
import scraper.api.template.T;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.MILLIS;
import static scraper.api.node.container.NodeLogLevel.*;

/**
 * Provides html functions (see RequestType):
 * <ul>
 *     <li>GET</li>
 *     <li>Image to Base64 GET</li>
 *     <li>POST (json body)</li>
 *     <li>POST (form)</li>
 * </ul>
 * Example:
 * <pre>
 * type: HttpRequestNode
 * proxyFile: "/tmp/proxies.csv"
 * proxyGroup: g1
 * proxyMode: PROXY
 * banResponses:
 *   - 403
 * exceptionContaining:
 *   - "Attention Required! | Cloudflare"
 *   - "522: Connection timed out"
 *   - "will be temporarily banned"
 * cache: "/tmp/cache/"
 * timeout: 50000
 * holdOnReservation: 30000
 * holdOnForward: 4000
 * </pre>
 *
 * If multiple HttpRequestNodes are used with a similar configuration, globalNodeConfigurations can be used.
 */
@NodePlugin("2.0.1")
@Io
public final class HttpRequestNode implements Node {

    private final ObjectMapper mapper = new ObjectMapper();

    // --------------
    // REQUEST
    // --------------
    /** Target URL. If URL starts with // without a schema, <var>defaultSchema</var> will be used as the schema */
    @FlowKey(defaultValue = "\"{url}\"")
    private final T<String> url = new T<>(){};
    /** Default schema if url starts with // */
    @FlowKey(defaultValue = "\"https:\"")
    private String defaultSchema;
    /** Response location if successful */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};
    /** Type of request: GET, POST, DELETE, PUT */
    @FlowKey(defaultValue = "\"GET\"")
    private RequestType requestType;
    /** Inserts name value pair request headers */
    @FlowKey(defaultValue = "{}")
    private final T<Map<String, String>> requestHeaders = new T<>(){};
    /** User agent of the request */
    @FlowKey(defaultValue = "\"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0\"")
    @Argument
    private String userAgent;
    /** Timeout for every HTTP request */
    @FlowKey(defaultValue = "5000") @Argument
    private Integer timeout;
    /** Use proxy or not. Possible values:
     * <code> LOCAL, PROXY, BOTH_PREFER_PROXY, BOTH_PREFER_LOCAL</code> */
    @FlowKey(defaultValue = "\"LOCAL\"")
    private ProxyMode proxyMode;
    /** Shared proxy group */
    @FlowKey(defaultValue = "\"local\"")
    private String proxyGroup;

    // --------------
    // RESPONSE HANDLING
    // --------------
    /** Expected response type, STRING_BODY, or FILE */
    @FlowKey(defaultValue = "\"STRING_BODY\"")
    private ResponseType expectedResponse;
    /** How long the used proxy or local connection is made unusable by other nodes. This can used to reduce the requests for a single IP. */
    @FlowKey(defaultValue = "1000") @Argument
    private Integer holdOnReservation;
    /** How long to wait after a request has completed but after the token has been freed. This can be used to not overload the target server. */
    @FlowKey(defaultValue = "1000") @Argument
    private Integer holdOnForward;
    /** Checks the response body of a string response for bad phrases, throwing an exception if one is found */
    @FlowKey(defaultValue = "[]")
    private final T<List<String>> exceptionContaining = new T<>(){};

    // --------------
    // COOKIES
    // --------------
    /** Cookie Ts to use for HTTP request */
    @FlowKey(defaultValue = "{}")
    private final T<Map<String, String>> cookies = new T<>(){};
//    /** Cookie domain. Mandatory, if cookies are used! */
//    @FlowKey @Argument
//    private String cookieDomain;

    // --------------
    // CACHING
    // --------------
    /** Caching of results to specified folder on a best-effort basis ignoring IO errors with a TTL of cacheTTLms.
     * Needs to end with a folder separator */
    @FlowKey
    @Argument
    @EnsureFile
    private String cache;
    /** TTL for cache files in Long format in ms. Default is forever. Null means caching forever. */
    @FlowKey
    @Argument
    private Long cacheTTLms;
    /** Path to a proxy file */
    @FlowKey
    @Argument
    private String proxyFile;

    // --------------
    // FILE DOWNLOAD
    // --------------
    /** Save path in case of file download */
    @FlowKey
    private final T<String> path = new T<>(){};

    // --------------
    // POST SPECIFIC
    // --------------
    /** Payload of a POST request */
    @FlowKey
    private final T<Object> payload = new T<>(){};

    @Override
    public void init(@NotNull NodeContainer<? extends Node> n, @NotNull final ScrapeInstance job) throws ValidationException {
        if (proxyFile != null) {
            try {
                n.getJobInstance().getProxyReservation().addProxies(proxyFile, proxyGroup);
            } catch (Exception e) {
                n.log(ERROR,"IO proxy read error: {}", proxyFile);
                throw new ValidationException("Could not read file at "+proxyFile+". "+e);
            }
        }
    }

    @Override @NotNull
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) throws NodeException {
        // evaluate Ts
        String url = o.eval(this.url);
        List<String> exceptionContaining = o.evalIdentity(this.exceptionContaining);

        // check if file already downloaded
        if(checkFileDownloaded(n, o)) {
            n.log(TRACE, "File already downloaded: {}", url);
            return o;
        }

        // check if response is cached
        if(cached(n, o, url, exceptionContaining)) {
            n.log(TRACE, "Request cached: {}", url);
            return o;
        }

        ReservationToken token;
        try {
            token = n.getJobInstance().getProxyReservation().reserveToken(proxyGroup, proxyMode,0, holdOnReservation);
        } catch (InterruptedException | TimeoutException e) {
            n.log(ERROR, "Interrupted while waiting for proxy");
            throw new NodeException(e, "Interrupted while waiting for proxy");
        }

        try {
            // build request
            HttpRequest request = buildRequest(n, o, url);
            @SuppressWarnings({"rawtypes"}) // choose bodyhandler by config
            HttpResponse.BodyHandler handler = null;

            switch (expectedResponse) {
                case STRING_BODY:
                    handler = HttpResponse.BodyHandlers.ofString();
                    break;

                case FILE:
                    Path download = Paths.get(o.eval(path));
                    handler = HttpResponse.BodyHandlers.ofFile(download);
                    break;
            }

            HttpService service = n.getJobInstance().getHttpService();

            // TODO use generics
            @SuppressWarnings({"rawtypes", "unchecked"}) // choose bodyhandler by config
            HttpResponse response = service.send(request, handler, token);

            Object body = response.body();

            validateBody(body, exceptionContaining);

            // cache if content not empty
            if(cache != null && body instanceof String)
                cacheResponse(n, cache, url, (String) body);

            if(expectedResponse.equals(ResponseType.STRING_BODY)) o.output(put, (String) body);

        } catch (InterruptedException e) {
            n.log(WARN, "Interrupted while waiting for token: {}", url);
            throw new NodeException(e, "Interrupted while waiting for token");
        } catch (IOException e) {
            token.bad();
            n.log(INFO, "IOException for request {}: {}", url, e.getMessage());
            throw new NodeException(e, "IOException");
        } catch (ExecutionException e) {
            n.log(WARN, "Execution exception: {} | {}", e.getMessage(), url);
            token.bad();
            throw new NodeException(e, "Bad Execution");
        } catch (TimeoutException e) {
            token.bad();
            n.log(INFO, "Token timeout bad: {} | {}", token, url);
            throw new NodeException(e, "Timeout");
        } finally {
            token.close();
        }

        n.log(INFO,"[✔] {}", url);

        try {
            Thread.sleep(holdOnForward);
        } catch (InterruptedException e) {
            n.log(ERROR, "Hold on forward interrupted");
            throw new NodeException(e, "Hold on forward interrupted");
        }

        return o;
    }

    private void validateBody(Object body, List<String> exceptionContaining) throws IOException {
        if(body == null) throw new IOException("Null body");
        if(body instanceof String) {
            String content = (String) body;
            for (String badPhrase : exceptionContaining) {
                if(content.toLowerCase().contains(badPhrase.toLowerCase())) {
                    throw new IOException("Request contains bad phrase: "+ badPhrase);
                }
            }
        }
    }

    private HttpRequest buildRequest(NodeContainer<? extends Node> n, FlowMap o, String url) throws NodeException {
        try {
            // set url
            if(url.startsWith("//")) url = defaultSchema+url;
            URI uri = new URI(url);
            HttpRequest.Builder request = HttpRequest.newBuilder(uri);

            // set type and payload
            String payload;
            switch (requestType) {
                case GET:
                    request.GET();
                    break;
                case POST:
                    payload = mapper.writeValueAsString(o.eval(this.payload));
                    request.POST(HttpRequest.BodyPublishers.ofString(payload));
                    break;
                case DELETE:
                    request.DELETE();
                    break;
                case PUT:
                    payload = mapper.writeValueAsString(o.eval(this.payload));
                    request.PUT(HttpRequest.BodyPublishers.ofString(payload));
                    break;
                default:
                    n.log(ERROR, "Using legacy request type for new HTTP node: {}", requestType);
                    throw new RuntimeException();
            }

            // set headers
            o.eval(requestHeaders).forEach(request::header);

            Map<String, String> cookies = o.eval(this.cookies);
            String cookieString = getCookieString(cookies);
            if(!cookieString.isEmpty()) request.header("Cookie", cookieString);

            // set timeout
            request.timeout(Duration.of(timeout, MILLIS));

            request.header("User-Agent", userAgent);

            return request.build();
        } catch (Exception e) {
            n.log(ERROR, "Could not build http request, {}: {}", e, url);
            throw new NodeException(e, "Could not build http request "+url);
        }
    }

    private String getCookieString(Map<String, String> cookies) {
        StringBuilder cookie = new StringBuilder();

        for (String key : cookies.keySet()) {
            cookie.append(key).append("=").append(cookies.get(key)).append("; ");
        }

        if(cookies.size() > 0) {
            cookie = new StringBuilder(cookie.substring(0, cookie.length() - 2));
        }

        return cookie.toString();
    }

    private void cacheResponse(NodeContainer<? extends Node> n, String cache, String url, String content) {
        String file = urlToCachedFilename(url);
        try {
            n.getJobInstance().getFileService().ensureFile(cache+file);
            n.getJobInstance().getFileService().replaceFile(cache+file, content);
        } catch (IOException e) {
            n.log(ERROR,"Could not cache content: {}", e.getMessage());
        }
    }

    // =================
    // Private functions
    // ==================

    enum ResponseType {
        STRING_BODY, FILE
    }

    /** Checks if file is already downloaded at expected location (for RequestType.FILE) */
    private boolean checkFileDownloaded(NodeContainer<? extends Node> n, FlowMap o) {
        if(expectedResponse.equals(ResponseType.FILE)) {
            String path = o.eval(this.path);
            File f = new File(path);
            if(f.exists() && f.length() == 0) {
                n.log(INFO,"Found empty downloaded file, deleting file '{}'", path);
                if(!f.delete()) n.log(WARN,"Could not delete empty downloaded file: {}", f.getPath());
                return false;
            }

            return f.exists();
        }

        return false;
    }

    /** If cached try to get cached value first */
    private boolean cached(NodeContainer<? extends Node> n, FlowMap o, String url, List<String> exceptionContaining) {
        if (cache != null) {
            String cachedContent = getCached(n, cache, url, exceptionContaining);
            if (cachedContent != null) {
                n.log(DEBUG,"[\uD83D\uDCBE] {}", url);

                o.output(put, cachedContent);
                return true;
            }
        }

        return false;
    }

    private String getCached(NodeContainer<? extends Node> n, String cache, String url, List<String> exceptionContaining) {
        String filename = urlToCachedFilename(url);
        File f = Paths.get(cache, filename).toFile();
        if(!f.exists()) return null;

        // get cache TTL, delete if exceeded
        if(cacheTTLms != null) {
            long lstModifiedMs = f.lastModified();
            long currentMs = System.currentTimeMillis();
            long diff = currentMs - lstModifiedMs;
            long cacheMs = cacheTTLms;
            if ((cacheMs - diff) <= 0) {
                // TTL exceeded
                boolean delete = f.delete();
                if(!delete) n.log(WARN,"Could not delete cached file: {}", url);
                return null;
            }
        }

        // try to read
        try {
            String cached = StringUtil.readBody(f);
            if(cached.isEmpty()) {
                n.log(INFO,"Found empty cache file, deleting cache for '{}'", url);
                if(!f.delete()) n.log(WARN,"Could not delete empty cache file: {}", f.getPath());
                return null;
            }

            try {
                validateBody(cached, exceptionContaining);
            } catch (Exception e) {
                n.log(WARN,"Invalidating cache file with invalid content: {}", e.getMessage());
                if(!f.delete()) n.log(WARN,"Could not delete invalid cache file: {}", f.getPath());
                return null;
            }
            return cached;
        } catch (IOException e) {
            n.log(WARN,"Could not read cached file content: {}", e);
            return null;
        }
    }

    private String urlToCachedFilename(String url) {
        return Base64.getUrlEncoder().encodeToString(url.getBytes());
    }
}
