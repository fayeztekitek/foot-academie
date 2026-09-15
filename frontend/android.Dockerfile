FROM node:20-alpine AS web-builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npm run build

FROM eclipse-temurin:17-jdk AS android-builder

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"
ENV JAVA_HOME=/opt/java/openjdk

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget unzip curl ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdtools.zip && \
    unzip -q /tmp/cmdtools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdtools.zip

RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} --licenses > /dev/null 2>&1 && \
    sdkmanager --sdk_root=${ANDROID_HOME} \
      "platform-tools" \
      "platforms;android-34" \
      "build-tools;34.0.0"

WORKDIR /app
COPY --from=web-builder /app/dist ./dist
COPY --from=web-builder /app/package.json ./
COPY --from=web-builder /app/package-lock.json ./
COPY capacitor.config.json ./
COPY src ./src

RUN npm install
RUN npm install -D typescript
RUN npx cap add android || true
RUN npx cap sync android

WORKDIR /app/android
RUN chmod +x gradlew && ./gradlew assembleDebug --no-daemon

FROM alpine:latest AS output
COPY --from=android-builder /app/android/app/build/outputs/apk/debug/app-debug.apk /app-debug.apk
CMD ["cp", "/app-debug.apk", "/output/nadi.apk"]
