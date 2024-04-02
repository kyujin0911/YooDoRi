from fastapi import APIRouter, Request, HTTPException
from . import models
from .random_generator import RandomNumberGenerator
from .database import Database

router = APIRouter()
db = Database()

SUCCESS = 200
WRONG_REQUEST = 400
KEYNOTFOUND = 600
LOCDATANOTFOUND = 650
LOCDATANOTENOUGH = 660
LOGINSUCCESS = 700
LOGINFAILED = 750
UNDEFERR = 500


@router.post("/receive-nok-info")
async def receive_nok_info(request: Request):
    data = await request.json()

    _key_from_dementia = data.get("keyFromDementia")
    rng = RandomNumberGenerator()

    session = next(db.get_session())
    try:
        existing_dementia = session.query(models.dementia_info).filter(models.dementia_info.dementia_key == _key_from_dementia).first()
        if existing_dementia:
            _nok_name = data.get("nokName")
            _nok_phonenumber = data.get("nokPhoneNumber")
            
            duplication_check = session.query(models.nok_info).filter(models.nok_info.nok_name == _nok_name, models.nok_info.nok_phonenumber == _nok_phonenumber, models.nok_info.dementia_info_key == _key_from_dementia).first()

            if duplication_check:
                _key = duplication_check.nok_key
            else:
                unique_key = None
                for _ in range(10):
                    unique_key = rng.generate_unique_random_number(100000, 999999)
                
                _key = str(unique_key)

            new_nok = models.nok_info(nok_key=_key, nok_name=_nok_name, nok_phonenumber=_nok_phonenumber, dementia_info_key=_key_from_dementia)
            session.add(new_nok)
            session.commit()
            result = {
                'dementiaInfoRecord' : {
                        'dementiaKey' : existing_dementia.dementia_key,
                        'dementiaName': existing_dementia.dementia_name,
                        'dementiaPhoneNumber': existing_dementia.dementia_phonenumber
                },
                'nokKey': _key
            }

            response = {
                'status': 'success',
                'status_code': SUCCESS,
                'result': result
            }
            
            return response
        else: # 보호 대상자 인증번호가 등록되어 있지 않은 경우
            response = {
                'status': 'fail',
                'status_code': KEYNOTFOUND,   
                'error': 'Dementia information not found'
            }

            return response
    finally:
        session.close()

@router.post("/receive-dementia-info")
async def receive_dementia_info(request: Request):
    data = await request.json()

    rng = RandomNumberGenerator()

    session = next(db.get_session())

    try:
        _dementia_name = data.get("name")
        _dementia_phonenumber = data.get("phoneNumber")

        duplication_check = session.query(models.dementia_info).filter(models.dementia_info.dementia_name == _dementia_name, models.dementia_info.dementia_phonenumber == _dementia_phonenumber).first()

        if duplication_check: # 기존의 인증번호를 가져옴
            _key = duplication_check.dementia_key
        else: # 새로운 인증번호 생성
            unique_key = None
            for _ in range(10):
                unique_key = rng.generate_unique_random_number(100000, 999999)
            
            _key = str(unique_key)

            new_dementia = models.dementia_info(dementia_key=_key, dementia_name=_dementia_name, dementia_phonenumber=_dementia_phonenumber)
            session.add(new_dementia)
            session.commit()

        result = {
            'dementiaKey': _key
        }

        response = {
            'status': 'success',
            'status_code' : SUCCESS,
            'result': result
        }

        return response
    
    finally:
        session.close()
