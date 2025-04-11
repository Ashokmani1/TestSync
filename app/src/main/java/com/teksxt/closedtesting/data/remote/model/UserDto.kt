package com.teksxt.closedtesting.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.profile.domain.model.UserType

data class UserDto(
    @DocumentId
    val id: String = "",
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("email")
    val email: String = "",
    
    @PropertyName("photo_url")
    val photoUrl: String? = null,
    
    @PropertyName("user_type")
    val userType: String = "UNKNOWN",
    
    @PropertyName("is_onboarded")
    val isOnboarded: Boolean = false,
    
    @PropertyName("created_at")
    val createdAt: Timestamp = Timestamp.now(),
    
    @PropertyName("updated_at")
    val updatedAt: Timestamp = Timestamp.now(),
    
    // Add data field to hold document data
    val data: Map<String, Any>? = null
) {
    // Add toMap method
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["name"] = name
        map["email"] = email
        photoUrl?.let { map["photo_url"] = it }
        map["user_type"] = userType
        map["is_onboarded"] = isOnboarded
        map["created_at"] = createdAt
        map["updated_at"] = updatedAt
        return map
    }
}

fun UserDto.toDomainModel(): User
{
    return User(
        id = id,
        name = name,
        email = email,
        photoUrl = photoUrl,
        userType = UserType.valueOf(userType),
        isOnboarded = isOnboarded
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        photoUrl = photoUrl,
        userType = userType.name,
        isOnboarded = isOnboarded
    )
}