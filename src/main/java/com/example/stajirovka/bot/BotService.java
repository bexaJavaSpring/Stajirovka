package com.example.stajirovka.bot;

import com.example.stajirovka.entity.User;
import com.example.stajirovka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.StringTemplate.STR;

@Service
@RequiredArgsConstructor
public class BotService {

    private final UserRepository userRepository;

    public void adminPanel(Update update, TelegramClient telegramClient) {

    }

    public void authenticate(User user, Update update, TelegramClient telegramClient) {
        if (user.getStep().equals(BotContains.START)) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton("Share Phone Number");
            button.setRequestContact(true);
            row.add(button);
            List<KeyboardRow> keyboard = new ArrayList<>();
            ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboard);
            keyboard.add(row);
            markup.setKeyboard(keyboard);
            markup.setResizeKeyboard(true);
            markup.setOneTimeKeyboard(true);
            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), STR."""
                    \uD83C\uDDFA\uD83C\uDDFF
                    Salom \{user.getLastName() != null ? user.getLastName() : ""}\{user.getFirstName() != null ? user.getFirstName() : user.getLastName()} \uD83D\uDC4B
                    @dt_learning_bot'ning rasmiy botiga xush kelibsiz

                    ⬇\uFE0F Kontaktingizni yuboring (tugmani bosib)

                    \uD83C\uDDFA\uD83C\uDDF8
                    Hi \{user.getLastName() != null ? user.getLastName() : ""}\{user.getFirstName() != null ? user.getFirstName() : user.getLastName()} \uD83D\uDC4B
                    Welcome to @dt_learning_bot's official bot

                    ⬇\uFE0F Send your contact (by clicking button)""");
            sendMessage.setReplyMarkup(markup);
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            getVerificationCode(telegramClient, update);
        }
    }

    private void getVerificationCode(TelegramClient telegramClient, Update update) {
        String verificationCode = generateVerificationCode(telegramClient, update.getMessage().getChatId());
        if (verificationCode != null) {
            String outputCode = "\uD83D\uDD12 Code: \n" + "``` " + verificationCode + " ```";
            String newCodeOutput = "\uD83C\uDDFA\uD83C\uDDFF\n" +
                    "\uD83D\uDD11 Yangi kod olish uchun /login ni bosing\n" +
                    "\n" +
                    "\uD83C\uDDFA\uD83C\uDDF8\uD83D\uDD11\n" +
                    "To get a new code click /login";
            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), outputCode + "\n" + newCodeOutput);
            sendMessage.setParseMode(ParseMode.MARKDOWNV2);
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void inlineUpdateButton(TelegramClient telegramClient, Update update) {
        String verificationCode = generateVerificationCode(telegramClient, update.getMessage().getChatId());
        if (verificationCode != null) {
            String outputCode = "\uD83D\uDD12 Code: \n" + "```" + verificationCode + "```";
            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), outputCode);
            sendMessage.setParseMode(ParseMode.MARKDOWNV2);
            sendMessage.setReplyMarkup(makeInlineMarkup());
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public ReplyKeyboard makeInlineMarkup() {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(BotContains.UPDATE_BUTTON);
        inlineKeyboardButton.setCallbackData(BotContains.UPDATE_BUTTON);
        List<InlineKeyboardRow> inlineKeyboardRow = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(inlineKeyboardButton);
        inlineKeyboardRow.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(inlineKeyboardRow);
        return markup;
    }

    public String generateVerificationCode(TelegramClient telegramClient, Long chatId) {
        User user = userRepository.findByChatId(chatId);
        if (user.getVerificationCode() != null) {
            User currentUser = userRepository.findByCodeAndChatId(chatId, user.getVerificationCode());
            if (currentUser != null) {
                Duration duration = Duration.between(currentUser.getIssueDate(), LocalDateTime.now());
                if (Math.abs(duration.getSeconds()) <= 120) {
                    SendMessage sendMessage = new SendMessage(chatId.toString(), "Eski kodingiz hali ham kuchda ☝\uFE0F");
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;
                } else {
                    SendMessage sendMessage = new SendMessage(chatId.toString(), "\uD83D\uDD12 Kod muddati tugadi. yangilash tugamsini bosib, yangi kod oling.\n" +
                            "\n" +
                            "\uD83D\uDD12 Code expired. Request a new code by pressing renew button.");
                    sendMessage.setParseMode(ParseMode.HTML);
                    sendMessage.setReplyMarkup(makeInlineMarkup());
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        }
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        user.setVerificationCode(code);
        user.setIssueDate(LocalDateTime.now());
        userRepository.save(user);
        return code;
    }

    public String generateNewCode(TelegramClient telegramClient, Long chatId) {
        User user = userRepository.findByChatId(chatId);
        if (user.getVerificationCode() != null) {
            User currentUser = userRepository.findByCodeAndChatId(chatId, user.getVerificationCode());
            if (currentUser != null) {
                Duration duration = Duration.between(currentUser.getIssueDate(), LocalDateTime.now());
                if (duration.getSeconds() <= 120) {
                    SendMessage sendMessage = new SendMessage(chatId.toString(), "Eski kodingiz hali ham kuchda ☝\uFE0F");
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        }
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        user.setVerificationCode(code);
        user.setIssueDate(LocalDateTime.now());
        userRepository.save(user);
        return code;
    }
}
