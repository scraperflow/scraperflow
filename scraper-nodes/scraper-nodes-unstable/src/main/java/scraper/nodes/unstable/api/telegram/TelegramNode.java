package scraper.nodes.unstable.api.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;
import scraper.core.Template;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Message something to a Telegram account
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class TelegramNode extends AbstractFunctionalNode {

    /** Message */
    @FlowKey(defaultValue = "\"{message}\"")
    private final Template<String> message = new Template<>(){};

    /** Bot token */
    @FlowKey(defaultValue = "\"{bot-token}\"") @Argument
    private String botToken;

    /** Message recipient. User or group chat id */
    @FlowKey(mandatory = true)
    private Template<List<Integer>> recipients = new Template<>(){};

    /** Telegram bot api link */
    @FlowKey(defaultValue = "\"https://api.telegram.org/bot\"") @Argument
    private String api;

    private ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    @Override
    public void modify(@NotNull final FlowMap o) {
        for (Integer id : recipients.eval(o)) {
            trySend(message.eval(o), id);
        }
    }

    private void trySend(String msg, Integer id) {
        try {
            /// Create Http POST method and set correct headers
            String url = api + botToken + "/sendmessage";
            String jsonString = mapper.writeValueAsString(new SendMessage("/sendmessage", String.valueOf(id), msg));

            HttpRequest request = HttpRequest
                    .newBuilder(URI.create(url))
                    .header("User-Agent", "telegram-bot")
                    .header("Content-type", "application/json")
                    .header("charset", "UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.discarding());

        } catch (IOException | InterruptedException e) {
            log(NodeLogLevel.ERROR, "Could not send message to {}: {}", id, e);
        }
    }


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

        public SendMessage(String s, String valueOf, String msg) {
            method = s;
            text = msg;
            chat_id = valueOf;
        }
    }
}
