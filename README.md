# Minestom Base
A base for Minestom servers, providing useful utilities and features to speed up development.

## TODO
- [ ] NPCs via juliarn/npc-lib
- [ ] Map displays via image files
- [ ] ktor-like dsl for creating Minecraft servers

## Setup project
### Windows
```shell
mkdir run/
./gradlew.bat build
```

### Linux/macOS
```shell
make setup
```
## Reinit project
### Windows
```shell
rm -rf run/
mkdir run/
./gradlew.bat build
```
### Linux/macOS
```shell
make reinit
```