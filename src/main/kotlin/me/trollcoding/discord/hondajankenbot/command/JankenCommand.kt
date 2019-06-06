package me.trollcoding.discord.hondajankenbot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.trollcoding.discord.hondajankenbot.Bot
import me.trollcoding.discord.hondajankenbot.janken.Janken

class JankenCommand : Command(){

    init {
        this.name = "janken"
        this.aliases = arrayOf("j")
        this.help = "本田とじゃんけんをする"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {
            val result: Janken.QueueResult = Janken.addToQueue(member, event.textChannel.idLong)
            channel.sendMessage(result.context.replace("{mention}", member.asMention)).queue()
        }
    }
}