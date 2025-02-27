package fr.le_campus_numerique.square_games.dto;

public class UserDto {
    private String id;
    private String email;

    public UserDto() {}

    public UserDto(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
