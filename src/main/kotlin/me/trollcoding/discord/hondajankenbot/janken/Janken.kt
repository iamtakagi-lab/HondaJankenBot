package me.trollcoding.discord.hondajankenbot.janken

import me.trollcoding.discord.hondajankenbot.Bot
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import java.util.*
import kotlin.collections.LinkedHashMap

class Janken(val voiceChannelId: Long, val guild: Guild) {

    private fun isWin(): Boolean {
        val n = Bot.random.nextInt(100)
        return n > 99
    }

    @Synchronized
    fun excecute() : Janken {
        val url: String

        if (isWin()) {
            url = JankenResources.winMovieURLs[Bot.random.nextInt(JankenResources.winMovieURLs.size)]
        } else {
            url = JankenResources.loseMovieURLs[Bot.random.nextInt(JankenResources.loseMovieURLs.size)]
        }

        Bot.instance.loadAndPlay(guild, guild.getVoiceChannelById(voiceChannelId), url)

        Timer(false).schedule(object : TimerTask() {
            override fun run() {
                guild.audioManager.closeAudioConnection()
                currentJankens[guild] = null
                if(queues.isNotEmpty()) {
                    val queue = queues.entries.stream().findFirst()
                    if(queue.isPresent) {
                        val queue = queue.get()
                        queue.value!!.excecute()
                        queues.remove(queue.key)
                    }
                }
            }
        }, 1000 * 30)
        return this
    }

    companion object {
        val queues: MutableMap<Member?, JankenQueue?> = LinkedHashMap()
        var currentJankens: MutableMap<Guild?, Janken?> = LinkedHashMap()

        fun addToQueue(member: Member): QueueResponse {
            if (queues.containsKey(member)) {
                return QueueResponse.ALREADY_IN_QUEUE
            }
            if (currentJankens[member.guild] != null) {
                queues[member] = JankenQueue(member)
                return QueueResponse.SUCCESSFULLY_ADDED_TO_QUEUE
            }
            if (!member.voiceState.inVoiceChannel()) {
                return QueueResponse.YOU_ARE_NOT_IN_VOICECHANNEL
            }
            currentJankens[member.guild] = Janken(member.voiceState.channel.idLong, member.guild).excecute()
            return QueueResponse.SUCCESSFULLY_STARTING_JANKEN
        }
    }

    enum class QueueResponse(val context: String) {
        ALREADY_IN_QUEUE(":x: 既にじゃんけんを予約済みだ。"),
        SUCCESSFULLY_ADDED_TO_QUEUE(":o: おっと、混みあっているようだ。 Queueに予約済したぞ: ${queues.size}"),
        SUCCESSFULLY_STARTING_JANKEN("かかってこい。じゃんけんするぞ！"),
        YOU_ARE_NOT_IN_VOICECHANNEL("ボイスチャンネルに入ってからオレを呼んでくれ！")
    }
}