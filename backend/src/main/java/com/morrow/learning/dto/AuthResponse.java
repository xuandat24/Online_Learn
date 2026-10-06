package com.morrow.learning.dto;

public record AuthResponse(String token, UserView user) {
}