FROM debian:bookworm-slim

RUN apt-get update \
 && apt-get install -y --no-install-recommends libicu72 \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY sockseek /usr/local/bin/sockseek

EXPOSE 5030

ENTRYPOINT ["sockseek"]
