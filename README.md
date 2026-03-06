# FancraftArena

Exemple de plugin PvP Arena pour serveur Minecraft 1.8.8 (PaperSpigot).

Ce projet sert de base/exemple pour apprendre a developper un plugin Bukkit avec un systeme de kits, un classement et une base de donnees.

## Fonctionnalites

### Systeme de Kits
Menu interactif avec `/kit` pour choisir parmi 4 kits :

| Kit | Arme | Armure | Golden Apples |
|-----|------|--------|---------------|
| **Guerrier** | Epee en fer | Fer complete | 3 |
| **Archer** | Arc Power I + epee en pierre + 64 fleches | Chaine complete | 2 |
| **Tank** | Epee en pierre | Diamant complete | 5 |
| **Berserker** | Epee en diamant Tranchant II | Cuir complete | 1 |

### Classement PvP
`/classement` affiche le Top 10 des joueurs tries par kills, avec ta position personnelle.

### Scoreboard
Scoreboard en temps reel avec kills, morts, ratio K/D et rang.

### Messages personnalises
Messages de kill et de mort contextuels en francais (vide, lave, chute, noyade, etc.).

### Base de donnees
Stats stockees en SQLite de maniere asynchrone.

## Installation

1. Telecharge `FancraftArena-2.0.jar` depuis le dossier [target](target/)
2. Place-le dans `plugins/` d'un serveur PaperSpigot 1.8.8
3. Redemarre le serveur

## Commandes

| Commande | Description |
|----------|-------------|
| `/kit` | Ouvre le menu de selection de kit |
| `/classement` | Affiche le top 10 PvP |

## Compilation

Java 8 + Maven :

```bash
mvn clean package
```

Le jar se trouve dans `target/FancraftArena-2.0.jar`.
