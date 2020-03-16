package scraper.nodes.unstable.api.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Message something to a Telegram account.
 * Failures to send will not result in exceptions, they are just logged.
 */
@NodePlugin("1.0.0")
@Io
public final class TelegramNode implements FunctionalNode {

    /** Message as a String */
    @FlowKey(defaultValue = "\"{message}\"")
    private final T<String> message = new T<>(){};

    /** Bot token */
    @FlowKey(defaultValue = "\"{bot-token}\"") @Argument
    private String botToken;

    /** Message recipients. User or group chat id */
    @FlowKey(mandatory = true)
    private T<List<String>> recipients = new T<>(){};

    /** Telegram bot api link */
    @FlowKey(defaultValue = "\"https://api.telegram.org/bot\"") @Argument
    private String api;

    private ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    private void trySend(String msg, String id) throws IOException, InterruptedException {
        /// Create Http POST method and set correct headers
        String url = api + botToken + "/sendmessage";
        String jsonString = mapper.writeValueAsString(new SendMessage("/sendmessage", id, msg));

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(url))
                .header("User-Agent", "telegram-bot")
                .header("Content-type", "application/json")
                .header("charset", "UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        client.send(request, HttpResponse.BodyHandlers.discarding());
    }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        for (String id : o.eval(recipients)) {
            try {
                trySend(o.eval(message), id);
            } catch (IOException | InterruptedException e) {
                n.log(NodeLogLevel.ERROR, "Could not send message to {}: {}", id, e);
            }
        }
    }


    @SuppressWarnings("unused") // used for serialization
    static class SendMessage implements Serializable {
        String chat_id;
        public String getChat_id() { return chat_id; }
        public void setChat_id(String chat_id) { this.chat_id = chat_id; }

        String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        String method;
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        SendMessage(String s, String valueOf, String msg) {
            method = s;
            text = msg;
            chat_id = valueOf;
        }
    }
}
