package coinche7.services;

import coinche7.model.User;
import coinche7.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EloInitializationService {

    private final UserRepository userRepository;

    public EloInitializationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeEloForExistingUsers() {
        List<User> users = userRepository.findAll();
        boolean updated = false;

        for (User user : users) {
            if (user.getElo() == null) {
                user.setElo(1000);
                updated = true;
            }
        }

        if (updated) {
            userRepository.saveAll(users);
        }
    }
}