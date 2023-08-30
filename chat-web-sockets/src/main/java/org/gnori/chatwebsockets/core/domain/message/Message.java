package org.gnori.chatwebsockets.core.domain.message;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("messages")
@FieldDefaults(level = AccessLevel.PROTECTED)
public class Message {

    @PrimaryKey
    MessagePrimaryKey key;

    @Column("from_user")
    @CassandraType(type = CassandraType.Name.TEXT)
    String fromUser;

    @Column("text")
    @CassandraType(type = CassandraType.Name.TEXT)
    String text;
}
