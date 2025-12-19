package soft.divan.financemanager.core.domain.extension


fun Float.pretty(): String =
    if ((this % 1).toDouble() == 0.0) this.toInt().toString() else this.toString()