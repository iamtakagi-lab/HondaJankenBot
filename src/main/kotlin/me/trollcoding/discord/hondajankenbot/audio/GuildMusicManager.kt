package me.trollcoding.discord.hondajankenbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

/**
 * Holder for both the player and a track scheduler for one guild.
 */
class GuildMusicManager
/**
 * Creates a player and a track scheduler.
 * @param manager Audio player order to use for creating the player.
 */
    (manager: AudioPlayerManager) {
    /**
     * Audio player for the guild.
     */
    val player: AudioPlayer
    /**
     * Track scheduler for the player.
     */
    val scheduler: TrackScheduler

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    val sendHandler: AudioPlayerSendHandler
        get() = AudioPlayerSendHandler(player)

    init {
        player = manager.createPlayer()
        scheduler = TrackScheduler(player)
        player.addListener(scheduler)
    }
}
