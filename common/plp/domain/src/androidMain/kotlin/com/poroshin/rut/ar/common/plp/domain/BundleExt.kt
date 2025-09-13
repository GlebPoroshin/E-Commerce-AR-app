package com.poroshin.rut.ar.common.plp.domain

import android.os.Bundle

fun PdpParams.bundleAuto(): Bundle = Bundle().apply {
    putLong("sku", sku)
}



