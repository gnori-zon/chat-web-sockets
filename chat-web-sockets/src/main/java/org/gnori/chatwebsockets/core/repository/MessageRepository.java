package org.gnori.chatwebsockets.core.repository;

import org.gnori.chatwebsockets.core.domain.message.Message;
import org.springframework.data.cassandra.repository.MapIdCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MapIdCassandraRepository<Message> {

}
