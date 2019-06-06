package me.trollcoding.discord.hondajankenbot.janken

import net.dv8tion.jda.core.entities.Member

class JankenQueue(val member: Member){

    fun excecute(){
        if(member.voiceState.inVoiceChannel()) {
            Janken.currentJankens[member.guild] = Janken(member.voiceState.channel.idLong, member.guild).excecute()
        }
    }
}