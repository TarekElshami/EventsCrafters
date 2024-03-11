package com.EventCrafters.EventCrafters.DTO;


import com.EventCrafters.EventCrafters.model.User;

import java.util.List;

public class FullUserDTO extends CensoredUserDTO {
    private Long id;
    private String name;
    private String username;
    private String email;

    private String photo;

    private boolean banned;
    private List<String> roles;

    public FullUserDTO(String name, String username, String email, boolean banned, List<String> roles, Long id) {
        super(name,username,id);
        this.name = name;
        this.username = username;
        this.email = email;
        this.banned = banned;
        this.roles = roles;
        this.id = id;
        this.photo = "/api/users/img/" + id;
    }

    public FullUserDTO(User user) {
        super(user.getName(),user.getUsername(), user.getId());
        this.name = user.getName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.banned = user.isBanned();
        this.roles = user.getRoles();
        this.id = user.getId();
        this.photo = "/api/users/img/" + user.getId();
    }
}
