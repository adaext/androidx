package com.mysdk

public interface TestSandboxSdk {
  public fun echoBoolean(input: Boolean): Boolean

  public fun echoChar(input: Char): Char

  public fun echoDouble(input: Double): Double

  public fun echoFloat(input: Float): Float

  public fun echoInt(input: Int): Int

  public fun echoLong(input: Long): Long

  public fun echoString(input: String): String

  public fun receiveAndReturnNothing(): Unit

  public fun receiveMultipleArguments(
    first: Int,
    second: String,
    third: Long,
  ): Unit
}
