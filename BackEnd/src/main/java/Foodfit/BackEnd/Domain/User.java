package Foodfit.BackEnd.Domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class User {


    @Builder
    public User(Long id, String name, int age, Gender gender, Long uid, byte[] profileImage) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.uid = uid;
        this.profileImage = profileImage;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    // oAuth2에서 DB에 중복 저장 체크를 막기 위해 제공하는 id값
    private Long uid;


    @Lob
    private byte[] profileImage;

    public String getProfileImage() {
        return new String(profileImage);
    }
}

