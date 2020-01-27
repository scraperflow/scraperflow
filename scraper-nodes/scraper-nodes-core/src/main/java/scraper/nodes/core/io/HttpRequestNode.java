package scraper.nodes.core.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.service.HttpService;
import scraper.api.service.HttpService.RequestType;
import scraper.api.service.proxy.ProxyMode;
import scraper.api.service.proxy.ReservationToken;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractNode;
import scraper.core.Template;
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
import static scraper.core.NodeLogLevel.*;

/**
 * Provides html functions (see RequestType):
 * <ul>
 *     <li>GET</li>
 *     <li>Image to Base64 GET</li>
 *     <li>POST (json body)</li>
 *     <li>POST (form)</li>
 * </ul>
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.1")
public final class HttpRequestNode extends AbstractNode {

    private final ObjectMapper mapper = new ObjectMapper();

    // TODO make userAgent modifiable
    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";

    // --------------
    // REQUEST
    // --------------
    /** Target URL */
    @FlowKey(defaultValue = "\"{url}\"")
    private final Template<String> url = new Template<>(){};
    /** Default schema if url starts with // */
    @FlowKey(defaultValue = "\"https:\"")
    private String defaultSchema;
    /** Response location if successful */
    @FlowKey(defaultValue = "\"response\"")
    private String put;
    /** Type of request */
    @FlowKey(defaultValue = "\"GET\"")
    private RequestType requestType;
    /** Inserts name value pair request headers */
    @FlowKey(defaultValue = "{}")
    private final Template<Map<String, String>> requestHeaders = new Template<>(){};
    /** Timeout for every HTTP request */
    @FlowKey(defaultValue = "5000") @Argument
    private Integer timeout;
    /** Use proxy or not */
    @FlowKey(defaultValue = "\"LOCAL\"")
    private ProxyMode proxyMode;
    /** Shared proxy group */
    @FlowKey(defaultValue = "\"local\"")
    private String proxyGroup;

    // --------------
    // RESPONSE HANDLING
    // --------------
    /** Expected response type, STRING_BODY, JSON, or FILE */
    @FlowKey(defaultValue = "\"STRING_BODY\"")
    private ResponseType expectedResponse;
    /** How long the used proxy or local connection is made unusable by other nodes */
    @FlowKey(defaultValue = "1000") @Argument
    private Integer holdOnReservation;
    /** How long to wait after a request has completed */
    @FlowKey(defaultValue = "1000") @Argument
    private Integer holdOnForward;
    /** Checks the response body of a string response for bad phrases, throwing an exception if one is found */
    @FlowKey(defaultValue = "[]")
    private List<String> exceptionContaining;

    // --------------
    // COOKIES
    // --------------
    /** Cookie templates to use for HTTP request */
    @FlowKey(defaultValue = "{}")
    private final Template<Map<String, String>> cookies = new Template<>(){};
//    /** Cookie domain. Mandatory, if cookies are used! */
//    @FlowKey @Argument
//    private String cookieDomain;

    // --------------
    // CACHING
    // --------------
    /** Caching of results to specified folder on a best-effort basis ignoring IO errors with a TTL of cacheTTLms */
    @FlowKey @Argument @EnsureFile
    private String cache;
    /** TTL for cache files in Long format in ms. Default is forever. Null means caching forever. */
    @FlowKey @Argument
    private Long cacheTTLms;
    @FlowKey @Argument
    private String proxyFile;

    // --------------
    // FILE DOWNLOAD
    // --------------
    /** Save path in case of file download */
    @FlowKey
    private Template<String> path = new Template<>(){};

    // --------------
    // POST SPECIFIC
    // --------------
    /** Payload of a POST request */
    @FlowKey
    private Template<Object> payload = new Template<>(){};

    @Override
    public void init(@NotNull final ScrapeInstance job) throws ValidationException {
        super.init(job);

        if (proxyFile != null) {
            try {
                getJobPojo().getProxyReservation().addProxies(proxyFile, proxyGroup);
            } catch (Exception e) {
                log(ERROR,"IO proxy read error: {}", proxyFile);
                throw new ValidationException("Could not read file at "+proxyFile+". "+e);
            }
        }
    }

    @Override @NotNull
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        // evaluate templates
        String url = this.url.eval(o);

        // check if file already downloaded
        if(checkFileDownloaded(o)) {
            log(TRACE, "File already downloaded: {}", url);
            finish(o);
            return forward(o);
        }

        // check if response is cached
        if(cached(o, url)) {
            log(TRACE, "Request cached: {}", url);
            finish(o);
            return forward(o);
        }

        ReservationToken token;
        try {
            token = getJobPojo().getProxyReservation().reserveToken(proxyGroup, proxyMode,0, holdOnReservation);
        } catch (InterruptedException | TimeoutException e) {
            log(ERROR, "Interrupted while waiting for proxy");
            throw new NodeException(e, "Interrupted while waiting for proxy");
        }

        try {
            // build request
            HttpRequest request = buildRequest(o, url);
            HttpResponse.BodyHandler handler = null;

            switch (expectedResponse) {
                case JSON: // continue
                case STRING_BODY:
                    handler = HttpResponse.BodyHandlers.ofString();
                    break;

                case FILE:
                    Path download = Paths.get(path.eval(o));
                    handler = HttpResponse.BodyHandlers.ofFile(download);
                    break;
            }

            HttpService service = getJobPojo().getHttpService();

            HttpResponse response = service.send(request, handler, token);

            Object body = response.body();

            validateBody(body);

            // cache if content not empty
            if(cache != null && body instanceof String)
                cacheResponse(cache, url, (String) body);

            // convert to JSON if specified
            if(expectedResponse.equals(ResponseType.JSON) && (body instanceof String))
                body = mapper.readValue((String) body, Object.class);

            o.put(put, body);

        } catch (InterruptedException e) {
            log(WARN, "Interrupted while waiting for token: {}", url);
            throw new NodeException(e, "Interrupted while waiting for token");
        } catch (IOException e) {
            token.bad();
            log(INFO, "IOException for request {}: {}", url, e.getMessage());
            throw new NodeException(e, "IOException");
        } catch (ExecutionException e) {
            log(WARN, "Execution exception: {} | {}", e.getMessage(), url);
            token.bad();
            throw new NodeException(e, "Bad Execution");
        } catch (TimeoutException e) {
            token.bad();
            log(INFO, "Token timeout bad: {} | {}", token, url);
            throw new NodeException(e, "Timeout");
        } finally {
            token.close();
        }

        log(INFO,"[✔] {}", url);

        try {
            Thread.sleep(holdOnForward);
        } catch (InterruptedException e) {
            log(ERROR, "Hold on forward interrupted");
            throw new NodeException(e, "Hold on forward interrupted");
        }

        return forward(o);
    }

    private void validateBody(Object body) throws IOException {
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

    private HttpRequest buildRequest(FlowMap o, String url) throws NodeException {
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
                    payload = mapper.writeValueAsString(this.payload.eval(o));
                    request.POST(HttpRequest.BodyPublishers.ofString(payload));
                    break;
                case DELETE:
//                    payload = mapper.writeValueAsString(this.payload.eval(o));
                    request.DELETE();
                    break;
                case PUT:
                    payload = mapper.writeValueAsString(this.payload.eval(o));
                    request.PUT(HttpRequest.BodyPublishers.ofString(payload));
                    break;
                default:
                    log(ERROR, "Using legacy request type for new HTTP node: {}", requestType);
                    throw new RuntimeException();
            }

            // set headers
            requestHeaders.eval(o).forEach(request::header);

            Map<String, String> cookies = this.cookies.eval(o);
            String cookieString = getCookieString(cookies);
            if(!cookieString.isEmpty()) request.header("Cookie", cookieString);

            // set timeout
            request.timeout(Duration.of(timeout, MILLIS));

            request.header("User-Agent", userAgent);

            return request.build();
        } catch (Exception e) {
            log(ERROR, "Could not build http request, {}: {}", e, url);
            throw new NodeException(e, "Could not build http request "+url);
        }
    }

    private String getCookieString(Map<String, String> cookies) {
        String cookie = "";

        for (String key : cookies.keySet()) {
            //noinspection StringConcatenationInLoop not that important to use StringBuilder here
            cookie += key+"="+cookies.get(key)+"; ";
        }

        if(cookies.size() > 0) {
            cookie = cookie.substring(0, cookie.length()-2);
        }

        return cookie;
    }

    private void cacheResponse(String cache, String url, String content) {
        String file = urlToCachedFilename(url);
        try {
            getJobPojo().getFileService().ensureFile(cache+file);
            getJobPojo().getFileService().replaceFile(cache+file, content);
        } catch (IOException e) {
            log(ERROR,"Could not cache content: {}", e.getMessage());
        }
    }

    // =================
    // Private functions
    // ==================

    enum ResponseType {
        STRING_BODY, FILE, JSON
    }

    /** Checks if file is already downloaded at expected location (for RequestType.FILE) */
    private boolean checkFileDownloaded(FlowMap o) {
        if(expectedResponse.equals(ResponseType.FILE)) {
            String path = this.path.eval(o);
            File f = new File(path);
            if(f.exists() && f.length() == 0) {
                log(INFO,"Found empty downloaded file, deleting file '{}'", path);
                if(!f.delete()) log(WARN,"Could not delete empty downloaded file: {}", f.getPath());
                return false;
            }

            return f.exists();
        }

        return false;
    }

    /** If cached try to get cached value first */
    private boolean cached(FlowMap o, String url) {
        if (cache != null) {
            String cachedContent = getCached(cache, url);
            if (cachedContent != null) {
                log(DEBUG,"[\uD83D\uDCBE] {}", url);

                o.put(put, cachedContent);
                return true;
            }
        }

        return false;
    }

    private String getCached(String cache, String url) {
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
                if(!delete) log(WARN,"Could not delete cached file: {}", url);
                return null;
            }
        }

        // try to read
        try {
            String cached = StringUtil.readBody(f);
            if(cached.isEmpty()) {
                log(INFO,"Found empty cache file, deleting cache for '{}'", url);
                if(!f.delete()) log(WARN,"Could not delete empty cache file: {}", f.getPath());
                return null;
            }

            try {
                validateBody(cached);
            } catch (Exception e) {
                log(WARN,"Invalidating cache file with invalid content: {}", e.getMessage());
                if(!f.delete()) log(WARN,"Could not delete invalid cache file: {}", f.getPath());
                return null;
            }
            return cached;
        } catch (IOException e) {
            log(WARN,"Could not read cached file content: {}", e);
            return null;
        }
    }

    private String urlToCachedFilename(String url) {
        return Base64.getUrlEncoder().encodeToString(url.getBytes());
    }
}
