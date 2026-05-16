package coinche7.repository;

import coinche7.model.Message;
import coinche7.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender = :user1 AND m.receiver = :user2) OR " +
           "(m.sender = :user2 AND m.receiver = :user1)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender = :user1 AND m.receiver = :user2) OR " +
           "(m.sender = :user2 AND m.receiver = :user1)) AND " +
           "m.createdAt > :timestamp " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversationSince(@Param("user1") User user1, @Param("user2") User user2, @Param("timestamp") Long timestamp);

       @Query("SELECT m.sender.id, COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.readAt IS NULL GROUP BY m.sender.id")
       List<Object[]> countUnreadGroupedBySender(@Param("receiver") User receiver);

       @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.readAt IS NULL")
       long countUnreadForReceiver(@Param("receiver") User receiver);

       @org.springframework.data.jpa.repository.Modifying
       @Query("UPDATE Message m SET m.readAt = :readAt WHERE m.receiver = :receiver AND m.sender = :sender AND m.readAt IS NULL")
       int markConversationAsRead(@Param("receiver") User receiver, @Param("sender") User sender, @Param("readAt") Long readAt);
}
