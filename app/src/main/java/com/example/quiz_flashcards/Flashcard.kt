package com.example.quiz_flashcards

data class Flashcard(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String
)