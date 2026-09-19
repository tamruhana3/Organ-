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
                name = "Axiom Gateway 01",
                author = "Crafted by Axiom Collective",
                note = "Primary secure wrapped TLS route with keepalive verification",
                host = "node-us-east.netforge.internal",
                port = 443,
                mode = Mode.Wrapped,
                sshUser = "netforge",
                sshPass = "netforge-demo",
                sni = "cdn.example.com",
                frontHost = "cdn.example.com",
                dnsPrimary = "1.1.1.1",
                dnsSecondary = "1.0.0.1",
                mtu = 1500,
                keepalive = 15,
                enableUdp = true,
                isFavorite = true
            )
            val directProfile = Profile(
                id = 2L,
                name = "Direct TCP Tunnel",
                author = "Axiom Collective",
                note = "Raw direct TCP socket forwarding without TLS envelope",
                host = "node-de-fra.netforge.internal",
                port = 22,
                mode = Mode.Direct,
                sshUser = "netforge",
                sshPass = "netforge-demo",
                mtu = 1500,
                keepalive = 20,
                enableUdp = true
            )
            val liveProfile = Profile(
                id = 3L,
                name = "Live WebSocket Stream",
                author = "Axiom Collective",
                note = "HTTP Upgrade websocket transport with SSH frames",
                host = "node-uk-lon.netforge.internal",
                port = 443,
                mode = Mode.Live,
                sshUser = "netforge",
                sshPass = "netforge-demo",
                sni = "stream.netforge.internal",
                frontHost = "stream.netforge.internal",
                mtu = 1420,
                keepalive = 10
            )
            saveProfile(defaultProfile)
            saveProfile(directProfile)
            saveProfile(liveProfile)
        }
    }
}
