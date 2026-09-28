from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas.user import UserRegister
from app.services.auth import register_user

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", status_code=201)
def register(body: UserRegister, db: Session = Depends(get_db)):
    try:
        user = register_user(db, body.email, body.username, body.password)
    except ValueError as e:
        raise HTTPException(status_code=409, detail=str(e))

    return {"id": user.id, "email": user.email, "username": user.username}