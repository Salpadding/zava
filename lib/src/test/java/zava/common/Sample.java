package zava.common;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sample {
    @Id
    private Long id;

    private String meta;

    @Transient
    private transient JdbcTemplate jdbc;
}
