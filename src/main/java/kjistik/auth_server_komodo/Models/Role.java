package kjistik.auth_server_komodo.Models;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("role")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @Column("id")
    UUID id;

    @Column("role")
    String role;
}
