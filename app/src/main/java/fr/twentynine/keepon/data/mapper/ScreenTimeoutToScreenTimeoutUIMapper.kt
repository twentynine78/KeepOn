package fr.twentynine.keepon.data.mapper

import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.util.StringResourceProvider

object ScreenTimeoutToScreenTimeoutUIMapper : Mapper<ScreenTimeout, ScreenTimeoutUI> {
    private var stringResourceProvider: StringResourceProvider? = null

    private var isSelected: Boolean = false
    private var isDefault: Boolean = false
    private var isCurrent: Boolean = false
    private var isLocked: Boolean = false

    fun setStringResourceProvider(stringResourceProvider: StringResourceProvider): ScreenTimeoutToScreenTimeoutUIMapper {
        this.stringResourceProvider = stringResourceProvider

        return this
    }

    fun setIsSelected(isSelected: Boolean): ScreenTimeoutToScreenTimeoutUIMapper {
        this.isSelected = isSelected

        return this
    }

    fun setIsDefault(isDefault: Boolean): ScreenTimeoutToScreenTimeoutUIMapper {
        this.isDefault = isDefault

        return this
    }

    fun setIsCurrent(isCurrent: Boolean): ScreenTimeoutToScreenTimeoutUIMapper {
        this.isCurrent = isCurrent

        return this
    }

    fun setIsLocked(isLocked: Boolean): ScreenTimeoutToScreenTimeoutUIMapper {
        this.isLocked = isLocked

        return this
    }

    override fun map(from: ScreenTimeout): ScreenTimeoutUI {
        if (this.stringResourceProvider == null) {
            throw InstantiationException("stringResourceProvider not set!")
        }

        return ScreenTimeoutUI(
            value = from.value,
            displayName = this.stringResourceProvider?.let { from.getFullDisplayTimeout(it) }.toString(),
            isSelected = this.isSelected,
            isDefault = this.isDefault,
            isCurrent = this.isCurrent,
            isLocked = this.isLocked
        )
    }
}
