package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempt")
@Getter
@Setter
@NoArgsConstructor
public class LoginAttempt {

    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(nullable = false)
    private LocalDateTime lastAttempt;

    public LoginAttempt(String username) {
        this.username = username;
        this.attempts = 1;
        this.lastAttempt = LocalDateTime.now();
    }
}
