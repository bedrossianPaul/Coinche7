package coinche7.controller;

import coinche7.model.Message;
import coinche7.model.User;
import coinche7.repository.MessageRepository;
import coinche7.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @GetMapping("/conversation/{friendId}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable Long friendId,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Reload user to ensure friends relationship is loaded
            @SuppressWarnings("null")
            User reloadedCurrentUser = userRepository.findById(currentUser.getId()).orElse(null);
            if (reloadedCurrentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            currentUser = reloadedCurrentUser;

            @SuppressWarnings("null")
            User friend = userRepository.findById(friendId).orElse(null);
            if (friend == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérifier que ce sont des amis
            if (!currentUser.getFriends().contains(friend)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Message> messages = messageRepository.findConversation(currentUser, friend);
            messageRepository.markConversationAsRead(currentUser, friend, System.currentTimeMillis());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    @GetMapping("/conversation/{friendId}/since")
    public ResponseEntity<List<Message>> getConversationSince(
            @PathVariable Long friendId,
            @RequestParam Long timestamp,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Reload user to ensure friends relationship is loaded
            @SuppressWarnings("null")
            User reloadedUser2 = userRepository.findById(currentUser.getId()).orElse(null);
            if (reloadedUser2 == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            currentUser = reloadedUser2;

            @SuppressWarnings("null")
            User friend = userRepository.findById(friendId).orElse(null);
            if (friend == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérifier que ce sont des amis
            if (!currentUser.getFriends().contains(friend)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Message> messages = messageRepository.findConversationSince(currentUser, friend, timestamp);
            if (!messages.isEmpty()) {
                messageRepository.markConversationAsRead(currentUser, friend, System.currentTimeMillis());
            }
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    @PostMapping("/conversation/{friendId}")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long friendId,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {
        try {
            String content = payload.get("content");
            User sender = getCurrentUser(request);

            if (sender == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Reload sender to ensure friends relationship is loaded
            @SuppressWarnings("null")
            User reloadedSender = userRepository.findById(sender.getId()).orElse(null);
            if (reloadedSender == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            sender = reloadedSender;

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            @SuppressWarnings("null")
            User receiver = userRepository.findById(friendId).orElse(null);
            if (receiver == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérifier que ce sont des amis
            if (!sender.getFriends().contains(receiver)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Message message = new Message(sender, receiver, content.trim());
            Message saved = messageRepository.save(message);

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/unread-counts")
    public ResponseEntity<Map<String, Object>> getUnreadCounts(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            @SuppressWarnings("null")
            User reloadedCurrentUser = userRepository.findById(currentUser.getId()).orElse(null);
            if (reloadedCurrentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Object[]> grouped = messageRepository.countUnreadGroupedBySender(reloadedCurrentUser);
            long totalUnreadCount = messageRepository.countUnreadForReceiver(reloadedCurrentUser);

            List<Map<String, Object>> counts = new ArrayList<>();
            for (Object[] row : grouped) {
                Long senderId = ((Number) row[0]).longValue();
                Long unreadCount = ((Number) row[1]).longValue();

                Map<String, Object> item = new HashMap<>();
                item.put("friendId", senderId);
                item.put("unreadCount", unreadCount);
                counts.add(item);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalUnreadCount", totalUnreadCount);
            response.put("counts", counts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        Object authenticatedUser = request.getAttribute("authenticatedUser");
        if (authenticatedUser instanceof User user) {
            return user;
        }
        return null;
    }
}
