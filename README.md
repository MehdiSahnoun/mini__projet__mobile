# GradeBook - Application Android de Gestion des Notes

**GradeBook** est une application Android native complète développée en **Kotlin** avec **Jetpack Compose**, **Material 3**, et alimentée en temps réel par **Firebase Authentication** et **Cloud Firestore**.

Elle permet à un enseignant de gérer la liste des étudiants, les matières et de saisir leurs notes, avec calcul automatique des moyennes pondérées, calcul des mentions et affichage de statistiques de classe. Les étudiants peuvent s'inscrire et consulter leur propre relevé de notes en toute sécurité.

---

## 🚀 Fonctionnalités Principales

### 👨‍🏫 Espace Enseignant (`role = ENSEIGNANT`)
1. **Gestion des Étudiants** : Ajout, modification, recherche par nom/prénom/classe, suppression avec suppression en cascade de leurs notes.
2. **Gestion des Matières** : Ajout, modification de la matière et de son coefficient, suppression en cascade.
3. **Saisie des Notes** : Sélection dynamique de l'étudiant, de la matière, du type d'évaluation (**DS**, **EXAMEN**, **TP**), contrôle de la valeur (0 ≤ note ≤ 20), modification et suppression.
4. **Relevé de Notes & Statistiques** : Consultation du relevé de n'importe quel étudiant, moyenne générale pondérée, mention (**Admis** / **Ajourné**), moyenne de la classe, meilleure et plus faible note.

### 🎓 Espace Étudiant (`role = ETUDIANT`)
1. **Consultation du Relevé Personnel** : Accès direct et sécurisé à son propre relevé de notes sans accès possible aux données des autres étudiants ni aux fonctionnalités d'édition Enseignant.
2. **Consultation des Statistiques de Classe** : Visualisation de la moyenne globale de classe, meilleure note et plus faible note.
3. **Profil Personnel** : Mise à jour du nom, prénom et changement de mot de passe.

---

## 🛠 Stack Technique

- **Langage** : Kotlin
- **Interface Utilisateur** : Jetpack Compose + Material 3
- **Architecture** : MVVM + Repository Pattern + StateFlow + Coroutines
- **Navigation** : Navigation Compose
- **Backend & Data** : Firebase Authentication (Email/Password) + Cloud Firestore
- **Min SDK** : 24 (Android 7.0) | **Target SDK** : 34

---

## 📋 Instructions d'Installation & Configuration Firebase

### 1. Cloner et ouvrir le projet dans Android Studio
- Ouvrir le dossier du projet dans **Android Studio**.

### 2. Configurer Firebase Console
1. Rendez-vous sur la [Console Firebase](https://console.firebase.google.com/).
2. Créez un nouveau projet nommé **GradeBook**.
3. Ajoutez une application Android avec le nom de package : `com.example.gradebook`.
4. Téléchargez le fichier `google-services.json` généré.
5. Remplacez le fichier exemple `app/google-services.json` du projet par votre fichier `google-services.json` téléchargé.

### 3. Activer Firebase Authentication
1. Dans la console Firebase, allez dans **Build > Authentication**.
2. Cliquez sur **Get Started** / **Commencer**.
3. Activez le fournisseur d'accès **Email / Mot de passe**.

### 4. Activer Cloud Firestore
1. Dans la console Firebase, allez dans **Build > Firestore Database**.
2. Cliquez sur **Create database** / **Créer une base de données**.
3. Dans l'onglet **Règles (Rules)**, remplacez le contenu par le fichier `firestore.rules` fourni à la racine du projet.

### 5. Synchroniser et Lancer
1. Dans Android Studio, cliquez sur **Sync Project with Gradle Files**.
2. Lancez l'application sur un émulateur Android ou un appareil physique.

---

## 🧪 Jeu de Données de Test (Script & Instructions)

Pour tester immédiatement l'application avec des données réelles, vous pouvez soit créer un compte enseignant depuis l'écran d'inscription, soit saisir manuellement les documents suivants dans la console Firestore :

### 1. Matières (Collection `matieres`)
| Document ID (auto) | `nom` | `coefficient` (number) |
|---|---|---|
| `mat_math` | Mathématiques | `3.0` |
| `mat_phys` | Physique-Chimie | `2.0` |
| `mat_info` | Informatique & Algo | `3.0` |
| `mat_ang` | Anglais Technique | `1.0` |
| `mat_mgt` | Management de Projet | `1.0` |

### 2. Étudiants (Collection `etudiants`)
| Document ID (auto) | `nom` | `prenom` | `numeroEtudiant` | `classe` | `uid` |
|---|---|---|---|---|---|
| `etud_1` | Benali | Youssef | `ET202401` | INFO-3A | *null* |
| `etud_2` | Mansouri | Sarah | `ET202402` | INFO-3A | *null* |
| `etud_3` | Alami | Karim | `ET202403` | INFO-3A | *null* |
| `etud_4` | Trabelsi | Amira | `ET202404` | INFO-3B | *null* |
| `etud_5` | Haddad | Omar | `ET202405` | INFO-3B | *null* |

### 3. Exemples de Notes (Collection `notes`)
| Document ID | `etudiantId` | `matiereId` | `type` | `valeur` (number) | `date` (Timestamp) |
|---|---|---|---|---|---|
| `note_1` | `etud_1` | `mat_math` | `DS` | `14.0` | *now* |
| `note_2` | `etud_1` | `mat_math` | `EXAMEN` | `16.0` | *now* |
| `note_3` | `etud_1` | `mat_info` | `TP` | `18.5` | *now* |
| `note_4` | `etud_2` | `mat_math` | `DS` | `8.0` | *now* |
| `note_5` | `etud_2` | `mat_phys` | `EXAMEN` | `9.5` | *now* |

---

## 🏛 Structure du Code Source

```
com.example.gradebook/
├── data/
│   ├── model/ (Role, TypeNote, User, Etudiant, Matiere, Note)
│   ├── repository/ (AuthRepository, EtudiantRepository, MatiereRepository, NoteRepository)
│   └── firebase/ (FirebaseRefs.kt)
├── ui/
│   ├── auth/ (LoginScreen, RegisterScreen, AuthViewModel)
│   ├── main/ (MainScreen, MainViewModel)
│   ├── etudiants/ (EtudiantsScreen, AjoutEtudiantScreen, EtudiantViewModel)
│   ├── matieres/ (MatieresScreen, MatiereViewModel)
│   ├── notes/ (NotesScreen, NoteViewModel)
│   ├── releve/ (ReleveScreen, ReleveViewModel)
│   ├── profil/ (ProfilScreen, ProfilViewModel)
│   └── theme/ (Color.kt, Type.kt, Theme.kt)
├── navigation/ (Routes.kt, AppNavGraph.kt)
├── util/ (MoyenneCalculator.kt, Validators.kt)
└── MainActivity.kt
```
