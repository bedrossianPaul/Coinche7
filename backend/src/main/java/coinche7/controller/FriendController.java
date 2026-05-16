package coinche7.controller;

import coinche7.model.FriendRequest;
import coinche7.model.User;
import coinche7.repository.FriendRequestRepository;
import coinche7.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;

    public FriendController(UserRepository userRepository, FriendRequestRepository friendRequestRepository) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<?> getFriends(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return unauthorized();
        }

        User me = loadUser(currentUser);
        return ResponseEntity.ok(Map.of(
                "friends", toUserList(me.getFriends()),
                "incomingRequests", toRequestList(friendRequestRepository.findByReceiver(me), true),
                "outgoingRequests", toRequestList(friendRequestRepository.findBySender(me), false)
        ));
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "") String q
    ) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return unauthorized();
        }

        User me = loadUser(currentUser);
        String query = q.trim();
        List<User> users = query.isBlank()
                ? userRepository.findAll()
                : userRepository.findByPseudoContainingIgnoreCase(query);

        List<Map<String, Object>> results = users.stream()
                .filter(user -> !user.getId().equals(me.getId()))
                .map(user -> toUserSummary(user, me))
                .toList();

        return ResponseEntity.ok(Map.of("results", results));
    }

    @SuppressWarnings("null")
    @Transactional
    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(
            HttpServletRequest request,
            @RequestParam long friendId
    ) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return unauthorized();
        }

        User me = loadUser(currentUser);
        User friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) {
            return notFound("User not found");
        }

        if (Objects.equals(me.getId(), friend.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot add yourself"));
        }

        if (me.getFriends().contains(friend)) {
            return ResponseEntity.ok(Map.of("message", "Already friends", "status", "FRIEND"));
        }

        // Si une demande existe déjà en sens inverse, on l'accepte directement.
        FriendRequest reverse = friendRequestRepository.findBySenderAndReceiver(friend, me).orElse(null);
        if (reverse != null) {
            acceptFriendRequest(me, friend, reverse);
            return ResponseEntity.ok(Map.of(
                    "message", "Friend request accepted",
                    "status", "FRIEND",
                    "friends", toUserList(me.getFriends())
            ));
        }

        if (friendRequestRepository.existsBySenderAndReceiver(me, friend)) {
            return ResponseEntity.ok(Map.of("message", "Request already sent", "status", "PENDING_OUT"));
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(me);
        friendRequest.setReceiver(friend);
        friendRequestRepository.save(friendRequest);

        return ResponseEntity.ok(Map.of(
                "message", "Friend request sent",
                "status", "PENDING_OUT"
        ));
    }

    @SuppressWarnings("null")
    @Transactional
    @PostMapping("/accept")
    public ResponseEntity<?> acceptRequest(
            HttpServletRequest request,
            @RequestParam long friendId
    ) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return unauthorized();
        }

        User me = loadUser(currentUser);
        User friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) {
            return notFound("User not found");
        }

        FriendRequest requestEntity = friendRequestRepository.findBySenderAndReceiver(friend, me).orElse(null);
        if (requestEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Friend request not found"));
        }

        acceptFriendRequest(me, friend, requestEntity);

        return ResponseEntity.ok(Map.of(
                "message", "Friend request accepted",
                "status", "FRIEND",
                "friends", toUserList(me.getFriends())
        ));
    }

    @SuppressWarnings("null")
    @Transactional
    @DeleteMapping("/reject")
    public ResponseEntity<?> rejectRequest(
            HttpServletRequest request,
            @RequestParam long friendId
    ) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return unauthorized();
        }

        User me = loadUser(currentUser);
        User friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) {
            return notFound("User not found");
        }

        FriendRequest requestEntity = friendRequestRepository.findBySenderAndReceiver(friend, me).orElse(null);
        if (requestEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Friend request not found"));
        }

        friendRequestRepository.delete(Objects.requireNonNull(requestEntity));
        return ResponseEntity.ok(Map.of("message", "Friend request rejected"));
    }

    @SuppressWarnings("null")
    @Transactional
    @DeleteMapping
    public ResponseEntity<?> removeFriend(
            HttpServletRequest request,
            @RequestParam long friendId
    ) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return unauthorized();
        }

        User me = loadUser(currentUser);
        User friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) {
            return notFound("User not found");
        }

        me.removeFriend(friend);
        friend.removeFriend(me);
        userRepository.save(me);
        userRepository.save(friend);

        return ResponseEntity.ok(Map.of(
                "message", "Friend removed",
                "friends", toUserList(me.getFriends())
        ));
    }

    private void acceptFriendRequest(User me, User friend, FriendRequest requestEntity) {
        friendRequestRepository.delete(Objects.requireNonNull(requestEntity));
        me.addFriend(friend);
        friend.addFriend(me);
        userRepository.save(me);
        userRepository.save(friend);
    }

    private User getCurrentUser(HttpServletRequest request) {
        Object authenticatedUser = request.getAttribute("authenticatedUser");
        if (authenticatedUser instanceof User user) {
            return user;
        }
        return null;
    }

    private User loadUser(User user) {
        Long userId = Objects.requireNonNull(user.getId(), "Authenticated user id cannot be null");
        return userRepository.findById(userId).orElse(user);
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    private ResponseEntity<Map<String, String>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", message));
    }

    private List<Map<String, Object>> toUserList(Iterable<User> users) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            result.add(toUserSummary(user, null));
        }
        return result;
    }

    private List<Map<String, Object>> toRequestList(List<FriendRequest> requests, boolean incoming) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendRequest request : requests) {
            User other = incoming ? request.getSender() : request.getReceiver();
            Map<String, Object> item = new HashMap<>();
            item.put("requestId", request.getId());
            item.put("id", other.getId());
            item.put("pseudo", other.getPseudo());
            item.put("avatarUrl", other.getAvatarUrl());
            item.put("createdAt", request.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> toUserSummary(User user, User me) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", user.getId());
        summary.put("pseudo", user.getPseudo());
        summary.put("avatarUrl", user.getAvatarUrl());
        summary.put("status", friendshipStatus(me, user));
        return summary;
    }

    private String friendshipStatus(User me, User candidate) {
        if (me == null) {
            return "NONE";
        }

        if (me.getFriends().contains(candidate)) {
            return "FRIEND";
        }

        if (friendRequestRepository.existsBySenderAndReceiver(me, candidate)) {
            return "PENDING_OUT";
        }

        if (friendRequestRepository.existsBySenderAndReceiver(candidate, me)) {
            return "PENDING_IN";
        }

        return "NONE";
    }
}