# 📚 GUIDE COMPLET - ARCHITECTURE PRODUCT & PRODUCTVARIANT

## 🎯 RÉSUMÉ EXÉCUTIF

Cette architecture Spring Boot REST API implémente un système de gestion de produits avec variantes et stock, respectant les meilleures pratiques :

- ✅ **Entités JPA corrigées** (setters, builders, validation métier)
- ✅ **DTOs avec Bean Validation** (fail-fast, messages clairs)
- ✅ **Mappers optimisés** (modification en place, pas de perte d'ID)
- ✅ **Services transactionnels** (ACID, dirty checking)
- ✅ **Repositories Spring Data** (query methods, custom queries)
- ✅ **Controllers REST** (CRUD complet, gestion stock, pagination)
- ✅ **Gestion d'erreurs centralisée** (@RestControllerAdvice)

---

## 🏗️ ARCHITECTURE EN COUCHES

```
┌─────────────────────────────────────────────────────┐
│                   CONTROLLERS                       │
│  ProductController, ProductVariantController        │
│  - Endpoints REST                                   │
│  - Validation @Valid                                │
│  - ResponseEntity avec status HTTP                  │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│                    SERVICES                         │
│  ProductService, ProductVariantService              │
│  - Logique métier                                   │
│  - @Transactional                                   │
│  - Orchestration repositories + mappers             │
└──────────────────┬──────────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│   REPOSITORIES   │  │     MAPPERS      │
│  - Spring Data   │  │  - DTO ↔ Entity  │
│  - Query Methods │  │  - MapStruct     │
│  - @Query JPQL   │  │  - Conversions   │
└────────┬─────────┘  └──────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│                    ENTITIES                         │
│  Product, ProductVariant, Category                  │
│  - JPA Annotations                                  │
│  - Business Rules (@PrePersist)                     │
│  - Domain Methods (addStock, removeStock)           │
└─────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│                    DATABASE                         │
│  Tables: products, product_variants, categories     │
└─────────────────────────────────────────────────────┘
```

---

## 📋 STRUCTURE DES FICHIERS

```
com.mmd.marcobrico/
├── domain/
│   ├── Product.java              ✅ Entité corrigée (setters, builder)
│   ├── ProductVariant.java       ✅ Entité corrigée + méthodes métier
│   └── Category.java              (existant)
│
├── dto/
│   ├── ProductCreateDto.java      ✅ Avec @Valid
│   ├── ProductUpdateDto.java      ✅ Nouveau (champs optionnels)
│   ├── ProductResponseDto.java    ✅ Existant
│   ├── ProductVariantCreateDto.java    ✅ Avec initialStock
│   ├── ProductVariantUpdateDto.java    ✅ Nouveau
│   ├── ProductVariantResponseDto.java  ✅ Avec stock
│   └── StockAdjustmentDto.java    ✅ Nouveau (gestion stock)
│
├── mapper/
│   ├── ProductMapper.java         ✅ Corrigé (updateEntity void)
│   └── ProductVariantMapper.java  ✅ Corrigé (updateEntity void)
│
├── repository/
│   ├── ProductRepository.java           ✅ Query methods
│   ├── ProductVariantRepository.java    ✅ Query methods + @Query
│   └── CategoryRepository.java          ✅ Basique
│
├── service/
│   ├── ProductService.java              ✅ Interface
│   ├── ProductServiceImpl.java          ✅ Implémentation
│   ├── ProductVariantService.java       ✅ Interface
│   └── ProductVariantServiceImpl.java   ✅ Implémentation
│
├── controller/
│   ├── ProductController.java           ✅ CRUD + recherches
│   └── ProductVariantController.java    ✅ CRUD + gestion stock
│
└── exception/
    ├── ResourceNotFoundException.java   ✅ Exception custom
    └── GlobalExceptionHandler.java      ✅ @RestControllerAdvice
```

---

## 🔧 CONCEPTS CLÉS EXPLIQUÉS

### 1️⃣ **POURQUOI @Setter DANS LES ENTITÉS?**

**Problème initial :**
```java
@Getter  // ❌ Seulement getter
@Entity
public class Product {
    private Long id;
    private String name;
    // Pas de setters = JPA ne peut pas hydrater!
}
```

**Solution :**
```java
@Getter
@Setter  // ✅ JPA a besoin de setters
@Entity
public class Product {
    private Long id;
    private String name;
}
```

**Explication profonde :**
- JPA/Hibernate utilise les setters pour remplir les objets depuis la DB
- Sans setters, `findById()` ne peut pas créer l'objet
- Les proxies lazy (@ManyToOne lazy) nécessitent des setters
- Alternative : Field Access (`@Access(AccessType.FIELD)`), mais moins standard

---

### 2️⃣ **UPDATE : NOUVELLE INSTANCE VS MODIFICATION EN PLACE**

**❌ MAUVAIS (votre code initial) :**
```java
default Product updateEntity(ProductUpdateDto dto, Product product, Category category) {
    return new Product(  // ❌ Nouvelle instance = perte d'ID!
        dto.name() != null ? dto.name() : product.getName(),
        product.getReference(),
        category != null ? category : product.getCategory(),
        dto.active() != null ? dto.active() : product.isActive(),
        dto.baseUnit() != null ? dto.baseUnit() : product.getBaseUnit()
    );
}

// Dans le service
Product product = productRepository.findById(id).get();  // ID = 5
Product updated = mapper.updateEntity(dto, product, category);
// updated.getId() = null !
productRepository.save(updated);  // INSERT au lieu de UPDATE!
```

**✅ BON (code corrigé) :**
```java
default void updateEntity(ProductUpdateDto dto, Product product, Category category) {
    // Modifie l'entité existante
    if (dto.name() != null) {
        product.setName(dto.name());
    }
    if (dto.baseUnit() != null) {
        product.setBaseUnit(dto.baseUnit());
    }
    // ... autres champs
}

// Dans le service
@Transactional
public ProductResponseDto update(Long id, ProductUpdateDto dto) {
    Product product = productRepository.findById(id).get();  // Managed entity
    mapper.updateEntity(dto, product, category);  // Modification en place
    // Pas besoin de save()! Dirty checking fait le UPDATE automatiquement
    return mapper.toDto(product);
}
```

**Explication profonde :**
- JPA gère les entités par leur **identité** (ID)
- Une entité chargée par `findById()` est **managed** (dans le contexte de persistence)
- Toute modification d'une entité managed est **automatiquement détectée** (dirty checking)
- Au commit de la transaction, JPA génère les UPDATE SQL nécessaires
- Créer une nouvelle instance = perdre le tracking JPA = `save()` fait un INSERT

---

### 3️⃣ **BEAN VALIDATION - FAIL FAST**

**Pourquoi valider dans les DTOs?**

```java
public record ProductCreateDto(
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    String name,
    
    @NotNull
    @Positive
    Long categoryId
) {}
```

**Flow de validation :**

```
HTTP Request
    ↓
Controller @Valid
    ↓
MethodArgumentNotValidException ❌
    ↓
GlobalExceptionHandler
    ↓
400 BAD REQUEST
{
  "errors": {
    "name": "Le nom est obligatoire",
    "categoryId": "doit être positif"
  }
}
```

**Avantages :**
- ❌ Échec **avant** accès à la DB
- 🎯 Messages clairs pour le client
- 📍 Validation centralisée (pas dispersée dans le code)
- 🧪 Testable facilement

---

### 4️⃣ **TRANSACTIONAL - DIRTY CHECKING**

```java
@Transactional
public ProductResponseDto update(Long id, ProductUpdateDto dto) {
    // Début transaction
    Product product = productRepository.findById(id).get();
    // → SELECT * FROM products WHERE id = ?
    // product est maintenant MANAGED
    
    product.setName("Nouveau nom");  // Modification en mémoire
    // Aucune requête SQL pour l'instant
    
    return mapper.toDto(product);
    // Fin de méthode → Commit transaction
    // → JPA détecte le changement (dirty checking)
    // → UPDATE products SET name = 'Nouveau nom' WHERE id = ?
}
```

**Dirty Checking :**
- JPA compare l'état initial vs état actuel
- Génère les UPDATE SQL automatiquement
- **Pas besoin de `save()`** explicite!

---

### 5️⃣ **PAGINATION**

**Pourquoi paginer?**

❌ **Sans pagination :**
```java
List<Product> products = productRepository.findAll();
// Charge TOUTES les lignes en mémoire!
// 100,000 produits → OutOfMemoryError
```

✅ **Avec pagination :**
```java
Page<Product> page = productRepository.findAll(
    PageRequest.of(0, 20)  // Page 0, 20 éléments
);
// SELECT * FROM products LIMIT 20 OFFSET 0
// Charge seulement 20 lignes
```

**Utilisation dans le controller :**

```java
@GetMapping("/api/products")
public Page<ProductResponseDto> getAll(
    @PageableDefault(size = 20, sort = "name") Pageable pageable
) {
    return productService.findAll(pageable);
}
```

**Requête HTTP :**
```
GET /api/products?page=0&size=20&sort=name,asc
```

**Réponse JSON :**
```json
{
  "content": [
    { "id": 1, "name": "Marteau" },
    { "id": 2, "name": "Tournevis" }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

---

## 🚀 EXEMPLES D'UTILISATION DES APIs

### **PRODUCT - CRUD**

#### 1. Créer un produit
```http
POST /api/products
Content-Type: application/json

{
  "name": "Marteau",
  "reference": "MAR-001",
  "baseUnit": "PIECE",
  "categoryId": 5
}

→ 201 CREATED
{
  "id": 123,
  "name": "Marteau",
  "reference": "MAR-001",
  "baseUnit": "PIECE",
  "categoryId": 5,
  "categoryName": "Outils",
  "active": true
}
```

#### 2. Modifier un produit
```http
PATCH /api/products/123
Content-Type: application/json

{
  "name": "Marteau Pro",
  "active": false
}

→ 200 OK
{
  "id": 123,
  "name": "Marteau Pro",  // ✅ Modifié
  "reference": "MAR-001",  // ⚪ Inchangé
  "baseUnit": "PIECE",
  "categoryId": 5,
  "categoryName": "Outils",
  "active": false  // ✅ Modifié
}
```

#### 3. Rechercher par nom
```http
GET /api/products/search?name=marteau&page=0&size=10

→ 200 OK (Page)
```

#### 4. Filtrer par catégorie
```http
GET /api/products?categoryId=5&page=0&size=20

→ 200 OK (Page)
```

#### 5. Désactiver un produit
```http
PATCH /api/products/123/active?active=false

→ 200 OK
```

#### 6. Supprimer un produit
```http
DELETE /api/products/123

→ 204 NO CONTENT
OU
→ 409 CONFLICT (si variants existent)
```

---

### **PRODUCT VARIANT - CRUD + STOCK**

#### 1. Créer une variante
```http
POST /api/products/123/variants
Content-Type: application/json

{
  "saleUnit": "PIECE",
  "conversionFactor": 1.0,
  "price": 15.99,
  "initialStock": 100,
  "wholesale": false
}

→ 201 CREATED
{
  "id": 456,
  "saleUnit": "PIECE",
  "conversionFactor": 1.0,
  "price": 15.99,
  "stock": 100,
  "wholesale": false
}
```

#### 2. Lister les variantes d'un produit
```http
GET /api/products/123/variants

→ 200 OK
[
  { "id": 456, "saleUnit": "PIECE", "stock": 100, ... },
  { "id": 457, "saleUnit": "CARTON", "stock": 50, ... }
]
```

#### 3. Ajouter du stock
```http
POST /api/variants/456/stock/add
Content-Type: application/json

{
  "quantity": 50,
  "reason": "Réapprovisionnement fournisseur"
}

→ 200 OK
{
  "id": 456,
  "stock": 150  // 100 + 50
}
```

#### 4. Retirer du stock
```http
POST /api/variants/456/stock/remove
Content-Type: application/json

{
  "quantity": 25,
  "reason": "Vente client"
}

→ 200 OK
{
  "id": 456,
  "stock": 125  // 150 - 25
}

OU si stock insuffisant:
→ 409 CONFLICT
{
  "message": "Stock insuffisant"
}
```

#### 5. Vérifier disponibilité
```http
GET /api/variants/456/stock/check?quantity=200

→ 200 OK
{
  "variantId": 456,
  "quantityRequested": 200,
  "available": false  // Stock = 125 < 200
}
```

#### 6. Lister les ruptures de stock
```http
GET /api/variants/out-of-stock?page=0&size=20

→ 200 OK (Page de variants avec stock = 0)
```

---

## ❌ GESTION DES ERREURS

### **Erreurs Gérées Automatiquement**

| Exception | Status HTTP | Exemple |
|-----------|-------------|---------|
| `ResourceNotFoundException` | 404 NOT FOUND | Produit ID inexistant |
| `MethodArgumentNotValidException` | 400 BAD REQUEST | Nom vide, prix négatif |
| `IllegalArgumentException` | 400 BAD REQUEST | conversionFactor <= 0 |
| `IllegalStateException` | 409 CONFLICT | Stock insuffisant |
| `DataIntegrityViolationException` | 409 CONFLICT | Référence dupliquée, FK violation |

### **Exemples de Réponses d'Erreur**

#### 404 - Ressource non trouvée
```json
{
  "timestamp": "2026-02-03T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Produit non trouvé avec l'ID: 999",
  "path": "/api/products/999"
}
```

#### 400 - Validation échouée
```json
{
  "timestamp": "2026-02-03T14:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "name": "Le nom est obligatoire",
    "price": "doit être positif",
    "categoryId": "ne peut pas être null"
  },
  "path": "/api/products"
}
```

#### 409 - Conflit
```json
{
  "timestamp": "2026-02-03T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Stock insuffisant",
  "path": "/api/variants/456/stock/remove"
}
```

---

## 🎓 BONNES PRATIQUES APPLIQUÉES

### ✅ **Entités**
- Setters pour JPA
- Builder pattern pour lisibilité
- Méthodes métier (addStock, removeStock)
- Validation dans @PrePersist/@PreUpdate
- FetchType.LAZY par défaut

### ✅ **DTOs**
- Records Java (immutables)
- Bean Validation complète
- Séparation Create/Update/Response
- DTOs métier (StockAdjustmentDto)

### ✅ **Mappers**
- Modification en place (pas de nouvelle instance)
- Méthodes default pour logique custom
- ComponentModel spring pour injection

### ✅ **Services**
- Interface + implémentation
- @Transactional avec readOnly
- Dirty checking (pas de save() explicite)
- Logging approprié

### ✅ **Repositories**
- Query methods Spring Data
- @Query JPQL pour requêtes complexes
- Pagination systématique
- exists() vs find() pour performance

### ✅ **Controllers**
- @Valid sur tous les @RequestBody
- ResponseEntity avec status HTTP approprié
- Pagination avec @PageableDefault
- Endpoints RESTful (verbes HTTP corrects)
- Nested resources (/products/{id}/variants)

### ✅ **Exceptions**
- Exceptions custom sémantiques
- GlobalExceptionHandler centralisé
- Messages utilisateur friendly
- Parsing des erreurs DB

---

## 🧪 TESTS RECOMMANDÉS

```java
// Test Service
@SpringBootTest
class ProductServiceImplTest {
    @Test
    void update_shouldModifyInPlace() {
        // Créer un produit
        Product product = productRepository.save(new Product(...));
        Long originalId = product.getId();
        
        // Modifier
        ProductUpdateDto dto = new ProductUpdateDto("Nouveau nom", null, null, null);
        ProductResponseDto updated = productService.update(originalId, dto);
        
        // Vérifier
        assertEquals(originalId, updated.id());  // ✅ Même ID
        assertEquals("Nouveau nom", updated.name());
    }
}

// Test Controller
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Test
    void create_withInvalidDto_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))  // ❌ Nom vide
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists());
    }
}
```

---

## 📝 CHECKLIST DE DÉPLOIEMENT

- [ ] Configuration DB (application.yml)
- [ ] Migrations Liquibase/Flyway
- [ ] Variables d'environnement (secrets)
- [ ] Actuator pour monitoring
- [ ] Logs structurés (JSON)
- [ ] Tests d'intégration
- [ ] Documentation API (Swagger/OpenAPI)
- [ ] CI/CD pipeline
- [ ] Rate limiting
- [ ] CORS configuration

---

## 🎉 CONCLUSION

Cette architecture est:
- ✅ **Production-ready** (gestion erreurs, validation, transactions)
- ✅ **Maintenable** (séparation couches, code clair)
- ✅ **Performante** (pagination, lazy loading, dirty checking)
- ✅ **Testable** (interfaces, injection dépendances)
- ✅ **Évolutive** (facile d'ajouter nouveaux endpoints)

**Prochaines étapes suggérées :**
1. Ajouter Swagger/OpenAPI pour documentation auto
2. Implémenter les tests unitaires et d'intégration
3. Ajouter table StockMovement pour historique
4. Implémenter soft delete pour ProductVariant
5. Ajouter endpoints de recherche avancée (filtres multiples)
6. Mettre en place caching (Redis) pour requêtes fréquentes