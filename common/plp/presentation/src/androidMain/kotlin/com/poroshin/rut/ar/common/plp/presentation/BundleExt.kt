package com.poroshin.rut.ar.common.plp.presentation

import android.os.Bundle
import com.poroshin.rut.ar.common.pdp.domain.PdpParams

fun PdpParams.bundleAuto(): Bundle = Bundle().apply {
    putLong("sku", sku)
}



