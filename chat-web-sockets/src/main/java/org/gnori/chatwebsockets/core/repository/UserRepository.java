package org.gnori.chatwebsockets.core.repository;

import jakarta.transaction.Transactional;
import org.gnori.chatwebsockets.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Transactional
    @Modifying
    @Query(value = """
            update users
            set chat_ids = to_jsonb(coalesce(chat_ids, '[]')) || to_jsonb(:id)
            where username = :username
            """, nativeQuery = true)
    void addChatRoomId(String username, String id);

    @Transactional
    @Modifying
    @Query(value = """
           update users
           set chat_ids = chat_ids - :id
           where username = :username
           """, nativeQuery = true)
    void deleteChatRoomId(String username, String id);

    @Query(value = """
            select count(*) = 1
            from users
            where username = :username and chat_ids @> to_jsonb(:id)
           """, nativeQuery = true)
    boolean hasChatRoomId(String username, String id);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);

}
