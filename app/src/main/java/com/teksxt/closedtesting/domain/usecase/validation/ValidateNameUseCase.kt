package com.teksxt.closedtesting.domain.usecase.validation

import com.teksxt.closedtesting.domain.model.ValidationResult
import javax.inject.Inject

class ValidateNameUseCase @Inject constructor() {

    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name cannot be blank"
            )
        }
        if (name.length < 2) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name must be at least 2 characters"
            )
        }
        return ValidationResult(successful = true)
    }
}