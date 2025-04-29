package project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name = "address_and_time")
public class AddressAndTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_user_id", unique = true)
    private Long telegramUserId;
    private String homeAddress;
    private String workAddress;
    private LocalTime workStartTime;
    @OneToOne(mappedBy = "addressAndTime")
    private User user;

    public AddressAndTime() {
    }
}
