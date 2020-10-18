package ninja.soroosh.chatopia.core.example;

import ninja.soroosh.chatopia.core.annotation.ChatController;
import ninja.soroosh.chatopia.core.annotation.OnCommand;
import ninja.soroosh.chatopia.core.runner.Context;
import ninja.soroosh.chatopia.core.runner.Option;
import ninja.soroosh.chatopia.core.runner.Response;
import ninja.soroosh.chatopia.core.session.Session;

import java.util.List;
import java.util.Optional;

@ChatController
public class MessageEchoExample {
    @OnCommand(value = "echo", help = "This command echo")
    public Response onEchoCommand(String message, Context context) {
        return () -> "echo " + message;
    }

    @OnCommand(value = "hi", help = "start a chat")
    public Response onHiCommand(String message, Context context) {
        final Session session = context.getSession().get();
        final Optional<String> previousCount = session.get("count");
        final String currentCount = previousCount.map(strCount -> String.valueOf(Integer.parseInt(strCount) + 1))
                .orElse("1");
        session.set("count", currentCount);

        return () -> "Hi man, How are you? your session Id is "
                + context.getSessionId() +
                " and you are on channel: " + context.getChannel() +
                " and you call me " + currentCount + " times";
    }

    @OnCommand(value = "options", help = "A showcase to list options")
    public Response onOptionsCommand(String message, Context context) {
        return new Response() {
            @Override
            public String message() {
                return "Here are the options";
            }

            @Override
            public List<Option> options() {
                return List.of(new Option("option1"));
            }
        };
    }
}
