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
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final BotService botService;
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
        final String adminId = "958536406";
        User currentUser;
        if (update.hasMessage()) {
            User user = userRepository.findByChatId(update.getMessage().getChatId());
            Message message = update.getMessage();
            if (message.hasText()) {
                if (message.getText().equals("/start")) {
                    if (user != null) {
                        user.setStep(BotContains.AUTHENTIFICATION);
                        currentUser = userRepository.save(user);
                    } else {
                        user = new User();
                        user.setChatId(update.getMessage().getChatId());
                        user.setFirstName(update.getMessage().getFrom().getFirstName());
                        user.setLastName(update.getMessage().getFrom().getLastName());
                        user.setRoles(Arrays.asList(roleRepository.findByCode("USER")));
                        user.setStep(BotContains.START);
                        currentUser = userRepository.save(user);
                    }
                    if (currentUser.getChatId().equals(adminId)) {
                        botService.adminPanel(update, telegramClient);
                    } else {
                        botService.authenticate(currentUser, update, telegramClient);
                    }

                }
                if (message.getText().equals("/login")) {
                    currentUser = userRepository.findByChatId(message.getChatId());
                    if (currentUser == null) {
                        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), "\uD83C\uDDFA\uD83C\uDDFF\n" +
                                "\uD83D\uDD11 Avval ro'yxatdan o'ting! Ro'yxatdan o'tish uchun /start ni bosing\n" +
                                "\n" +
                                "\uD83C\uDDFA\uD83C\uDDF8\uD83D\uDD11\n" +
                                "You should register first! To register the bot click /start ");
                        try {
                            telegramClient.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    botService.inlineUpdateButton(telegramClient, update);
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            User user = userRepository.findByChatId(update.getMessage().getChatId());
            Contact contact = update.getMessage().getContact();
            String phoneNumber = contact.getPhoneNumber();
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);
            String verificationCode = botService.generateVerificationCode(telegramClient, update.getMessage().getChatId());
            if (verificationCode != null) {
                String outputCode = "\uD83D\uDD12 Code: \n" + "``` " + verificationCode + " ```";
                String newCodeOutput = "\uD83C\uDDFA\uD83C\uDDFF\n" +
                        "\uD83D\uDD11 Yangi kod olish uchun /login ni bosing\n" +
                        "\n" +
                        "\uD83C\uDDFA\uD83C\uDDF8\uD83D\uDD11\n" +
                        "To get a new code click /login";
                SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), outputCode + "\n" + newCodeOutput);
                List<KeyboardRow> rows = new ArrayList<>();
                ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
                markup.setKeyboard(new ArrayList<>());
                sendMessage.setParseMode(ParseMode.MARKDOWNV2);
                sendMessage.setReplyMarkup(markup);
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.getCallbackQuery().getData().equals(BotContains.UPDATE_BUTTON)) {
            String verificationCode = botService.generateNewCode(telegramClient, update.getCallbackQuery().getMessage().getChatId());
            if (verificationCode != null) {
                String outputCode = "\uD83D\uDD12 Code: \n" + "```" + verificationCode + "```";
                String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);
                try {
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                SendMessage sendMessage = new SendMessage(chatId, outputCode);
                sendMessage.setReplyMarkup(botService.makeInlineMarkup());
                sendMessage.setParseMode(ParseMode.MARKDOWNV2);
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}