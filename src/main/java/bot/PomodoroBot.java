package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

public class PomodoroBot extends TelegramLongPollingBot {

    private final ConcurrentHashMap<UserTimer, Long> userTimerRepository = new ConcurrentHashMap<>();

    private record UserTimer(Instant userTimer, TimerType timerType) {
    }

    private int count = 1;

    @Override
    public String getBotUsername() {
        return "Pomodoro";
    }

    @Override
    public String getBotToken() {
        return "5555488673:AAE8wDSFAoaRyeIUpxmJwU_eOkvKhX88LP0";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String sentMsg = update.getMessage().getText();
            String userName = update.getMessage().getChat().getFirstName();
            Long chatId = update.getMessage().getChatId();
            String greetingMsg = "Привет, " + userName + "! Я бот Помодоро, помогающий настроить рабочий процесс!" +
                    "\nВведи через пробел, сколько ты хочешь работать и сколько отдыхать в секундах." +
                    "\nПо желанию введи количество итераций (повторений)";

            if (sentMsg.equals("/start")) {
                sendMsg(chatId, greetingMsg);
                System.out.println("Пользователь использовал бота, имя: " + userName);
                return;
            }

            var args = update.getMessage().getText().split(" ");

            sendMsg(chatId, "Сейчас: " + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute());
            sendMsg(chatId, "Ты хочешь работать: " + Long.parseLong(args[0]) + "sec \uD83D\uDE80");
            sendMsg(chatId, "Ты хочешь отдыхать: " + Long.parseLong(args[1]) + "sec \uD83D\uDE0E");

            if (args.length > 2) {
                count = Integer.parseInt(args[2]);
            }

            sendMsg(chatId, "Количество повторений: " + count);
            timeMapSetting(chatId, args);
            sendMsg(chatId, "Поставил таймер! Можно начинать работу ☺");
            System.out.println(userTimerRepository.toString());
        }
    }

    private void timeMapSetting(Long chatId, String[] args) {
        Instant nowTime = Instant.now();
        for (int i = count; i > 0; i--) {
            Instant workTime = nowTime.plus(Long.parseLong(args[0]), ChronoUnit.SECONDS);
            Instant breakTime = workTime.plus(Long.parseLong(args[1]), ChronoUnit.SECONDS);
            nowTime = breakTime;
            userTimerRepository.put(new UserTimer(workTime, TimerType.WORK), chatId);
            userTimerRepository.put(new UserTimer(breakTime, TimerType.BREAK), chatId);
        }
    }

    public void checkTimer() {
        while (true) {
            System.out.println("Количество таймеров пользователей " + userTimerRepository.size());
            userTimerRepository.forEach(((timer, userId) -> {
                if (Instant.now().isAfter(timer.userTimer)) {
                    switch (timer.timerType) {
                        case WORK -> sendMsg(userId, "Пора отдыхать! ☺");
                        case BREAK -> sendMsg(userId, "Время отдыха закончилось! Можно и поработать \uD83D\uDE05");
                    }
                    userTimerRepository.remove(timer);
                }
            }));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }
    }

    private void sendMsg(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.getStackTrace();
        }
    }
}
