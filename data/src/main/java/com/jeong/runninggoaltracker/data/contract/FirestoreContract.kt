package com.jeong.runninggoaltracker.data.contract

object FirestoreFields {
    const val UID = "uid"
    const val NICKNAME = "nickname"
    const val NORMALIZED_NICKNAME = "normalizedNickname"
    const val CREATED_AT = "createdAt"
    const val LAST_ACTIVE_AT = "lastActiveAt"
    const val IS_ANONYMOUS = "isAnonymous"
}

object FirestorePaths {
    const val COLLECTION_USERNAMES = "usernames"
    const val COLLECTION_USERS = "users"
}
