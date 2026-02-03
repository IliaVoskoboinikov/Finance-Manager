package soft.divan.financemanager.feature.designapp.impl.data.mapper

inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    return enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) }
}