<p align="center"><img src="assets/icon.png" width="128" height="128" alt="Tick Freeze Exit icon"></p>

<h1 align="center">Tick Freeze Exit</h1>

<p align="center">Server-only NeoForge 1.21.1 mod that halts a frozen or half-stopped server JVM with exit code 1, so the hosting panel restarts it instead of leaving it hung.</p>

<p align="center">
  <a href="https://github.com/milkycloud-dev/tick-freeze-exit/actions/workflows/build.yml"><img src="https://github.com/milkycloud-dev/tick-freeze-exit/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <a href="https://github.com/milkycloud-dev/tick-freeze-exit/releases/latest"><img src="https://img.shields.io/github/v/release/milkycloud-dev/tick-freeze-exit" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-proprietary-lightgrey" alt="License: proprietary"></a>
</p>

<p align="center"><a href="#english">English</a> | <a href="#русский">Русский</a></p>

<a id="english"></a>

## English

### Overview

Hosting panels such as Pterodactyl restart a Minecraft server when its process exits. They do nothing while the process is alive, even when the game inside is dead. Two cases leave a server like that:

- **Frozen server thread.** A mod loops forever inside a tick. The Spigot/Paper watchdog dumps threads, then starts a shutdown on its own thread, which waits on the frozen server thread and never finishes. In the case that led to this mod, the process stayed in that state for 45 minutes, with players unable to join, until someone killed it by hand.
- **Stopped server, live JVM.** The shutdown fails half way, for example out of memory while saving, and background threads of plugins and the core keep the JVM running.

The mod starts a small daemon thread when the server has started. Every 5 seconds it looks at the server thread and the tick counter:

1. If the tick counter has not moved for half the freeze limit, it logs a warning.
2. If it has not moved for the full limit (300 seconds by default), it logs an error and calls `Runtime.halt(1)`.
3. If the server thread has ended and the JVM is still alive after 90 seconds, it does the same.

Exit code 1 looks like a crash to the panel, which restarts the server. World data since the last autosave is lost, exactly as with a manual kill, but the server is back in minutes instead of whenever someone notices.

A normal stop ends the JVM within seconds and never reaches the mod.

### Requirements

- Minecraft 1.21.1, dedicated server
- NeoForge 21.1 or newer, or a NeoForge-based hybrid core (Youer, Mohist)
- Java 21
- A host that restarts the server when the process exits (panel, systemd with `Restart=`, a start script loop)

### Installation

Download `tick-freeze-exit-<version>.jar` from [Releases](https://github.com/milkycloud-dev/tick-freeze-exit/releases) and put it into the server's `mods/` folder. Clients do not need it; on a client it does nothing.

### Configuration

Optional JVM flags:

- `-Dtickfreezeexit.freezeSeconds=<seconds>`: how long the tick counter may stand still, default 300.
- `-Dtickfreezeexit.deadSeconds=<seconds>`: how long the JVM may outlive the server thread, default 90.

Keep `freezeSeconds` well above your longest legitimate stall, such as a large world save.

### Testing

Checked on an isolated Youer 1.21.1 server with a helper command that blocks the server thread for good: with `freezeSeconds=30` the server stopped answering, the warning and the error were logged, and the JVM exited with code 1 thirty seconds after the last tick.

### Releases

Every tag `v*` is built by GitHub Actions from this source and published in [Releases](../../releases). The build log of each release is public, so every jar can be traced to its commit.

### Known limitations

- It restarts the server, it does not fix what froze it. Read the watchdog dump in the log before the exit.
- Unsaved world changes since the last autosave are lost, as with any kill.
- Built and tested for 1.21.1 only.

### License

Proprietary, all rights reserved. Official release binaries may be run unmodified on servers you operate. Copying, modifying or redistributing the code or the binaries requires written permission. Full terms: [LICENSE](LICENSE).

<a id="русский"></a>

## Русский

### Обзор

Панели хостинга вроде Pterodactyl перезапускают сервер Minecraft, когда его процесс завершается. Пока процесс жив, они ничего не делают, даже если игра внутри мертва. До такого состояния сервер доводят два случая:

- **Завис серверный поток.** Мод бесконечно крутится внутри тика. Сторож Spigot/Paper печатает дампы потоков, затем запускает остановку в своём потоке, а она ждёт зависший серверный поток и не заканчивается. В случае, из-за которого появился мод, процесс провёл так 45 минут: игроки не могли зайти, пока его не убили вручную.
- **Сервер остановлен, JVM жива.** Остановка обрывается на середине, например из-за нехватки памяти при сохранении, а фоновые потоки плагинов и ядра держат JVM.

После запуска сервера мод заводит небольшой фоновый поток. Каждые 5 секунд он смотрит на серверный поток и счётчик тиков:

1. Если счётчик стоит половину предела, в лог пишется предупреждение.
2. Если стоит весь предел (по умолчанию 300 секунд), пишется ошибка и вызывается `Runtime.halt(1)`.
3. Если серверный поток завершился, а JVM жива ещё 90 секунд, происходит то же самое.

Код выхода 1 панель видит как падение и перезапускает сервер. Изменения мира после последнего автосохранения теряются, ровно как при ручном убийстве процесса, зато сервер возвращается через минуты, а не тогда, когда кто-то заметит.

Обычная остановка завершает JVM за секунды и до мода не доходит.

### Требования

- Minecraft 1.21.1, выделенный сервер
- NeoForge 21.1 и новее или гибридное ядро на NeoForge (Youer, Mohist)
- Java 21
- Хостинг, который перезапускает сервер при выходе процесса (панель, systemd с `Restart=`, цикл в скрипте запуска)

### Установка

Скачать `tick-freeze-exit-<версия>.jar` из [Releases](https://github.com/milkycloud-dev/tick-freeze-exit/releases) и положить в папку `mods/` сервера. Клиентам он не нужен; на клиенте он ничего не делает.

### Настройка

Необязательные флаги JVM:

- `-Dtickfreezeexit.freezeSeconds=<секунды>`: сколько счётчик тиков может стоять, по умолчанию 300.
- `-Dtickfreezeexit.deadSeconds=<секунды>`: сколько JVM может жить после серверного потока, по умолчанию 90.

`freezeSeconds` стоит держать заметно выше самой долгой законной паузы, например сохранения большого мира.

### Проверка

Проверено на отдельном сервере Youer 1.21.1 с вспомогательной командой, которая навсегда блокирует серверный поток: при `freezeSeconds=30` сервер перестал отвечать, в лог попали предупреждение и ошибка, JVM вышла с кодом 1 через тридцать секунд после последнего тика.

### Релизы

Каждый тег `v*` собирается из этих исходников в GitHub Actions и публикуется в [Releases](../../releases). Лог сборки каждого релиза открыт, так что любой jar можно сверить с его коммитом.

### Ограничения

- Мод перезапускает сервер, а не чинит то, что его повесило. Причину смотреть в дампе сторожа в логе перед выходом.
- Несохранённые изменения мира после последнего автосохранения теряются, как при любом убийстве процесса.
- Собрано и проверено только для 1.21.1.

### Лицензия

Проприетарная, все права защищены. Официальные сборки из релизов можно запускать без изменений на своих серверах. Копировать, изменять и распространять код или сборки можно только с письменного разрешения. Полный текст: [LICENSE](LICENSE).
