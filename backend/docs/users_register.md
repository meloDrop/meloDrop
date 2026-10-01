### Règles de validation

- `email` : adresse e-mail valide
- `username` : obligatoire
- `password` : entre 8 et 128 caractères

### Réponses

- `201` : compte créé, renvoie `id`, `email` et `username`
- `409` : `email_taken` ou `username_taken`
- `422` : données invalides (email mal formé, mot de passe trop court ou trop long)

### Simulation `curl` (Création de compte) :

```bash
curl -X POST http://localhost:8000/auth/register \
     -H "Content-Type: application/json" \
     -d '{
           "email": "litoxam@example.com",
           "username": "Litoxam",
           "password": "MyPassWord123456"
         }'
```