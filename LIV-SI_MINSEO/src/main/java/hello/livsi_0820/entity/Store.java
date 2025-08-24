package hello.livsi_0820.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "store_name", nullable = true, length = 100)
    private String storeName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "region_id", nullable = true)
    private Region region;

    @Column(name = "store_address", length = 100)
    private String storeAddress;

}