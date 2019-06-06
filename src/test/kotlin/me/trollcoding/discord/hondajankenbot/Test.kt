package me.trollcoding.discord.hondajankenbot

import java.util.*

object Test {
    val random: Random = Random()

    @JvmStatic
    fun main(args: Array<String>) {
        for(i in 0..100){
            System.out.println("$i: ${random.nextInt(100)}")
        }
    }
}