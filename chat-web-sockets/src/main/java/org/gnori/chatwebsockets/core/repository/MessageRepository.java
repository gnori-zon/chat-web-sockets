package org.gnori.chatwebsockets.core.repository;

import org.gnori.chatwebsockets.core.domain.message.Message;
import org.gnori.chatwebsockets.core.domain.message.MessagePrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CassandraRepository<Message, MessagePrimaryKey> {
}
