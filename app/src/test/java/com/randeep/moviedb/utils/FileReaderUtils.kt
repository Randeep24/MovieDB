package com.randeep.moviedb.utils

object FileReaderUtils {

        /**
         * Reads a file from src/test/resources folder
         * and returns file text as string
         */
        fun readTestResourceFile(fileName: String): String {
                val fileInputStream = javaClass.classLoader?.getResourceAsStream(fileName)
                return fileInputStream?.bufferedReader()?.readText() ?: ""
        }
}