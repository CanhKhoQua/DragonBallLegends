# DragonBallLegends Source

Fan-made educational Java game server source for studying private-server architecture, gameplay systems, database loading, admin UI, events, boss logic, and client/server workflows.

This repository is shared for learning and research purposes only. It is not affiliated with, endorsed by, or sponsored by TeaMobi, Ngoc Rong Online, Dragon Ball, or any related rights holders. Trademarks and third-party assets belong to their respective owners.

## Origin & Credits

This codebase originates from the Ngoc Rong Online private-server community, where it has circulated across forums/Discord servers over time without a single clearly identified original author. This repository is a cleaned-up, sanitized republish (secrets/configs/logs/binaries stripped) for educational sharing — it is not presented as original authorship. If you are the original author of any part of this code and want credit added or content removed, please open an issue.

## Public-Safe Contents

- Java server source in `src/`
- NetBeans/Ant build files
- Local example config in `data/config/config.example.properties`
- Schema-only SQL in `database/schema.sql`
- Optional Zalo integration examples in `settings/*.example.*`

The public version intentionally excludes runtime assets, real configs, cookies, logs, player/account data, built jars, and private NetBeans files.

## Build

Requirements:

- JDK 21
- Apache Ant

Build jar:

```bat
ant clean jar
```

The output jar is generated under `dist/`.

## Local Setup

1. Create a local MariaDB/MySQL database named `nroserver`.
2. Import `database/schema.sql`.
3. Copy `data/config/config.example.properties` to `data/config/config.properties`.
4. Update `database.user`, `database.pass`, host, and ports for your local machine.
5. Add the required runtime data/assets from your own allowed source before running.

Optional API keys can be supplied with environment variables:

```bat
set NRO_API_KEY=CHANGE_ME
set NRO_RECHARGE_WEBHOOK_KEY=CHANGE_ME
```

## Notes

- This source is provided as-is for educational use.
- Do not use it to impersonate official products or services.
- Do not commit real database dumps, player data, cookies, server IPs, passwords, or built binaries.
