package com.testarena.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @Column(name = "user_id")
    private  User user;

    @ManyToOne
    @Column(name = "test_id")
    private  Test test;

    @OneToOne(mappedBy = "feedback")
    private AttemptedTest attemptedTest;


    @Column(columnDefinition = "json")
    private String response;

    private Date attemptedDate;
}
