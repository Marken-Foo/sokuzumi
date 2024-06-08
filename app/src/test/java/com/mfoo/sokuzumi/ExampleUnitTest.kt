package com.mfoo.sokuzumi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest : FunSpec ({
    test("Assert 2+2=4") {
        2 + 2 shouldBe 4
    }
})