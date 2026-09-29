### Simulation `curl` (Création de compte) :

```bash
curl -X POST http://localhost:8000/auth/register \
     -H "Content-Type: application/json" \
     -d '{
           "email": "litoxam@gmail.com",
           "username": "Litoxam",
           "password": "MyPassWord123456"
         }'