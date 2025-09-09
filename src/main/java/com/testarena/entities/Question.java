package com.testarena.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String question;

    @Column(name = "option_a")
    private String optionA;
    @Column(name = "option_b")
    private String optionB;
    @Column(name = "option_c")
    private String optionC;
    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_option")
    private String correctOption;

    private int marks;

    @ManyToMany(mappedBy = "questions")
    private Set<Test> tests = new HashSet<>();

    @OneToMany(mappedBy = "question")
    private  Set<TestResponse> testResponses = new HashSet<>();



}
