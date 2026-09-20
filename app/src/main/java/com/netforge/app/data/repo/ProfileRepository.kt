package com.netforge.app.data.repo

import com.netforge.app.data.db.ProfileDao
import com.netforge.app.data.db.ProfileEntity
import com.netforge.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val profileDao: ProfileDao) {

    val allProfilesFlow: Flow<List<Profile>> = profileDao.getAllProfilesFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getAllProfilesOnce(): List<Profile> {
        return profileDao.getAllProfiles().map { it.toDomain() }
    }

    suspend fun getProfileById(id: Long): Profile? {
        return profileDao.getProfileById(id)?.toDomain()
    }

    fun getProfileFlowById(id: Long): Flow<Profile?> {
        return profileDao.getProfileFlowById(id).map { it?.toDomain() }
    }

    suspend fun saveProfile(profile: Profile): Long {
        val entity = ProfileEntity.fromDomain(profile)
        return profileDao.insertOrUpdate(entity)
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) {
        profileDao.setFavorite(id, !current)
    }

    suspend fun renameProfile(id: Long, newName: String) {
        profileDao.renameProfile(id, newName)
    }

    suspend fun deleteProfile(id: Long) {
        profileDao.deleteById(id)
    }

    suspend fun incrementImportCount(id: Long) {
        profileDao.incrementImportCount(id)
    }

    suspend fun seedInitialProfilesIfEmpty() {
        if (profileDao.getCount() == 0) {
            val defaultProfile = Profile(
                id = 1L,
                name = "USA Fast Gateway (SSL)",
                author = "NetForge Core",
                note = "Primary secure SSL/TLS tunnel with SNI spoofing and keepalive verification",
                host = "104.16.132.229",
                port = 443,
                mode = Mode.SslTunnel,
                sshUser = "netforge",
                sshPass = "netforge-demo",
                sni = "cloudflare.com",
                frontHost = "cloudflare.com",
                dnsPrimary = "1.1.1.1",
                dnsSecondary = "1.0.0.1",
                mtu = 1500,
                keepalive = 15,
                enableUdp = true,
                isFavorite = true
            )
            val nlProfile = Profile(
                id = 2L,
                name = "Netherlands Amsterdam 01",
                author = "NetForge Core",
                note = "High speed Amsterdam tunnel node for streaming and low ping",
                host = "104.18.25.1",
                port = 443,
                mode = Mode.CustomPayload,
                sshUser = "netforge",
                sshPass = "netforge-demo",
                sni = "speedtest.net",
                payloadTemplate = "CONNECT [host_port] HTTP/1.1[crlf]Host: [host][crlf]User-Agent: [ua][crlf]Connection: Upgrade[crlf]Upgrade: websocket[crlf][crlf]",
                mtu = 1500,
                keepalive = 20,
                enableUdp = true
            )
            val directProfile = Profile(
                id = 3L,
                name = "Direct SSH Node",
                author = "NetForge Core",
                note = "Direct TCP SSH socket connection without wrapper",
                host = "1.1.1.1",
                port = 443,
                mode = Mode.SshDirect,
                sshUser = "netforge",
                sshPass = "netforge-demo",
                mtu = 1500,
                keepalive = 10
            )
            saveProfile(defaultProfile)
            saveProfile(nlProfile)
            saveProfile(directProfile)
        }
    }
}
