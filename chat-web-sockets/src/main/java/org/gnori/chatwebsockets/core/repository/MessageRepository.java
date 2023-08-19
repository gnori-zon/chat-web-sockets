package org.gnori.chatwebsockets.core.repository;

import org.gnori.chatwebsockets.core.domain.message.Message;
import org.springframework.data.cassandra.repository.MapIdCassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MapIdCassandraRepository<Message> {

    @Query(value = "select * from messages where chat_room_id = :chatRoomId ALLOW FILTERING", allowFiltering = true)
    List<Message> findAllByChatId(String chatRoomId);
}
