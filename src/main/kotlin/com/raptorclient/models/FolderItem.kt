package com.raptorclient.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class FolderItem(
    @JsonProperty("id")
    val id: String = UUID.randomUUID().toString(),
    @JsonProperty("name")
    var name: String = "New Folder",
    @JsonProperty("parentId")
    var parentId: String? = null,
    @JsonProperty("expanded")
    var expanded: Boolean = true,
    @JsonProperty("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
)
