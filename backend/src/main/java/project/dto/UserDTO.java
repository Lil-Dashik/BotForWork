package project.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UserDTO {
    private Long telegramUserId;
    private String homeAddress;
    private String workAddress;
    private LocalTime workStartTime;

    public UserDTO() {
    }

}
