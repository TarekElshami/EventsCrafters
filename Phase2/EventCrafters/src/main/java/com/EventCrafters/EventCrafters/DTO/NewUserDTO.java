package com.EventCrafters.EventCrafters.DTO;

import com.EventCrafters.EventCrafters.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewUserDTO {
    private String name;
    private String username;
    private String email;
    private String password;


    public NewUserDTO(User user) {
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = "";
    }
}
