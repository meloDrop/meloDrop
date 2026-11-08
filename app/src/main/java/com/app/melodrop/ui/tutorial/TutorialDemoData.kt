package com.app.melodrop.ui.tutorial

import com.app.melodrop.network.dto.BadgeOut
import com.app.melodrop.network.dto.FeedItem
import com.app.melodrop.network.dto.LabeledSongOut
import com.app.melodrop.network.dto.LikedSongOut
import com.app.melodrop.network.dto.MyDropOut
import com.app.melodrop.network.dto.PodiumSongOut
import com.app.melodrop.network.dto.ProfileDropOut
import com.app.melodrop.network.dto.ProfileOut
import com.app.melodrop.network.dto.StatsOut
import com.app.melodrop.network.dto.TrackResult
import com.app.melodrop.network.dto.UserBriefOut
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Example data of the tutorial screens: real songs (the same as database/demo.sql),
// with their Deezer covers, and made-up members. Nothing is sent to the backend

// A song of the examples, with its Deezer cover
private class DemoSong(
    val id: Int,
    val deezerId: Long,
    val title: String,
    val artist: String,
    val genre: String?,
    coverHash: String,
) {
    val coverUrl = "https://cdn-images.dzcdn.net/images/cover/$coverHash/500x500-000000-80-0-0.jpg"
}

private val BlindingLights = DemoSong(14, 908604612, "Blinding Lights", "The Weeknd", "R&B", "fd00ebd6d30d7253f813dba3bb1c66a9")
private val Levitating = DemoSong(19, 1124841682, "Levitating", "Dua Lipa", "Pop", "f8364f090ba04f1b19b381ec0390f3e4")
private val BillieJean = DemoSong(10, 4603408, "Billie Jean", "Michael Jackson", "Pop", "a0ad67d1beb761f2cb9f8b60e5bcf07a")
private val Numb = DemoSong(1, 677232, "Numb", "Linkin Park", "Alternative", "44df4f6fb2534768f4924365c103d0f7")
private val AlorsOnDanse = DemoSong(3, 6297555, "Alors on danse", "Stromae", "Dance", "43bd78a4753df33da9efc2207c4286ee")
private val Rehab = DemoSong(5, 2176852, "Rehab", "Amy Winehouse", "R&B", "5772b495f0dcdf660d0fc88c4c38a3fa")
private val BohemianRhapsody = DemoSong(9, 4091937401, "Bohemian Rhapsody", "Queen", "Rock", "0b56f4eee05fa1ea753c5654b2cdb70c")
private val RollingInTheDeep = DemoSong(11, 8086126, "Rolling in the Deep", "Adele", "Pop", "dc1ce848d830ecc93521be5a78350364")
private val BadGuy = DemoSong(13, 655095912, "bad guy", "Billie Eilish", "Alternative", "6630083f454d48eadb6a9b53f035d734")
private val BalanceTonQuoi = DemoSong(15, 546875572, "Balance ton quoi", "Angèle", "Pop", "4a2360324af313f73b56fd1f7faaac88")
private val Dreams = DemoSong(17, 63480987, "Dreams", "Fleetwood Mac", "Rock", "9732751ce91d786dcf30069853697078")
private val LaVieEnRose = DemoSong(20, 73693857, "La Vie en Rose", "Edith Piaf", null, "6e6dde2c332dcb75e76f302f19de636c")
private val GetLucky = DemoSong(2, 66609426, "Get Lucky", "Daft Punk", "Electro", "bc49adb87758e0c8c4e508a9c5cce85d")

// Made-up members who dropped the song of the first card
private val DemoDroppers = listOf(
    UserBriefOut(id = 2, username = "elliot", displayName = "Elliot", avatarColor = "peach", avatarStyle = "rock"),
    UserBriefOut(id = 3, username = "sam", avatarColor = "mint", avatarStyle = "pop"),
    UserBriefOut(id = 4, username = "nina", displayName = "Nina", avatarColor = "rose"),
)

// Discover: a song dropped by 3 members (one followed), and a second card behind
val DemoFeed = listOf(
    FeedItem(
        id = 1,
        songId = BlindingLights.id,
        title = BlindingLights.title,
        artist = BlindingLights.artist,
        coverUrl = BlindingLights.coverUrl,
        genre = BlindingLights.genre,
        authorId = 2,
        authorUsername = "elliot",
        savesCount = 12,
        isFollowed = true,
        droppers = DemoDroppers,
    ),
    FeedItem(
        id = 2,
        songId = Dreams.id,
        title = Dreams.title,
        artist = Dreams.artist,
        coverUrl = Dreams.coverUrl,
        genre = Dreams.genre,
        authorId = 3,
        authorUsername = "sam",
        savesCount = 4,
        droppers = DemoDroppers.take(1),
    ),
)

// Drop: what "li" finds, the first one is chosen
private fun track(song: DemoSong) = TrackResult(song.deezerId, song.title, song.artist, song.coverUrl, null)

val DemoSearchQuery = "li"
val DemoTracks = listOf(track(Levitating), track(BillieJean), track(BlindingLights))

// The drop of the day of the example, with 17 h left. A function: the time left counts from now
fun demoMyDrop() = MyDropOut(
    id = 100,
    songId = Levitating.id,
    title = Levitating.title,
    artist = Levitating.artist,
    coverUrl = Levitating.coverUrl,
    genre = Levitating.genre,
    expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(17).plusMinutes(42).toString(),
    savesCount = 9,
    picksCount = 3,
    viewsCount = 15,
)

// Profil: an example member with a full activity and a full Mixtape
private fun profileDrop(id: Int, song: DemoSong, daysAgo: Long, likes: Int, picks: Int, views: Int) = ProfileDropOut(
    id = id,
    songId = song.id,
    title = song.title,
    artist = song.artist,
    coverUrl = song.coverUrl,
    genre = song.genre,
    droppedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(daysAgo).toString(),
    savesCount = likes,
    picksCount = picks,
    viewsCount = views,
)

private fun pick(song: DemoSong, by: UserBriefOut) = LikedSongOut(
    songId = song.id,
    title = song.title,
    artist = song.artist,
    coverUrl = song.coverUrl,
    likedAt = "2026-11-01T18:00:00Z",
    authorId = by.id,
    authorUsername = by.username,
    isPick = true,
    genre = song.genre,
    droppers = listOf(by),
)

val DemoProfile = ProfileOut(
    id = 0,
    username = "alex",
    displayName = "Alex",
    createdAt = "2026-10-01T10:00:00Z",
    avatarColor = "mint",
    avatarStyle = "pop",
    stats = StatsOut(dropsCount = 18, likesReceived = 74, likesCount = 41),
    currentDrop = profileDrop(100, Levitating, daysAgo = 0, likes = 9, picks = 3, views = 15),
    recentDrops = listOf(
        profileDrop(101, Numb, daysAgo = 1, likes = 6, picks = 1, views = 11),
        profileDrop(102, AlorsOnDanse, daysAgo = 2, likes = 8, picks = 2, views = 12),
        profileDrop(103, Rehab, daysAgo = 3, likes = 4, picks = 0, views = 9),
        profileDrop(104, BadGuy, daysAgo = 4, likes = 7, picks = 1, views = 10),
        profileDrop(105, GetLucky, daysAgo = 5, likes = 5, picks = 1, views = 8),
    ),
    podium = listOf(
        PodiumSongOut(1, BohemianRhapsody.id, BohemianRhapsody.deezerId, BohemianRhapsody.title, BohemianRhapsody.artist, BohemianRhapsody.coverUrl),
        PodiumSongOut(2, RollingInTheDeep.id, RollingInTheDeep.deezerId, RollingInTheDeep.title, RollingInTheDeep.artist, RollingInTheDeep.coverUrl),
        PodiumSongOut(3, BillieJean.id, BillieJean.deezerId, BillieJean.title, BillieJean.artist, BillieJean.coverUrl),
    ),
    // Two labels only: the badges stay on the screen of small phones
    labeledSongs = listOf(
        LabeledSongOut("never_skip", Dreams.id, Dreams.deezerId, Dreams.title, Dreams.artist, Dreams.coverUrl),
        LabeledSongOut("cry", LaVieEnRose.id, LaVieEnRose.deezerId, LaVieEnRose.title, LaVieEnRose.artist, LaVieEnRose.coverUrl),
    ),
    badges = listOf(
        BadgeOut(1, "Testeur de la 1re heure", "Tu as testé meloDrop avant sa sortie.", "drop", "spectrum", "2026-11-03T18:00:00Z"),
        BadgeOut(2, "Tube du jour", "Le drop le plus liké d'une journée.", "flame", "peach", "2026-11-02T18:00:00Z"),
        BadgeOut(3, "7 jours d'affilée", "Un drop par jour pendant 7 jours.", "calendar", "mint", "2026-10-30T18:00:00Z"),
    ),
    followersCount = 23,
    followingCount = 31,
    isMe = true,
)

// The picks of the example member, for the "Mes picks" tab
val DemoPicks = listOf(
    pick(BalanceTonQuoi, DemoDroppers[2]),
    pick(BlindingLights, DemoDroppers[0]),
    pick(Rehab, DemoDroppers[1]),
)
