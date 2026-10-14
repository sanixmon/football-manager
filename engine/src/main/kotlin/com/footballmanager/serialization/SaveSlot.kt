package com.footballmanager.serialization

enum class SaveSlot(val fileName: String, val displayName: String) {
    AUTOSAVE("autosave.json", "Autosave"),
    MANUAL("manual_save.json", "Quick Save"),
    SLOT_1("save_slot_1.json", "Career Slot 1"),
    SLOT_2("save_slot_2.json", "Career Slot 2"),
    SLOT_3("save_slot_3.json", "Career Slot 3");

    companion object {
        val userSlots = listOf(MANUAL, SLOT_1, SLOT_2, SLOT_3)
        val allSlots = entries
    }
}

data class SaveSlotMetadata(
    val slot: SaveSlot,
    val exists: Boolean,
    val managerName: String? = null,
    val clubName: String? = null,
    val inGameDate: String? = null,
    val roundInfo: String? = null,
    val lastModified: Long = 0L,
)
