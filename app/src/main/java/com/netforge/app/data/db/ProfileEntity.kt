package com.netforge.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.netforge.app.domain.model.*

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val author: String,
    val note: String,
    val host: String,
    val port: Int,
    val mode: Mode,
    val sshUser: String,
    val sshPass: String,
    val sshKey: String,
    val sni: String,
    val frontHost: String,
    val payloadTemplate: String,
    val proxyHost: String,
    val proxyPort: Int,
    val dnsPrimary: String,
    val dnsSecondary: String,
    val customDns: String,
    val mtu: Int,
    val keepalive: Int,
    val enableUdp: Boolean,
    val isFavorite: Boolean,
    val isEncrypted: Boolean,
    val expiryRule: ExpiryRule,
    val limitRule: LimitRule,
    val bindingRule: BindingRule,
    val lockInfo: LockInfo,
    val bannerInfo: BannerInfo,
    val advancedInfo: AdvancedInfo,
    val createdAt: Long,
    val updatedAt: Long,
    val importCount: Int
) {
    fun toDomain(): Profile {
        return Profile(
            id = id,
            name = name,
            author = author,
            note = note,
            host = host,
            port = port,
            mode = mode,
            sshUser = sshUser,
            sshPass = sshPass,
            sshKey = sshKey,
            sni = sni,
            frontHost = frontHost,
            payloadTemplate = payloadTemplate,
            proxyHost = proxyHost,
            proxyPort = proxyPort,
            dnsPrimary = dnsPrimary,
            dnsSecondary = dnsSecondary,
            customDns = customDns,
            mtu = mtu,
            keepalive = keepalive,
            enableUdp = enableUdp,
            isFavorite = isFavorite,
            isEncrypted = isEncrypted,
            expiryRule = expiryRule,
            limitRule = limitRule,
            bindingRule = bindingRule,
            lockInfo = lockInfo,
            bannerInfo = bannerInfo,
            advancedInfo = advancedInfo,
            createdAt = createdAt,
            updatedAt = updatedAt,
            importCount = importCount
        )
    }

    companion object {
        fun fromDomain(p: Profile): ProfileEntity {
            return ProfileEntity(
                id = p.id,
                name = p.name,
                author = p.author,
                note = p.note,
                host = p.host,
                port = p.port,
                mode = p.mode,
                sshUser = p.sshUser,
                sshPass = p.sshPass,
                sshKey = p.sshKey,
                sni = p.sni,
                frontHost = p.frontHost,
                payloadTemplate = p.payloadTemplate,
                proxyHost = p.proxyHost,
                proxyPort = p.proxyPort,
                dnsPrimary = p.dnsPrimary,
                dnsSecondary = p.dnsSecondary,
                customDns = p.customDns,
                mtu = p.mtu,
                keepalive = p.keepalive,
                enableUdp = p.enableUdp,
                isFavorite = p.isFavorite,
                isEncrypted = p.isEncrypted,
                expiryRule = p.expiryRule,
                limitRule = p.limitRule,
                bindingRule = p.bindingRule,
                lockInfo = p.lockInfo,
                bannerInfo = p.bannerInfo,
                advancedInfo = p.advancedInfo,
                createdAt = p.createdAt,
                updatedAt = p.updatedAt,
                importCount = p.importCount
            )
        }
    }
}
