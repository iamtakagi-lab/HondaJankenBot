package me.trollcoding.discord.hondajankenbot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.trollcoding.discord.hondajankenbot.audio.GuildMusicManager
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.managers.AudioManager
import kotlin.collections.HashMap
import com.jagrosh.jdautilities.command.CommandClientBuilder
import me.trollcoding.discord.hondajankenbot.command.JankenCommand
import java.util.*

class Bot (private val token: String) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot
        @JvmStatic
        val random: Random = Random()
    }

    lateinit var jda: JDA
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()

    fun start() {
        instance = this
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build()
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
        val builder = CommandClientBuilder()

        builder.setOwnerId("127640287249039360")
        builder.setPrefix("!")
        builder.addCommand(JankenCommand())
        builder.setHelpWord("honda")
        val client = builder.build()
        jda.addEventListener(client)
    }

    @Synchronized
    fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = java.lang.Long.parseLong(guild.id)
        var musicManager: GuildMusicManager? = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.sendHandler

        return musicManager
    }

    fun loadAndPlay(guild: Guild, voiceCh: VoiceChannel, trackUrl: String) {
        val musicManager = getGuildAudioPlayer(guild)

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                play(guild, voiceCh, musicManager, track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                var firstTrack: AudioTrack? = playlist.selectedTrack

                if (firstTrack == null) {
                    firstTrack = playlist.tracks[0]
                }

                play(guild, voiceCh, musicManager, firstTrack)
            }

            override fun noMatches() {

            }

            override fun loadFailed(exception: FriendlyException) {

            }
        })
    }

    fun play(guild: Guild, voiceCh: VoiceChannel, musicManager: GuildMusicManager, track: AudioTrack?) {
        connectToFirstVoiceChannel(guild.audioManager, voiceCh)

        musicManager.scheduler.queue(track!!)
    }

    private fun skipTrack(channel: TextChannel) {
        val musicManager = getGuildAudioPlayer(channel.guild)
        musicManager.scheduler.nextTrack()

        channel.sendMessage("Skipped to next track.").queue()
    }

    fun connectToFirstVoiceChannel(audioManager: AudioManager, voiceCh: VoiceChannel) {
        if (!audioManager.isConnected && !audioManager.isAttemptingToConnect) {
            audioManager.openAudioConnection(voiceCh)
        }
    }

}