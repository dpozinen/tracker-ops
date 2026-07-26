FROM debian:bookworm-slim

RUN apt-get update \
 && apt-get install -y --no-install-recommends libicu72 python3 python3-pip ffmpeg curl unzip ca-certificates \
 && pip3 install --break-system-packages yt-dlp \
 && curl -fsSL https://deno.land/install.sh | DENO_INSTALL=/usr/local sh \
 && apt-get purge -y curl unzip && apt-get autoremove -y \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY sockseek /usr/local/bin/sockseek

EXPOSE 5030

ENTRYPOINT ["sockseek"]
