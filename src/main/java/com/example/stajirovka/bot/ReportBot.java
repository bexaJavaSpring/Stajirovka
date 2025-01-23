package com.example.stajirovka.bot;

import com.example.stajirovka.entity.User;
import com.example.stajirovka.repository.RoleRepository;
import com.example.stajirovka.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ReportBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Value("${telegram.bot.token}")
    private String token;

    @PostConstruct
    public void init() {
        this.telegramClient = new OkHttpTelegramClient(token);
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if (message.getText().equals("/start")) {
                User user = userRepository.findByChatId(update.getMessage().getChatId());
                if (user != null) {
                    user.setStep(BotContains.AUTHENTIFICATION);
                    userRepository.save(user);
                } else {
                    user = new User();
                    user.setChatId(update.getMessage().getChatId());
                    user.setFirstName(update.getMessage().getFrom().getFirstName());
                    user.setLastName(update.getMessage().getFrom().getLastName());
                    user.setRoles(Arrays.asList(roleRepository.findByCode("USER")));
                    user.setStep(BotContains.START);
                    userRepository.save(user);
                }
            }
        }
    }

    @AfterBotRegistration
    public void afterRegistration (BotSession botSession){
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}