package com.testarena.feedback.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultItem{
    private String title;
    private String snippet;
    private String link;
}
