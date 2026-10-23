# UniTV Clone — Android / TV Box

App IPTV completo: canais ao vivo, filmes, séries, animes.
Compatível com qualquer painel **Xtream Codes** e playlists **M3U**.

---

## Build

**Requisitos:**
- Android Studio Hedgehog (2023.1+)
- JDK 17
- Android SDK 34

**Passos:**
1. Abre a pasta `unitvclone/` no Android Studio
2. Aguarda o Gradle sync
3. `Build > Build APK` ou liga um TV Box via ADB e clica Run

**Gerar APK release:**
```bash
./gradlew assembleRelease
# APK em: app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## Primeiro uso

1. Instala o APK no TV Box (ou emulador Android TV)
2. Na tela de ativação, insere:
   - **Servidor:** `http://meuservidor.com:8080`
   - **Usuário** e **Senha** do painel Xtream
3. O app valida, salva as credenciais e carrega o conteúdo

---

## Onde conseguir painel Xtream

- Grupos de IPTV no Telegram (busca "IPTV Brasil revenda")
- Painéis gratuitos para teste: procura "xtream codes free trial"
- Para montar o próprio: instala **XtreamUI** ou **MHD IPTV** num VPS

---

## Estrutura do projeto

```
data/m3u/       → Parser M3U — lê qualquer lista
data/xtream/    → API Xtream Codes + mapeamento de URLs
data/local/     → Room DB (favoritos, histórico) + DataStore
model/          → Channel, Movie, Series, Episode
player/         → ExoPlayerManager (HLS/RTMP/MP4/MKV)
ui/activation/  → Login Xtream
ui/home/        → Home com tabs + grid de conteúdo
ui/player/      → Player fullscreen + controle remoto
```

---

## Funcionalidades

| Feature | Status |
|---|---|
| Canais ao vivo (HLS/RTMP) | ✅ |
| Filmes VOD | ✅ |
| Séries + episódios | ✅ |
| Animes (filtro por categoria) | ✅ |
| Favoritos | ✅ |
| Histórico + retomada de posição | ✅ |
| Controle remoto TV Box (DPAD) | ✅ |
| Cache local (funciona offline) | ✅ |
| Login Xtream Codes | ✅ |
| Parser M3U | ✅ |

---

## Próximos módulos (quando quiser expandir)

- EPG (guia de programação via XML)
- Busca global
- Múltiplos perfis / contas
- Tela de configurações (buffer, codec)
- Catchup TV
