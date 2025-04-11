package com.teksxt.closedtesting.domain.usecase.validation

import com.teksxt.closedtesting.domain.model.ValidationResult
import java.util.regex.Pattern
import javax.inject.Inject

class ValidateUrlUseCase @Inject constructor() {

    private val urlPattern = Pattern.compile(
        "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    )

    operator fun invoke(url: String): ValidationResult {
        if (url.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "URL cannot be blank"
            )
        }
        if (!urlPattern.matcher(url).matches()) {
            return ValidationResult(
                successful = false,
                errorMessage = "That's not a valid URL"
            )
        }
        return ValidationResult(successful = true)
    }
}