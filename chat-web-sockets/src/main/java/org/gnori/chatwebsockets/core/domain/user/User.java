package org.gnori.chatwebsockets.core.domain.user;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.gnori.chatwebsockets.core.domain.AbstractEntity;
import org.gnori.chatwebsockets.core.domain.user.enums.Role;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"username"})
public class User extends AbstractEntity implements Serializable {

    @Column(name = "username", nullable = false, unique = true)
    String username;

    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "users_role",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"}, name = "uk_users_role_user_id_role")
    )
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinColumn
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<Role> roles = new ArrayList<>();

    @Column(name = "chat_ids")
    @Type(JsonType.class)
    List<String> chatIds = new ArrayList<>();

}
