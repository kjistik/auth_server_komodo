package kjistik.auth_server_komodo.Models;

import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("user_role")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User_role {

    @Column("user_id")
    UUID user_id;
    @Column("role_id")
    UUID role_id;
}
