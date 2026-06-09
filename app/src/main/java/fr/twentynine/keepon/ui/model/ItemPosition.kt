package fr.twentynine.keepon.ui.model

/**
 * A list item's position, used to shape a card's corners and borders (first/last get rounded outer
 * corners; a lone item is [FIRST_AND_LAST] and rounds all four).
 */
enum class ItemPosition {
    FIRST,
    MIDDLE,
    LAST,
    FIRST_AND_LAST;

    companion object {
        /** Resolves the [ItemPosition] of the item at [itemIndex] within a list of [listSize]. */
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
