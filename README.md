# FancraftArena

Plugin PvP Arena pour serveur Minecraft 1.8.8 (PaperSpigot).

## Fonctionnalites

### Systeme de Kits
Choisis ton kit avec `/kit` via un menu interactif. 4 kits disponibles :

| Kit | Arme | Armure | Golden Apples |
|-----|------|--------|---------------|
| **Guerrier** | Epee en fer | Fer complete | 3 |
| **Archer** | Arc Power I + epee en pierre + 64 fleches | Chaine complete | 2 |
| **Tank** | Epee en pierre | Diamant complete | 5 |
| **Berserker** | Epee en diamant Tranchant II | Cuir complete | 1 |

### Classement PvP
Tape `/classement` pour afficher le Top 10 des joueurs tries par kills, avec ta position personnelle.

### Scoreboard
Un scoreboard en temps reel affiche pour chaque joueur :
- Kills / Morts / Ratio K/D
- Rang dans le classement

### Messages personnalises
- Messages de kill en francais (avec detection des projectiles)
- Messages de mort contextuels (vide, lave, chute, noyade, etc.)
- Message de bienvenue a la connexion

### Base de donnees
Les stats sont stockees en SQLite (kills, morts, ratio). Tout est asynchrone pour ne pas lag le serveur.

## Installation

1. Telecharge le fichier `FancraftArena-2.0.jar` depuis le dossier [target](target/)
2. Place-le dans le dossier `plugins/` de ton serveur PaperSpigot 1.8.8
3. Redemarre le serveur

## Commandes

| Commande | Description |
|----------|-------------|
| `/kit` | Ouvre le menu de selection de kit |
| `/classement` | Affiche le top 10 PvP |

## Compilation

Requires Java 8 et Maven.

```bash
mvn clean package
```

Le jar se trouve dans `target/FancraftArena-2.0.jar`.

## Auteur

**lasalade_**
