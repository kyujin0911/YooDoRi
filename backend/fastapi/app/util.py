from .database import Database
from .config import Config
from fastapi import Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
from datetime import timedelta, datetime
from jose import JWTError, jwt
from . import models



def make_token(name: str, key: str):
    data = {
        "name": name,
        "key": key,
        "exp": datetime.utcnow() + timedelta(minutes = Config.ACCESS_TOKEN_EXPIRE_MINUTES)
    }

    access_token = jwt.encode(data, Config.SECRET_KEY, Config.ALGORITHM)

    return access_token

def get_user(userName, key, session = Depends(Database().get_session)):
    try:

        userInfo = session.query(models.nok_info).filter_by(nok_name = userName, nok_key = key).first()
        if userInfo:
            return userInfo, 0
        else:
            userInfo = session.query(models.dementia_info).filter_by(dementia_name = userName, dementia_key = key).first()
            if userInfo:
                return userInfo, 1
            else:
                return None
    except Exception as e:
        print(f"[ERROR] User information not found")
        raise HTTPException(status_code=404, detail="User information not found")
    finally:
        session.close()

def get_current_user(token: str = Depends(OAuth2PasswordBearer(tokenUrl="/login")), session = Depends(Database().get_session)):
    credentials_exception = HTTPException(
        status_code=401,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, Config.SECRET_KEY, algorithms=[Config.ALGORITHM])

        username: str = payload.get("name")

        if username is None:
            raise credentials_exception
        
        user, user_type = get_user(username, payload.get("key"), session)

        if user is None:
            raise credentials_exception
        
    except JWTError:
        raise credentials_exception
    
    return user, user_type
