from fastapi import APIRouter, Request, HTTPException
from . import models
from .random_generator import RandomNumberGenerator
from .update_user_status import UpdateUserStatus
from .database import Database

import json

router = APIRouter()
db = Database()
session = next(db.get_session())

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

@router.post("/is-connected")
async def is_connected(request: Request):
    data = await request.json()
    _dementia_key = data.get("dementiaKey")

    session = next(db.get_session())
    try:
        existing_dementia = session.query(models.nok_info).filter_by(dementia_info_key = _dementia_key).first()
        if existing_dementia:
            result = {
                'nokInfoRecord':{
                    'nokKey': existing_dementia.nok_key,
                    'nokName': existing_dementia.nok_name,
                    'nokPhoneNumber': existing_dementia.nok_phonenumber
                }
            }
            response = {
                'status': 'success',
                'status_code': SUCCESS,
                'result': result
            }

            return response
        
        else:
            response = {
                'status': 'fail',
                'status_code': KEYNOTFOUND,
                'error': 'Dementia information not found'
            }

            return response
    finally:
        session.close()

@router.post("/receive-user-login")
async def receive_user_login(request: Request):
    data = await request.json()

    _key = data.get("key")
    _isdementia = data.get("isDementia")

    try:
        if _isdementia == 0: # 보호자인 경우
            existing_nok = session.query(models.nok_info).filter_by(nok_key = _key).first()

            if existing_nok:
                response = {
                    'status': 'success',
                    'status_code': LOGINSUCCESS
                }

                return response
            else:
                response = {
                    'status': 'fail',
                    'status_code': LOGINFAILED
                }

                return response
        elif _isdementia == 1: # 보호 대상자인 경우
            existing_dementia = session.query(models.dementia_info).filter_by(dementia_key = _key).first()

            if existing_dementia:
                response = {
                    'status': 'success',
                    'status_code': LOGINSUCCESS
                }

                return response
            else:
                response = {
                    'status': 'fail',
                    'status_code': LOGINFAILED
                }

                return response
            
    finally:
        session.close()

@router.post("/receive-location-info")
async def receive_location_info(request: Request):
    data = await request.json()

    json_data = json.dumps(data)

    _dementia_key = data.get("dementiaKey")

    try:
        existing_dementia = session.query(models.dementia_info).filter_by(dementia_key = _dementia_key).first()

        if existing_dementia:

            user_status_updater = UpdateUserStatus()

            lightsensor = data.get("lightsensor")

            prediction = user_status_updater.predict(json_data)

            new_location = models.location_info(
                dementia_key = data.get("dementiaKey"),
                date = data.get("date"),
                time = data.get("time"),
                latitude = data.get("latitude"),
                longitude = data.get("longitude"),
                bearing = data.get("bearing"),
                user_status = int(prediction[0]),
                accelerationsensor_x = data.get("accelerationsensor")[0],
                accelerationsensor_y = data.get("accelerationsensor")[1],
                accelerationsensor_z = data.get("accelerationsensor")[2],
                directionsensor_x = data.get("directionsensor")[0],
                directionsensor_y = data.get("directionsensor")[1],
                directionsensor_z = data.get("directionsensor")[2],
                gyrosensor_x = data.get("gyrosensor")[0],
                gyrosensor_y = data.get("gyrosensor")[1],
                gyrosensor_z = data.get("gyrosensor")[2],
                lightsensor = lightsensor[0],
                battery = data.get("battery"),
                isInternetOn = data.get("isInternetOn"),
                isRingstoneOn = data.get("isRingstoneOn"),
                isGpsOn = data.get("isGpsOn"),
                current_speed = data.get("currentSpeed")
            )

            session.add(new_location)
            session.commit()

            print(f"[INFO] Location data received from {existing_dementia.dementia_name}({existing_dementia.dementia_key})")

            response = {
                'status': 'success',
                'status_code': SUCCESS
            }
        else:
            response = {
                'status': 'fail',
                'status_code': KEYNOTFOUND,
                'error': 'Dementia key not found'
            }

            return response
        
    finally:
        session.close()


