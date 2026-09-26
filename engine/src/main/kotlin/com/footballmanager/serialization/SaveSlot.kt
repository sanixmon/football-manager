package com.footballmanager.serialization

enum class SaveSlot(val fileName: String) {
    AUTOSAVE("autosave.json"),
    MANUAL("manual_save.json"),
}
