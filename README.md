# TP 5 — Requêtes avancées JPA : disponibilité, filtres et pagination

Exemple Maven autonome de recherche de salles avec Hibernate, JPA 2.2 et H2. Il couvre JPQL pour les créneaux libres, Criteria API pour composer des filtres facultatifs, un résultat paginé et des couches repository/service sans EJB.

## Prérequis et commandes

- JDK 11 ou plus récent
- Maven 3.6 ou plus récent

```bash
mvn clean test
mvn exec:java
```

La base H2 est créée en mémoire et son schéma est recréé à chaque exécution.

## Recherche

`SalleSearchCriteria` accepte un fragment de nom, une capacité minimale et un intervalle facultatif. Les deux bornes temporelles doivent être fournies ensemble, et le début doit précéder la fin. Une salle est indisponible si une réservation existante satisfait `reservation.debut < recherche.fin AND reservation.fin > recherche.debut`; deux créneaux consécutifs sont donc compatibles.

`SalleRepositoryImpl` expose :

- `findAvailableRooms(debut, fin)` avec une requête JPQL `NOT EXISTS`.
- `search(criteria, page, size)` avec Criteria API et des paramètres typés.
- `count(criteria)` avec les mêmes filtres de recherche.

Les pages sont numérotées à partir de 1, leur taille doit être positive et le tri est stable par nom puis identifiant. `PageResult` renvoie les éléments, la page courante, la taille, le total d'éléments et le total de pages. La validation des entrées a lieu avant l'exécution JPQL/Criteria.

Le service et le repository sont des classes Java SE et reçoivent leur `EntityManager` explicitement ; les transactions d'écriture restent manuelles.
