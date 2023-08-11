package org.gnori.chatwebsockets.core.repository;

import org.gnori.chatwebsockets.core.domain.chat.ChatRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends CrudRepository<ChatRoom, String> {
}
