package hello.livsi_0820.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, length = 40)
    private String password;

    @Column(name = "member_name", nullable = trueg, length = 10)
    private String memberName;

    @Column(nullable = false, unique = true, length = 40)
    private String email;
}
