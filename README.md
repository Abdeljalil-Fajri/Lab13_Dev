# PinPoint

Application Android de géolocalisation en temps réel, développée en Java. PinPoint enregistre automatiquement la position GPS de l'appareil et la transmet à un serveur distant, puis permet de visualiser l'ensemble des positions enregistrées sur une carte interactive OpenStreetMap.

---

## Table des matières

- [Aperçu fonctionnel](#aperçu-fonctionnel)
- [Architecture du projet](#architecture-du-projet)
- [Technologies utilisées](#technologies-utilisées)
- [Prérequis](#prérequis)
- [Installation et configuration](#installation-et-configuration)
- [Structure des fichiers](#structure-des-fichiers)
- [Description des composants](#description-des-composants)
- [Base de données](#base-de-données)
- [API Backend](#api-backend)

---

## Aperçu fonctionnel

PinPoint fonctionne en deux temps :

1. L'application écoute en continu les mises à jour GPS. Dès qu'un déplacement d'au moins 150 mètres est détecté (ou après 60 secondes), les nouvelles coordonnées sont envoyées automatiquement au serveur PHP via une requête HTTP POST.

2. L'utilisateur peut à tout moment appuyer sur le bouton "View Pinned Locations" pour ouvrir la carte et visualiser tous les points enregistrés, chacun matérialisé par un marqueur personnalisé.

L'interface principale affiche en temps réel la latitude, la longitude, l'altitude, la précision du signal GPS, le statut de la connexion, l'identifiant de l'appareil ainsi qu'un journal d'activité horodaté.

---

## Architecture du projet

```
Client Android (Java)
        |
        | HTTP POST (Volley)
        v
Serveur local XAMPP (PHP)
        |
        | PDO
        v
Base de données MySQL (map_project)
```

Le client Android communique exclusivement avec le serveur via deux endpoints PHP. Toutes les données sont stockées en MySQL et restituées au format JSON.

---

## Technologies utilisées

- Java (Android SDK)
- OSMDroid — affichage de cartes OpenStreetMap
- Volley — gestion des requêtes HTTP asynchrones
- PHP avec PDO — backend REST minimal
- MySQL — stockage des positions
- XAMPP — serveur local de développement

---

## Prérequis

- Android Studio (version récente recommandée)
- Un émulateur Android ou un appareil physique sous Android 6.0 minimum
- XAMPP (ou WAMP) installé et démarré avec Apache et MySQL actifs
- PHP 7.4 ou supérieur

---

## Installation et configuration

### 1. Backend PHP

Placez les deux fichiers PHP dans le répertoire suivant :

```
C:/xampp/htdocs/map_project/
```

Ouvrez phpMyAdmin, créez une base de données nommée `map_project` puis exécutez le script SQL fourni dans la section [Base de données](#base-de-données) pour créer la table `positions`.

### 2. Application Android

Ouvrez le projet dans Android Studio. Dans le fichier `MainActivity.java`, vérifiez que la constante `INSERT_URL` pointe bien vers votre serveur :

```java
private static final String INSERT_URL =
        "http://10.0.2.2/map_project/createPosition.php";
```

L'adresse `10.0.2.2` est l'alias interne de l'émulateur Android pour accéder à `localhost` sur la machine hôte. Si vous testez sur un appareil physique, remplacez cette adresse par l'IP locale de votre machine (par exemple `192.168.1.x`).

Faites de même dans `MapActivity.java` pour la constante `GET_URL`.

### 3. Lancer l'application

Démarrez l'émulateur ou connectez votre appareil, puis exécutez le projet depuis Android Studio. Accordez les permissions de localisation lorsque la boîte de dialogue apparaît. Pour simuler un déplacement GPS sur l'émulateur, utilisez le panneau "Extended Controls" d'Android Studio (icône des trois points dans la barre latérale de l'émulateur), puis l'onglet "Location".

---

## Structure des fichiers

```
PinPoint/
├── app/src/main/
│   ├── java/com/example/pinpoint/
│   │   ├── MainActivity.java         # Activité principale, GPS et envoi des données
│   │   └── MapActivity.java          # Activité carte, chargement et affichage des pins
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml     # Interface principale (cards, log, status)
│   │   │   └── activity_map.xml      # Interface carte (header + MapView)
│   │   ├── drawable/
│   │   │   └── ic_pin.xml            # Icône de marqueur (vecteur SVG)
│   │   └── values/
│   │       ├── colors.xml            # Palette de couleurs de l'application
│   │       └── strings.xml           # Ressources texte et messages
│   └── AndroidManifest.xml           # Permissions et déclaration des activités
└── htdocs/map_project/               # (serveur XAMPP)
    ├── createPosition.php            # Endpoint POST : enregistrement d'une position
    └── getPosition.php               # Endpoint GET : récupération de toutes les positions
```

---

## Description des composants

### MainActivity.java

Activité de démarrage de l'application. Elle gère les responsabilités suivantes :

- Demande des permissions de localisation au runtime (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION) conformément aux exigences Android 6.0 et supérieur.
- Initialisation du `LocationManager` et abonnement aux mises à jour GPS via `requestLocationUpdates`, avec un intervalle minimum de 60 secondes et une distance minimum de 150 mètres.
- Mise à jour de l'interface en temps réel : latitude, longitude, altitude, précision, statut, horodatage de la dernière mise à jour.
- Envoi des coordonnées au serveur via une `StringRequest` Volley en HTTP POST.
- Maintien d'un journal d'activité horodaté affiché dans l'interface.
- Identification de l'appareil via `Settings.Secure.ANDROID_ID`, qui ne requiert aucune permission supplémentaire.

### MapActivity.java

Activité dédiée à la visualisation des positions enregistrées. Elle gère :

- L'initialisation et la configuration de la carte OSMDroid (source de tuiles MAPNIK, zoom initial sur Casablanca, contrôles multi-touch).
- Le chargement des positions depuis le serveur via une `JsonObjectRequest` Volley en HTTP GET.
- La création dynamique d'un marqueur OSMDroid pour chaque position reçue, avec titre, extrait (date et identifiant appareil) et icône vectorielle personnalisée.
- La navigation automatique vers la position la plus récente après chargement.
- La gestion correcte du cycle de vie de la carte (`onResume` / `onPause`).

### activity_main.xml

Interface construite avec un `ScrollView` contenant un `LinearLayout` vertical. Elle se compose de quatre zones :

- Un en-tête coloré avec le nom de l'application et son sous-titre.
- Une carte (CardView) affichant les quatre valeurs GPS en temps réel.
- Une carte de statut avec le message courant, l'identifiant appareil et la date de dernière mise à jour.
- Une carte de journal d'activité affichant les événements récents en police monospace.

### activity_map.xml

Interface minimaliste composée d'un en-tête affichant le titre et le nombre de pins chargés, suivi d'un `MapView` OSMDroid en plein écran.

### ic_pin.xml

Drawable vectoriel représentant une icône de localisation (forme de goutte inversée avec cercle intérieur), utilisée comme marqueur sur la carte. Couleur principale : violet foncé (#4A148C).

### colors.xml

Définit la palette complète de l'application : couleur primaire (violet #4A148C), couleur sombre (#12005E), accent (lilas #EA80FC), couleurs sémantiques pour les états succès, erreur et avertissement.

### strings.xml

Contient toutes les chaînes de caractères de l'application, dont le format du message de nouvelle position (avec placeholders pour latitude, longitude, altitude et précision) et les messages d'état des fournisseurs GPS.

### AndroidManifest.xml

Déclare les permissions requises :

- `ACCESS_FINE_LOCATION` et `ACCESS_COARSE_LOCATION` — accès à la localisation GPS
- `INTERNET` et `ACCESS_NETWORK_STATE` — communication réseau
- `WRITE_EXTERNAL_STORAGE` — cache des tuiles OSMDroid

Active également `android:usesCleartextTraffic="true"` pour autoriser les requêtes HTTP non chiffrées vers le serveur local de développement.

---

## Base de données

Exécutez le script suivant dans phpMyAdmin :

```sql
CREATE DATABASE IF NOT EXISTS map_project
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE map_project;

CREATE TABLE `positions` (
    `id`        INT(11)      NOT NULL AUTO_INCREMENT,
    `latitude`  DOUBLE       NOT NULL,
    `longitude` DOUBLE       NOT NULL,
    `date`      DATETIME     NOT NULL,
    `imei`      VARCHAR(50)  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

La colonne `imei` contient en réalité l'`ANDROID_ID` de l'appareil. Le nom de la colonne a été conservé tel quel pour la compatibilité avec le backend existant.

---

## API Backend

### POST /map_project/createPosition.php

Enregistre une nouvelle position dans la base de données.

Paramètres POST attendus :

| Paramètre   | Type   | Description                        |
|-------------|--------|------------------------------------|
| latitude    | double | Latitude en degrés décimaux        |
| longitude   | double | Longitude en degrés décimaux       |
| date        | string | Horodatage au format yyyy-MM-dd HH:mm:ss |
| imei        | string | Identifiant unique de l'appareil   |

Réponse JSON en cas de succès :

```json
{
  "success": true,
  "message": "Position saved",
  "latitude": 33.5731,
  "longitude": -7.5898
}
```

### GET /map_project/getPosition.php

Retourne toutes les positions enregistrées, triées par date décroissante.

Réponse JSON :

```json
{
  "success": true,
  "count": 3,
  "positions": [
    {
      "id": "3",
      "latitude": "33.5731",
      "longitude": "-7.5898",
      "date": "2025-05-30 14:22:00",
      "imei": "a1b2c3d4e5f6g7h8"
    }
  ]
}
```

---

## Notes de développement

- L'URL `10.0.2.2` est spécifique à l'émulateur Android. Sur appareil physique, remplacez-la par l'IP locale de la machine hébergeant XAMPP.
- `usesCleartextTraffic` est activé uniquement pour le développement local. En production, passez à HTTPS et retirez cette option.
- L'intervalle GPS de 60 secondes et la distance de 150 mètres sont des valeurs de compromis entre précision et autonomie de la batterie. Ajustez-les selon le cas d'usage.



