package fr.twentynine.keepon.data.enums

enum class ItemPosition {
    FIRST,
    MIDDLE,
    LAST,
    FIRST_AND_LAST;

    companion object {
        fun getItemPosition(itemIndex: Int, listSize: Int): ItemPosition {
            return when {
                listSize == 1 -> FIRST_AND_LAST
                itemIndex == 0 -> FIRST
                itemIndex == listSize - 1 -> LAST
                itemIndex in 1 until listSize - 1 -> MIDDLE
                else -> throw IllegalArgumentException("Invalid item index: $itemIndex")
            }
        }
    }
}
