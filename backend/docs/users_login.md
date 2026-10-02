### Règles de validation

- `identifier` : adresse e-mail ou nom d'utilisateur
- `password` : 128 caractères maximum

### Réponses

- `200` : connexion réussie, renvoie `access_token` et `token_type`
- `401` : `invalid_credentials` (identifiant inconnu ou mauvais mot de passe)
- `422` : données invalides (champ manquant, mot de passe trop long)

### Simulation `curl` (Connexion) :

```bash
curl -X POST http://localhost:8000/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "identifier": "Litoxam",
           "password": "MyPassWord123456"
         }'
```
