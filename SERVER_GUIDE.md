# NetForge Server Backend Guide

A complete, production-grade guide to deploying a multi-transport NetForge VPN server on **Ubuntu 22.04 LTS (Jammy Jellyfish)**.

NetForge supports five distinct encapsulation modes:
1. **Direct** — Raw SSH over TCP on a custom port
2. **Wrapped** — SSH wrapped inside TLS (stunnel4) on port 443 with SNI masking
3. **WrappedPlus** — TLS wrap + HTTP CONNECT proxy (Squid/3proxy) + payload injection
4. **Slow** — Covert DNS tunnel (dnstt / iodine) over UDP port 53
5. **Live** — WebSocket tunnel over TLS (Nginx + wssh / websockify)

---

## 1. Automated Quick Setup Script

For rapid deployment, run this automated installation script as `root`:

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "=== NetForge Server Setup (Ubuntu 22.04 LTS) ==="

# Update package repository
apt-get update && apt-get upgrade -y
apt-get install -y openssh-server stunnel4 squid ufw fail2ban certbot nginx dnstt-server curl git

# 1. Create dedicated tunnel user
useradd -m -s /bin/false netforge_user
echo "netforge_user:SetYourStrongPasswordHere" | chpasswd

# 2. Configure OpenSSH for Tunneling
cat << 'EOF' > /etc/ssh/sshd_config.d/netforge.conf
Port 2222
PermitTunnel yes
AllowTcpForwarding yes
X11Forwarding no
GatewayPorts no
PasswordAuthentication yes
ClientAliveInterval 15
ClientAliveCountMax 3
EOF

systemctl restart ssh

# 3. Configure stunnel (Wrapped Mode - Port 443 -> 2222)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/stunnel/netforge.key \
  -out /etc/stunnel/netforge.crt \
  -subj "/C=US/ST=State/L=City/O=NetForge/CN=yourdomain.com"

cat << 'EOF' > /etc/stunnel/netforge.conf
cert = /etc/stunnel/netforge.crt
key = /etc/stunnel/netforge.key
pid = /var/run/stunnel.pid

[ssh-tls]
accept = 0.0.0.0:443
connect = 127.0.0.1:2222
EOF

sed -i 's/ENABLED=0/ENABLED=1/' /etc/default/stunnel4
systemctl restart stunnel4

# 4. Configure Squid Proxy (WrappedPlus Mode - Port 8080)
cat << 'EOF' > /etc/squid/squid.conf
http_port 8080
acl Safe_ports port 22 2222 443 80
http_access allow localhost
http_access allow all
forwarded_for off
via off
EOF

systemctl restart squid

# 5. Configure Firewall (UFW)
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment 'SSH Management'
ufw allow 2222/tcp comment 'NetForge Direct TCP'
ufw allow 443/tcp comment 'NetForge Wrapped TLS'
ufw allow 8080/tcp comment 'NetForge Squid Proxy'
ufw allow 53/udp comment 'NetForge Slow DNS'
ufw --force enable

echo "=== NetForge Server Setup Completed Successfully ==="
```

---

## 2. Protocol Details & Architecture

### Mode 1: Direct (Raw TCP)
- **Client behavior:** Opens direct TCP socket to `host:2222`.
- **Handshake:** SSH-2.0 banner exchange followed by key agreement (Curve25519/RSA), user authentication, and dynamic SOCKS5 forwarder (`DirectTunnel`).

### Mode 2: Wrapped (TLS over SSH)
- **Client behavior:** Initiates TLS 1.3/1.2 handshake on port 443 with SNI (`cdn.cloudflare.com` or your domain).
- **Server behavior:** `stunnel4` terminates TLS and proxies cleartext stream to local `127.0.0.1:2222`.
- **Evasion characteristics:** Traffic appears identical to standard HTTPS web browsing.

### Mode 3: WrappedPlus (TLS + Custom HTTP Injector)
- **Client behavior:** Connects over TLS to proxy port 8080, injects formatted HTTP `CONNECT [host_port] HTTP/1.1` payload with custom headers, and switches to raw byte stream upon receiving `HTTP/1.1 200 OK`.

### Mode 4: Slow (DNS Tunneling)
- **Server configuration:** Run `dnstt-server`:
  ```bash
  dnstt-server -gen-key -privkey-file dnstt.key -pubkey-file dnstt.pub
  dnstt-server -udp :53 -privkey-file dnstt.key t.yourdomain.com 127.0.0.1:2222
  ```
- **Client behavior:** Encapsulates frames inside DNS queries (`TXT` / `NULL`) sent to recursive resolvers (`1.1.1.1` or `8.8.8.8`).

### Mode 5: Live (WebSocket + TLS)
- **Server configuration (Nginx reverse proxy):**
  ```nginx
  server {
      listen 443 ssl http2;
      server_name yourdomain.com;

      ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
      ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

      location /netforge-live {
          proxy_pass http://127.0.0.1:2222;
          proxy_http_version 1.1;
          proxy_set_header Upgrade $http_upgrade;
          proxy_set_header Connection "Upgrade";
          proxy_read_timeout 86400s;
      }
  }
  ```

---

## 3. Creating & Distributing `.nfg` Profiles

1. In the **NetForge Android App**, create or edit a profile with your server IP and credentials.
2. Open **Export Flow** from the menu.
3. Enter an encryption passphrase (minimum 8 characters; 16+ recommended for `Fortified` rating).
4. Select desired protection policies (Read-only lock, device binding token, expiry duration, root prevention).
5. Tap **Generate Sealed .nfg** and share the resulting file with clients.
