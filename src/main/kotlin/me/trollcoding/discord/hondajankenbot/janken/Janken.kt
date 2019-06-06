package me.trollcoding.discord.hondajankenbot.janken

import com.sun.org.apache.xpath.internal.operations.Bool
import me.trollcoding.discord.hondajankenbot.Bot
import net.dv8tion.jda.core.entities.Member
import java.util.*
import kotlin.collections.LinkedHashMap

class Janken(val memberId: Long, val voiceChannelId: Long, val guildId: Long) {

    private fun isWin(): Boolean {
        val n = Bot.random.nextInt(100)
        return n > 99
    }

    @Synchronized
    fun excecute(textChId: Long, queue: Boolean) : Janken? {
        val url: String
        if (isWin()) {
            url = JankenResources.winMovieURLs[Bot.random.nextInt(JankenResources.winMovieURLs.size)]
        } else {
            url = JankenResources.loseMovieURLs[Bot.random.nextInt(JankenResources.loseMovieURLs.size)]
        }

        val guild = Bot.instance.jda.getGuildById(guildId)

        if (guild == null) {
            queues.remove(guildId)
            return null
        }

        if (!queue) {
            Bot.instance.loadAndPlay(guild, guild.getVoiceChannelById(voiceChannelId), url)
        }

        val janken: Janken = this

        Timer(false).schedule(object : TimerTask() {
            override fun run() {
                guild.audioManager.closeAudioConnection()
                currentJankens.remove(guild.idLong)
                if(queues.isNotEmpty()) {
                    val queue = queues.entries.stream().findFirst()
                    if (queue.isPresent) {
                        val queue = queue.get()

                        val member = guild.getMemberById(queue.key)

                        if (member != null) {
                            val url2: String
                            if (isWin()) {
                                url2 =
                                    JankenResources.winMovieURLs[Bot.random.nextInt(JankenResources.winMovieURLs.size)]
                            } else {
                                url2 =
                                    JankenResources.loseMovieURLs[Bot.random.nextInt(JankenResources.loseMovieURLs.size)]
                            }
                            Bot.instance.loadAndPlay(guild, guild.getVoiceChannelById(voiceChannelId), url2)
                            val textCh = guild.getTextChannelById(textChId)
                            if (textCh != null) {
                                textCh.sendMessage(":o: お前の番だ！じゃんけんするぞ！ " + member.asMention).queue()
                                Janken.currentJankens[guildId] = janken.excecute(textChId, true)
                            }
                        }
                        queues.remove(queue.key)
                    }
                }
            }
        }, 1000 * 30)
        return this
    }

    companion object {
        //UserID, JankenQueue
        val queues: MutableMap<Long, JankenQueue?> = LinkedHashMap()
        //GuildID, Janken
        var currentJankens: MutableMap<Long, Janken?> = LinkedHashMap()

        fun addToQueue(member: Member, textChId: Long): QueueResult {
            /*if(currentJankens[guildId] != null && currentJankens[guildId]!!.memberId == member.user.idLong){
                return QueueResult.YOU_ARE_DOING_JANKEN
            }*/
            if (queues.containsKey(member.user.idLong)) {
                return QueueResult.ALREADY_IN_QUEUE
            }
            if (currentJankens[member.guild.idLong] != null) {
                queues[member.user.idLong] = JankenQueue(member.user.idLong, textChId, member.guild.idLong)
                return QueueResult.SUCCESSFULLY_ADDED_TO_QUEUE
            }
            if (!member.voiceState.inVoiceChannel()) {
                return QueueResult.YOU_ARE_NOT_IN_VOICECHANNEL
            }
            currentJankens[member.guild.idLong] = Janken(member.user.idLong, member.voiceState.channel.idLong, member.guild.idLong).excecute(textChId, false)
            return QueueResult.SUCCESSFULLY_STARTING_JANKEN
        }
    }

    enum class QueueResult(val context: String) {
        YOU_ARE_DOING_JANKEN(":x: お前はじゃんけん中だ。{mention}"),
        ALREADY_IN_QUEUE(":x: 既にじゃんけんを予約済みだ。{mention}"),
        SUCCESSFULLY_ADDED_TO_QUEUE(":o: おっと、混みあっているようだ\nQueueに予約したぞ。 順番: ${queues.size+1} {mention}"),
        SUCCESSFULLY_STARTING_JANKEN(":o: かかってこい。じゃんけんするぞ！ {mention}"),
        YOU_ARE_NOT_IN_VOICECHANNEL(":x: ボイスチャンネルに入ってからオレを呼んでくれ！ {mention}")
    }
}