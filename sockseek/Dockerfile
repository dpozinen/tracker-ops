FROM alpine:3.20

ARG VERSION=3.0.4
ARG ARCH=linux-x64

RUN apk add --no-cache libstdc++ libgcc icu-libs wget ca-certificates \
 && wget -qO /tmp/sockseek.tar.gz \
      "https://github.com/fiso64/sockseek/releases/download/v${VERSION}/sockseek_${VERSION}_${ARCH}.tar.gz" \
 && tar -xzf /tmp/sockseek.tar.gz -C /usr/local/bin \
 && chmod +x /usr/local/bin/sockseek \
 && rm /tmp/sockseek.tar.gz

EXPOSE 5030

ENTRYPOINT ["sockseek"]
