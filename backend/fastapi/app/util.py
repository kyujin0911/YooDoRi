from .database import Database
from .config import Config
from fastapi import Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
from datetime import timedelta, datetime
from jose import JWTError, jwt
from pytz import timezone
from . import models



def make_token(name: str, key: str):
    data = {
        "name": name,
        "key": key,
        "exp": datetime.utcnow() + timedelta(minutes = Config.ACCESS_TOKEN_EXPIRE_MINUTES)
    }

    access_token = jwt.encode(data, Config.SECRET_KEY, Config.ALGORITHM)

    return access_token

def make_refresh_token(name: str, key: str):
    data = {
        "name": name,
        "key": key,
        "exp": datetime.now(timezone('Asia/Seoul')) + timedelta(minutes = Config.REFRESH_TOCKEN_EXPIRE_MINUTES)
    }

    refresh_token = jwt.encode(data, Config.SECRET_KEY, Config.ALGORITHM)

    return refresh_token

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

def check_token_expired(token: str):
    try:
        payload = jwt.decode(token, Config.SECRET_KEY, algorithms=[Config.ALGORITHM])
        now = datetime.timestamp(datetime.now(timezone('Asia/Seoul')))
        exp: datetime = payload.get("exp")

        if exp < now:
            return None
        
        return payload
    
    except JWTError:
        return True

def get_current_user(token: str = Depends(OAuth2PasswordBearer(tokenUrl="/login")), session = Depends(Database().get_session)):
    try:
        payload = jwt.decode(token, Config.SECRET_KEY, algorithms=[Config.ALGORITHM])

        if check_token_expired(token):
            pass
        else:
            raise HTTPException(
                    status_code=401,
                    detail="Expired token",
                    headers={"WWW-Authenticate": "Bearer"},
                    code="EXPIRED_TOKEN"
                )

        username: str = payload.get("name")

        if username is None:
            raise HTTPException(status_code=401, detail="Could not validate credentials")
        
        user, user_type = get_user(username, payload.get("key"), session)

        if user is None:
            raise HTTPException(status_code=404, detail="User not found")
        
    except HTTPException as e:
        raise e
    
    return user, user_type
