package kjistik.auth_server_komodo.Models;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("user")
public class User {
    @Id
    @Column("id")
    UUID id;

    @Column("email")
    String email;

    @Column("givenName")
    String givenName;

    @Column("lastName")
    String lastName;

    @Column("userName")
    String userName;

    @Column("password")
    String password;

    @Column("verified")
    boolean verified;

    @Column("createdAt")
    Timestamp createdAt;

    @Column("modifiedAt")
    Timestamp modifiedAt;
}
