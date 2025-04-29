package project.service;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.dto.UserDTO;
import project.mapper.UserMapper;
import project.model.User;
import project.repository.UserRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class ParseService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public ParseService(UserService userService, UserRepository userRepository, UserMapper userMapper) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public void parseAndSaveUser(String input) {
        String[] parts = input.split(";");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Формат должен быть: telegramId; username; firstName");
        }

        Long telegramId = Long.parseLong(parts[0].trim());
        String username = parts[1].trim();
        String firstName = parts[2].trim();

        Optional<User> existing = userRepository.findByTelegramUserId(telegramId);
        if (existing.isPresent()) {
            User user = existing.get();
            if (!user.isNotificationEnabled()) {
                user.setNotificationEnabled(true);
                userRepository.save(user);
            }
            return;
        }

        User newUser = userMapper.toNewUser(telegramId, username, firstName);
        userRepository.save(newUser);

    }

    public void parseAndSave(Long telegramId, String userInput) throws JSONException {
        String[] parts = userInput.split(";");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Ожидается 3 части: дом; работа; время. Снова выполните команду /go_to_work");
        }

        String home = parts[0].trim();
        String work = parts[1].trim();
        String timeStr = parts[2].trim();

        LocalTime time;
        try {
            time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат времени. Ожидается HH:mm. Снова выполните команду /go_to_work");
        }

        UserDTO dto = userMapper.toUserDTO(telegramId, home, work, time);
        userService.saveUserWork(dto);
    }
}
