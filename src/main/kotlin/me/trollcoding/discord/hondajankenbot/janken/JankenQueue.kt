package me.trollcoding.discord.hondajankenbot.janken

import me.trollcoding.discord.hondajankenbot.Bot
import net.dv8tion.jda.client.entities.Application
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member

class JankenQueue(val memberId: Long, val textChId: Long, val guildId: Long)