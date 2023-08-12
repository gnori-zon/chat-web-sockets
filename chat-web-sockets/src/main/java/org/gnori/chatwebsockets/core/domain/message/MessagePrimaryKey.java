package org.gnori.chatwebsockets.core.domain.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@PrimaryKeyClass
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessagePrimaryKey implements Serializable {

    @PrimaryKeyColumn(name = "username", type = PrimaryKeyType.PARTITIONED)
    String username;

    @PrimaryKeyColumn(name = "chat_room_id", type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = CassandraType.Name.TEXT)
    String chatRoomId;

    @PrimaryKeyColumn(name = "date", type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    LocalDateTime date;
}
