package com.cybernexus.nexguard.network

data class ContactItem(
    val name: String,
    val phone: String
)

data class ContactsRequest(
    val deviceId: String,
    val contacts: List<ContactItem>
)
