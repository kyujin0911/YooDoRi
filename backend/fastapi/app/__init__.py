from fastapi import FastAPI, Depends, Path, HTTPException
from pydantic import BaseModel
from .database import Database
from sqlalchemy import *


# FastAPI 인스턴스 생성
app = FastAPI()
engine = Database()
session = engine.get_session()

# 라우트 설정
def create_app():
    from . import routes
    app.include_router(routes.router)
    routes.sched.start()

    return app