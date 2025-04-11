package com.teksxt.closedtesting.domain.usecase.validation

import com.teksxt.closedtesting.domain.model.ValidationResult
import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor() {

    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password cannot be blank"
            )
        }
        if (password.length < 8) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password must be at least 8 characters"
            )
        }
        val containsLettersAndDigits = password.any { it.isLetter() } && 
                password.any { it.isDigit() }
        if (!containsLettersAndDigits) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password must contain at least one letter and one digit"
            )
        }
        return ValidationResult(successful = true)
    }
}