package com.testarena.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_response")
public class TestResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @Column(name = "user_id")
    private  User user;

    @ManyToOne
    @Column(name = "test_id")
    private  Test test;

    @ManyToOne
    @Column(name = "question_id")
    private  Question question;

    private String answer;

    @Column(name = "is_correct")
    private boolean isCorrect;

}
