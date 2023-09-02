package org.gnori.chatwebsockets.core.repository;

import org.gnori.chatwebsockets.core.domain.message.Message;
import org.gnori.chatwebsockets.core.domain.message.MessagePrimaryKey;
import org.springframework.data.cassandra.repository.MapIdCassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MapIdCassandraRepository<Message> {

    @Query(value = "select * from messages where chat_room_id = :chatRoomId ALLOW FILTERING", allowFiltering = true)
    List<Message> findAllByChatId(String chatRoomId);

    @Query(value = "delete from messages where chat_room_id = :chatRoomId and username = :username and date = :date")
    void deleteByKey(String username, String chatRoomId, LocalDateTime date);

    @Query(value = "select * from messages where chat_room_id = :chatRoomId and username = :username and date = :date")
    Optional<Message> findByKey(String username, String chatRoomId, LocalDateTime date);

    @Query(value = "delete from messages where chat_room_id = :chatRoomId and username = :username")
    void deleteAllByChatRoomIdAndUsername(String username, String chatRoomId);
}
