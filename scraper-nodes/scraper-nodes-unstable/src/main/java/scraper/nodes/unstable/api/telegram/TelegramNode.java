package scraper.nodes.unstable.api.telegram;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.NodeLogLevel;
import scraper.core.Template;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Message something to a Telegram account
 */
@NodePlugin("1.0.0")
public final class TelegramNode extends AbstractFunctionalNode {
    /** Bot token */
    @FlowKey(mandatory = true) @Argument
    private String botToken;

    /** Message recipient. User or group chat id */
    @FlowKey(mandatory = true)
    private Template<List<Integer>> recipients = new Template<>(){};

    /** Telegram bot api link */
    @FlowKey(defaultValue = "\"https://api.telegram.org/bot\"") @Argument
    private String api;

    /** Message */
    @FlowKey(defaultValue = "\"{message}\"")
    private final Template<String> message = new Template<>(){};

    // API as of 01.2020
    private final static String jsonRequest = "{\"chat_id\":\"{CHAT_ID_INTERNAL}\",\"text\":\"{MESSAGE_INTERNAL}\", \"method\":\"/sendmessage\"}}";
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    @Override
    public void modify(@NotNull final FlowMap o) {
        String message = this.message.eval(o);
        recipients.eval(o).forEach(id -> trySend(message,id));
    }

    private void trySend(@NotNull final String msg, @NotNull final Integer id) {
        try {
            /// Create Http POST method and set correct headers
            String url = api + botToken + "/sendmessage";
            String jsonString = jsonRequest
                    .replace("{CHAT_ID_INTERNAL}",id.toString())
                    .replace("{MESSAGE_INTERNAL}", msg);

            HttpRequest request = HttpRequest
                    .newBuilder(URI.create(url))
                    .header("User-Agent", "telegram-bot")
                    .header("Content-type", "application/json")
                    .header("charset", "UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log(NodeLogLevel.ERROR, "Could not send message to {}: {}", id, e);
        }
    }
}
