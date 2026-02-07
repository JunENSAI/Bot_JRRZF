package com.chess.jr_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Openings {
    private String eco;
    private String name;
    private String fen;
    private String moves;
}