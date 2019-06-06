package me.trollcoding.discord.hondajankenbot

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val bot = Bot(Secret.TOKEN)
        bot.start()
    }
}