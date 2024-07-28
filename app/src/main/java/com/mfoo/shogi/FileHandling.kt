package com.mfoo.shogi

import java.io.BufferedReader
import java.io.File
import java.nio.charset.Charset


fun readFile(filename: String): BufferedReader {
    return File(filename).bufferedReader(Charset.forName("SHIFT-JIS"))
}
