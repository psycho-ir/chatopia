package ninja.soroosh.chatopia.core.connectors.telegram;

import ninja.soroosh.chatopia.core.runner.*;
import ninja.soroosh.chatopia.core.runner.responses.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

@RestController
@ConditionalOnProperty(value = "chatopia.connector.telegram.enabled", havingValue = "true", matchIfMissing = true)
class TelegramController {
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private CommandRunner commandRunner;
    @Autowired
    private TelegramCommandBuilder telegramCommandBuilder;
    @Autowired
    private TelegramEventBuilder telegramEventBuilder;

    @Autowired
    private ResponseHandler responseHandler;

    @Value("${chatopia.connector.telegram.key}")
    private String key;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = restTemplateBuilder.build();
    }


    @PostMapping(path = "/connectors/telegram")
    public String webhook(@RequestBody TelegramRequest telegramRequest) throws IOException {
        var message = telegramRequest.getMessage() != null ? telegramRequest.getMessage() : telegramRequest.getCallbackQuery().getMessage();
        var commandText = telegramRequest.getCallbackQuery() == null ? message.getText() : telegramRequest.getCallbackQuery().getData();
        var isCallback = telegramRequest.getCallbackQuery() == null ? false : true;

        final long chatId = message.getChat().getId();
        final String sessionId = "telegram-" + chatId;
        final Context context = new Context(Optional.of(sessionId), "telegram");

        final Response commandResponse;

        if (commandText == null) {
            final Event event = telegramEventBuilder.build(telegramRequest.getMessage());
            commandResponse = commandRunner.runEvent(event, context);
        } else {
            final Command command;
            if (isCallback) {
                command = telegramCommandBuilder.buildFrom(telegramRequest.getCallbackQuery());
            } else {
                command = telegramCommandBuilder.buildFrom(telegramRequest.getMessage());
            }

            if (command.name() == null) {
                return "ok";
            }

            commandResponse = commandRunner.run(
                    command,
                    context
            );
        }

        Object response = responseHandler.handle(message, commandResponse);

        System.out.println(response);
        return "ok";
    }


}

