package com.wiremock_Boot.wiremock_Boot.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


/*
*
*  Lombok will handle creating constructor , getter and setter
* */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    public String cast;
    public Long movie_id;
    public String name;
    public LocalDate release_date;
    public Integer year;
}
