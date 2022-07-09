package dev.dnbln.asms.lang.psi

val AsmMem.label: AsmLabelRef?
    get() {
        if (memSegmentOverride != null || memBaseIndexScale != null)
            return null

        val off = memOffset ?: return null
        if (off.memOffsetElementList.size != 1)
            return null

        val (offElem) = off.memOffsetElementList

        return offElem.labelRef
    }