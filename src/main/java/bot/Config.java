package bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class Config {

    @Bean
    public PomodoroBot pomodoroBot() {
        return new PomodoroBot();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(PomodoroBot pomodoroBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(pomodoroBot);

        new Thread(() -> {
            pomodoroBot.checkTimer();
        }).run();
        return telegramBotsApi;
    }
}