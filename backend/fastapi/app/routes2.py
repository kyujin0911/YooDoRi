from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import OAuth2PasswordRequestForm, APIKeyHeader, OAuth2PasswordBearer

from apscheduler.schedulers.background import BackgroundScheduler
from passlib.context import CryptContext
from haversine import haversine
from PyKakao import Local
from datetime import datetime, timedelta
from pytz import timezone

from . import models
from .random_generator import RandomNumberGenerator
from .update_user_status import UpdateUserStatus
from .database import Database
from .bodymodel import *
from .util import JWTService
from .config import Config
from .schedularFunc import SchedulerFunc
from .fcm_notification import send_push_notification
from .user_status_convertor import convertor
from .LocationPredict import ForecastLSTMClassification, Preprocessing

import asyncio
import requests
import urllib.parse
import pandas as pd
import time


router = APIRouter()
db = Database()
session = next(db.get_session())
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
jwt = JWTService()
schedFunc = SchedulerFunc()
sched = BackgroundScheduler(timezone="Asia/Seoul", daemon=True)
kakao = Local(service_key=Config.kakao_service_key)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")



@router.post("/test/fcm", description="FCM 테스트")
async def send_fcm(title: str, body: str, token: str, data : str):

    return send_push_notification(token, body, title, data)

@router.post("/noks",status_code=status.HTTP_201_CREATED, responses = {201 : {"model" : ReceiveNokInfoResponse, "description" : "유저 등록 성공" },404: {"model": ErrorResponse, "description": "보호 대상자 키 조회 실패"}}, description="보호자가 보호 대상자의 정보를 등록")
async def receive_nok_info(request: ReceiveNokInfoRequest):

    _key_from_dementia = request.keyFromDementia

    rng = RandomNumberGenerator()

    try:
        existing_dementia = session.query(models.dementia_info).filter(models.dementia_info.dementia_key == _key_from_dementia).first()
        if existing_dementia:
            _nok_name = request.nokName
            _nok_phonenumber = request.nokPhoneNumber
            
            duplication_check = session.query(models.nok_info).filter(models.nok_info.nok_name == _nok_name, models.nok_info.nok_phonenumber == _nok_phonenumber, models.nok_info.dementia_info_key == _key_from_dementia).first()

            if duplication_check:
                _key = duplication_check.nok_key
                
            else:
                unique_key = None
                for _ in range(10):
                    unique_key = rng.generate_unique_random_number(100000, 999999)
                
                _key = str(unique_key)

                new_nok = models.nok_info(nok_key=_key, nok_name=_nok_name, nok_phonenumber=_nok_phonenumber, dementia_info_key=_key_from_dementia, update_rate=1) # update_rate는 기본값 1분으로 설정
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

            print(f"[INFO] NOK information received from {existing_dementia.dementia_name}({existing_dementia.dementia_key})")

            response = {
                'status': 'success',
                'message': 'NOK information received',
                'result': result
            }

            return response
            
        else: # 보호 대상자 인증번호가 등록되어 있지 않은 경우

            print(f"[ERROR] Dementia key({_key_from_dementia}) not found")

            raise HTTPException(status_code=404, detail="Dementia key not found")

    finally:
        session.close()

@router.post("/dementias", status_code=status.HTTP_201_CREATED, responses = {201 : {"model" : ReceiveDementiaInfoResponse, "description" : "유저 등록 성공" }}, description="보호 대상자의 정보를 등록")
async def receive_dementia_info(request: ReceiveDementiaInfoRequest):

    rng = RandomNumberGenerator()

    try:
        _dementia_name = request.name
        _dementia_phonenumber = request.phoneNumber

        duplication_check = session.query(models.dementia_info).filter(models.dementia_info.dementia_name == _dementia_name, models.dementia_info.dementia_phonenumber == _dementia_phonenumber).first()

        if duplication_check: # 기존의 인증번호를 가져옴
            _key = duplication_check.dementia_key
        else: # 새로운 인증번호 생성
            unique_key = None
            for _ in range(10):
                unique_key = rng.generate_unique_random_number(100000, 999999)
            
            _key = str(unique_key)

            #_key = pwd_context.hash(unique_key)

            new_dementia = models.dementia_info(dementia_key=_key, dementia_name=_dementia_name, dementia_phonenumber=_dementia_phonenumber, update_rate=1) # update_rate는 기본값 1분으로 설정
            session.add(new_dementia)
            session.commit()

        result = {
            'dementiaKey': _key
        }

        response = {
            'status': 'success',
            'message' : 'Dementia information received',
            'result': result
        }

        print(f"[INFO] Dementia information received from {_dementia_name}({_key})")

        return response
    
    finally:
        session.close()

@router.post("/connection", responses = {200 : {"model" : ConnectionResponse, "description" : "연결 확인 성공" }, 400: {"model": ErrorResponse, "description": "연결 실패"}}, description="보호자와 보호 대상자의 연결 확인")
async def is_connected(request: ConnectionRequest):

    _dementia_key = request.dementiaKey

    session = next(db.get_session())
    try:
        existing_nok = session.query(models.nok_info).filter_by(dementia_info_key = _dementia_key).first()
        if existing_nok:
            result = {
                'nokInfoRecord':{
                    'nokKey': existing_nok.nok_key,
                    'nokName': existing_nok.nok_name,
                    'nokPhoneNumber': existing_nok.nok_phonenumber
                }
            }
            response = {
                'status': 'success',
                'message': 'Connection check',
                'result': result
            }

            print(f"[INFO] Connection check from {existing_nok.nok_name}(from {existing_nok.dementia_info_key})")

            return response
        
        else:
            print (f"[ERROR] Connection denied from Dementia key({_dementia_key})")

            raise HTTPException(status_code=400, detail="Connection denied")

    finally:
        session.close()

@router.post("/login", responses = {200 : {"model" : CommonResponse, "description" : "로그인 성공" }, 400: {"model": ErrorResponse, "description": "로그인 실패"}}, description="보호자와 보호 대상자의 로그인 | isDementia : 0(보호자), 1(보호 대상자)")
async def receive_user_login(request: loginRequest):
    _key = request.key
    _isdementia = request.isDementia
    try:
        if _isdementia == 0: # 보호자인 경우
            existing_nok = session.query(models.nok_info).filter_by(nok_key = _key).first()

            if existing_nok:
                response = {
                    'status': 'success',
                    'message': 'User login success',
                }
                print(f"[INFO] User login from {existing_nok.nok_name}({existing_nok.nok_key})")

            else:
                print(f"[ERROR] User login failed from NOK key({_key})")

                raise HTTPException(status_code=400, detail="User login failed")
        
        elif _isdementia == 1: # 보호 대상자인 경우
            existing_dementia = session.query(models.dementia_info).filter_by(dementia_key = _key).first()

            if existing_dementia:
                response = {
                    'status': 'success',
                    'message': 'User login success',
                }
                print(f"[INFO] User login from {existing_dementia.dementia_name}({existing_dementia.dementia_key})")

            else:
                print(f"[ERROR] User login failed from Dementia key({_key})")

                raise HTTPException(status_code=400, detail="User login failed")

        return response
        
    finally:
        session.close()

@router.post("/locations/dementias", responses = {200 : {"model" : TempResponse, "description" : "위치 정보 전송 성공" }, 404: {"model": ErrorResponse, "description": "보호 대상자 키 조회 실패"}}, description="보호 대상자의 위치 정보를 전송 | isRingstoneOn : 0(무음), 1(진동), 2(벨소리)")
async def receive_location_info(request: ReceiveLocationRequest):

    try:
        _dementia_key = request.dementiaKey

        existing_dementia = session.query(models.dementia_info).filter_by(dementia_key = _dementia_key).first()

        if existing_dementia:

            user_status_updater = UpdateUserStatus()

            accel = request.accelerationSensor
            gyro = request.gyroSensor
            direction = request.directionSensor

            prediction = user_status_updater.predict(accel, gyro, direction)

            if prediction[0]==1:
                status = "정지"
            elif prediction[0]==2:
                status = "도보"
            elif prediction[0]==3:
                status = "차량"
            elif prediction[0]==4:
                status = "지하철"
            else:
                pass

            new_location = models.location_info(
                dementia_key = _dementia_key,
                date = request.date,
                time = request.time,
                latitude = request.latitude,
                longitude = request.longitude,
                bearing = request.bearing,
                user_status = status,
                accelerationsensor_x = accel[0],
                accelerationsensor_y = accel[1],
                accelerationsensor_z = accel[2],
                directionsensor_x = direction[0],
                directionsensor_y = direction[1],
                directionsensor_z = direction[2],
                gyrosensor_x = gyro[0],
                gyrosensor_y = gyro[1],
                gyrosensor_z = gyro[2],
                lightsensor = request.lightSensor[0],
                battery = request.battery,
                isInternetOn = request.isInternetOn,
                isRingstoneOn = request.isRingstoneOn,
                isGpsOn = request.isGpsOn,
                current_speed = request.currentSpeed
            )

            session.add(new_location)
            session.commit()

            print(f"[INFO] Location data received from {existing_dementia.dementia_name}({existing_dementia.dementia_key})")

            response = {
                'status': 'success',
                'message': 'Location data received',
                'result' : int(prediction[0])
            }

        else:
            print(f"[ERROR] Dementia key({_dementia_key}) not found(receive location info)")

            raise HTTPException(status_code=404, detail="Dementia key not found")

        return response
        
    finally:
        session.close()

@router.get("/locations/noks", responses = {200 : {"model" : GetLocationResponse, "description" : "위치 정보 전송 성공" }, 404: {"model": ErrorResponse, "description": "위치 정보 없음"}}, description="보호자에게 보호 대상자의 위치 정보를 전송(쿼리 스트링) | userStatus : 1(정지), 2(도보), 3(차량), 4(지하철) | isRingstoneOn : 0(무음), 1(진동), 2(벨소리)")
async def send_live_location_info(dementiaKey : str):

    try:
        
        latest_location = session.query(models.location_info).filter_by(dementia_key = dementiaKey).order_by(models.location_info.num.desc()).first()
        


        if latest_location:
            result = {
                'latitude': latest_location.latitude,
                'longitude': latest_location.longitude,
                'bearing': latest_location.bearing,
                'currentSpeed': latest_location.current_speed,
                'userStatus': latest_location.user_status, # 1: 정지, 2: 도보, 3: 차량, 4: 지하철
                'battery': latest_location.battery,
                'isInternetOn': latest_location.isInternetOn,
                'isGpsOn': latest_location.isGpsOn,
                'isRingstoneOn': latest_location.isRingstoneOn # 0 : 무음, 1 : 진동, 2 : 벨소리
            }
            response = {
                'status': 'success',
                'message': 'Live location data sent',
                'result': result
            }
            print(f"[INFO] Live location data sent to {latest_location.dementia_key}")

        else:
            print(f"[ERROR] Location data not found for Dementia key({dementiaKey})")

            raise HTTPException(status_code=404, detail="Location data not found")

        return response
    
    finally:
        session.close()

@router.post("/users/modification/userInfo", responses = {200 : {"model" : CommonResponse, "description" : "유저 정보 수정 성공" }, 404: {"model": ErrorResponse, "description": "유저 키 조회 실패"}}, description="보호자와 보호대상자의 정보를 수정 | isDementia : 0(보호자), 1(보호대상자) | 변경하지 않는 값은 기존의 값을 그대로 수신할 것")
async def modify_user_info(request: ModifyUserInfoRequest):

    _is_dementia = request.isDementia
    _key = request.key
    _before_name = request.name
    _before_phonenumber = request.phoneNumber

    try:
        if _is_dementia == 0: #보호자
            existing_nok = session.query(models.nok_info).filter_by(nok_key = _key).first()

            if existing_nok:
                # 수정된 정보를 제외한 나머지 정보들은 기존의 값을 그대로 수신받음

                if not existing_nok.nok_name == _before_name:
                    existing_nok.nok_name = _before_name
                
                if not existing_nok.nok_phonenumber == _before_phonenumber:
                    existing_nok.nok_phonenumber = _before_phonenumber
                
                session.commit()

                print(f"[INFO] User information modified by {existing_nok.nok_name}({existing_nok.nok_key})")

                response = {
                    'status': 'success',
                    'message': 'User information modified'
                }
            else:
                print(f"[ERROR] NOK key not found")
                
                raise HTTPException(status_code=404, detail="NOK key not found")

        elif _is_dementia == 1: #보호대상자
            existing_dementia = session.query(models.dementia_info).filter_by(dementia_key = _key).first()

            if existing_dementia:
                # 수정된 정보를 제외한 나머지 정보들은 기존의 값을 그대로 수신받음

                if not existing_dementia.dementia_name == _before_name:
                    existing_dementia.dementia_name = _before_name
                
                if not existing_dementia.dementia_phonenumber == _before_phonenumber:
                    existing_dementia.dementia_phonenumber = _before_phonenumber
                
                session.commit()

                print(f"[INFO] User information modified by {existing_dementia.dementia_name}({existing_dementia.dementia_key})")

                response = {
                    'status': 'success',
                    'message': 'User information modified'
                }

            else:
                print(f"[ERROR] Dementia key not found")

                raise HTTPException(status_code=404, detail="Dementia key not found")

        return response
    
    finally:
        session.close()

@router.post("/users/modification/updateRate", responses = {200 : {"model" : CommonResponse, "description" : "업데이트 주기 수정 성공" }, 404: {"model": ErrorResponse, "description": "유저 키 조회 실패"}}, description="보호자와 보호대상자의 업데이트 주기를 수정 | isDementia : 0(보호자), 1(보호대상자)")
async def modify_updatint_rate(request: ModifyUserUpdateRateRequest):
    _is_dementia = request.isDementia
    _key = request.key
    _update_rate = request.updateRate

    #보호자와 보호대상자 모두 업데이트
    try:
        if _is_dementia == 0: #보호자
            existing_nok = session.query(models.nok_info).filter_by(nok_key = _key).first()

            if existing_nok:
                connected_dementia = session.query(models.dementia_info).filter_by(dementia_key = existing_nok.dementia_info_key).first()
                existing_nok.update_rate = _update_rate
                connected_dementia.update_rate = _update_rate

                print(f"[INFO] Update rate modified by {existing_nok.nok_name}, {connected_dementia.dementia_name}")

                response = {
                    'status': 'success',
                    'message': 'User update rate modified'
                }
            else:
                print(f"[ERROR] NOK key not found(update rate)")

                raise HTTPException(status_code=404, detail="NOK key not found")

        elif _is_dementia == 1:
            existing_dementia = session.query(models.dementia_info).filter_by(dementia_key = _key).first()

            if existing_dementia:
                connected_nok = session.query(models.nok_info).filter_by(dementia_info_key = existing_dementia.dementia_key).first()
                existing_dementia.update_rate = _update_rate
                connected_nok.update_rate = _update_rate

                print(f"[INFO] Update rate modified by {existing_dementia.dementia_name}, {connected_nok.nok_name}")

                response = {
                    'status': 'success',
                    'message': 'User update rate modified'
                }
            else:
                print(f"[ERROR] Dementia key not found(update rate)")

                raise HTTPException(status_code=404, detail="Dementia key not found")
        
        session.commit()
    
        return response
    
    finally:
        session.close()

@router.post("/dementias/averageWalkingSpeed", responses = {200 : {"model" : AverageWalkingSpeedResponse, "description" : "평균 걷기 속도 계산 성공" }, 404: {"model": ErrorResponse, "description": "보호 대상자 키 조회 실패 or 위치 정보 부족"}}, description="보호 대상자의 평균 걷기 속도를 계산 및 마지막 정보 전송")
async def caculate_dementia_average_walking_speed(requset: AverageWalkingSpeedRequest): # current_user : int = Depends(APIKeyHeader(name = "Authorization"))

    #_dementia_key = get_current_user(current_user)["key"]

    _dementia_key = requset.dementiaKey

    if _dementia_key is None:
        print(f"[ERROR] Dementia key not found(calculate dementia average walking speed)")
        
        raise HTTPException(status_code=404, detail="Dementia key not found")
    
    try:
        #최근 10개의 정보를 가져와 평균 속도 계산(임시)
        location_info_list = session.query(models.location_info).filter_by(dementia_key = _dementia_key, user_status = "도보").order_by(models.location_info.num.desc()).limit(10).all()
        
        if location_info_list:
            sum_speed = 0
            for location_info in location_info_list:
                print(location_info.current_speed)
                sum_speed += float(location_info.current_speed)
                print(sum_speed)
            
            average_speed = round(sum_speed / len(location_info_list), 2)
            
            geo = kakao.geo_coord2address(location_info_list[0].longitude, location_info_list[0].latitude)

            if not geo['documents'][0]['road_address'] == None:
                xy2addr = geo['documents'][0]['road_address']['address_name'] + " " + geo['documents'][0]['road_address']['building_name']
                    
            else:
                xy2addr = geo['documents'][0]['address']['address_name']

            response = {
                'status': 'success',
                'message': 'Dementia average walking speed calculated',
                'result': {
                    'averageSpeed': average_speed,
                    'lastLatitude': location_info_list[0].latitude,
                    'lastLongitude': location_info_list[0].longitude,
                    'addressName' : xy2addr
                }
            }
            print(f"[INFO] Dementia average walking speed calculated for {location_info_list[0].dementia_key}")

        else:
            print(f"[ERROR] Not enough location data for Dementia key({_dementia_key})")

            raise HTTPException(status_code=404, detail="Not enough location data")

        return response
    
    finally:
        session.close()

@router.get("/users/info", responses = {200 : {"model" : GetUserInfoResponse, "description" : "유저 정보 전송 성공" }, 404: {"model": ErrorResponse, "description": "유저 정보 없음"}}, description="보호자와 보호 대상자 정보 전달(쿼리 스트링)")
async def get_user_info(nokKey : str):
    _nok_key = nokKey

    try:
        nok_info_record = session.query(models.nok_info).filter_by(nok_key = _nok_key).first()
        

        if nok_info_record:
            dementia_info_record = session.query(models.dementia_info).filter_by(dementia_key = nok_info_record.dementia_info_key).first()
            if not dementia_info_record:
                print(f"[ERROR] Dementia information not found for nok key({_nok_key})")

                raise HTTPException(status_code=404, detail="Dementia information not found")
            
            result = {
                'dementiaInfoRecord': {
                    'dementiaKey': dementia_info_record.dementia_key,
                    'dementiaName': dementia_info_record.dementia_name,
                    'dementiaPhoneNumber': dementia_info_record.dementia_phonenumber,
                    'updateRate': dementia_info_record.update_rate
                },
                'nokInfoRecord': {
                    'nokKey': nok_info_record.nok_key,
                    'nokName': nok_info_record.nok_name,
                    'nokPhoneNumber': nok_info_record.nok_phonenumber,
                    'updateRate': nok_info_record.update_rate
                }
            }

            response = {
                'status': 'success',
                'message': 'User information sent',
                'result': result
            }

            print(f"[INFO] User information sent to {dementia_info_record.dementia_name}({dementia_info_record.dementia_key})")

        else:
            print(f"[ERROR] User information not found for nok key({_nok_key})")

            return ErrorResponse(status_code=404, message="User information not found")

        return response
    
    finally:
        session.close()

@router.get("/locations/meaningful", responses = {200 : {"model" : MeaningfulLocResponse, "description" : "의미장소 전송 성공" }, 404: {"model": ErrorResponse, "description": "의미 장소 없음"}}, description="보호 대상자의 의미 장소 정보 및 주변 경찰서 정보 전달(쿼리 스트링)")
async def send_meaningful_location_info(dementiaKey: str):
    _key = dementiaKey

    try:
        meaningful_location_list = session.query(models.meaningful_location_info).filter_by(dementia_key=_key).all()

        if meaningful_location_list:
            meaningful_places_dict = {}


            for location in meaningful_location_list:
                address = location.address
                day_of_week = location.day_of_the_week
                time = location.time

                # 주소가 이미 존재하는지 확인하고, 없으면 새로운 딕셔너리 엔트리 생성
                if address not in meaningful_places_dict:
                    # 해당 주소의 경찰서 정보 가져오기(distance 순으로 정렬)
                    police_list = session.query(models.police_info).filter_by(key = location.key).order_by(models.police_info.distance).limit(3).all()

                    #police_list의 num 속성 제거
                    for police in police_list:
                        del police.num
                        del police.key

                    meaningful_places_dict[address] = {
                        'address': address,
                        'timeInfo': [],
                        'latitude': location.latitude,
                        'longitude': location.longitude,
                        'policeStationInfo' : police_list
                    }

                # 해당 주소의 시간 정보 리스트에 현재 시간 정보가 없으면 추가
                time_info_list = meaningful_places_dict[address]['timeInfo']
                if {'dayOfTheWeek': day_of_week, 'time': time} not in time_info_list:
                    time_info_list.append({'dayOfTheWeek': day_of_week, 'time': time})

            # 결과를 리스트 형태로 변환
            meaningful_places = list(meaningful_places_dict.values())

            result = {
                'meaningfulPlaces': meaningful_places
            }

            response = {
                'status': 'success',
                'message': 'Meaningful location data sent',
                'result': result
            }

            print(f"[INFO] Meaningful location data sent to {_key}")

        else:
            print(f"[ERROR] Meaningful location data not found for {_key}")
            raise HTTPException(status_code=404, detail="Meaningful location data not found")

        return response

    finally:
        session.close()

@router.get("/locations/history", responses = {200 : {"model" : LocHistoryResponse, "description" : "위치 이력 전송 성공" }, 404: {"model": ErrorResponse, "description": "위치 이력 없음"}}, description="보호 대상자의 위치 이력 정보 전달(쿼리 스트링) | distance는 현재 값과 다음 값과의 거리 | date : YYYY-MM-DD")
async def send_location_history(date: str, dementiaKey: str):
    _key = dementiaKey

    try:
        location_list = session.query(models.location_info).filter_by(dementia_key=_key, date=date).all()

        if not location_list:
            print(f"[ERROR] Location history data not found for {_key}")
            raise HTTPException(status_code=404, detail="Location history data not found")

        locHistory = []
        prev_location = None

        for index, location in enumerate(location_list):
            current_location = (location.latitude, location.longitude)
            distance = 0

            if prev_location:
                distance = round(haversine(current_location, prev_location, unit='m'), 2)

            if not locHistory or location.user_status != "정지" or locHistory[-1]['userStatus'] != "정지":
                locHistory.append({
                    'latitude': location.latitude,
                    'longitude': location.longitude,
                    'time': location.time,
                    'userStatus': location.user_status,
                    'distance': distance
                })
            else:
                locHistory[-1]['time'] = locHistory[-1]['time'][:8] + '~' + location.time

            prev_location = current_location

            # If it's the last location, set distance to 0
            if index == len(location_list) - 1:
                locHistory[-1]['distance'] = 0

        result = {
            'locationHistory': locHistory,
        }

        response = {
            'status': 'success',
            'message': 'Location history data sent',
            'result': result
        }

        print(f"[INFO] Location history data sent to {_key}")

    finally:
        session.close()

    return response

@router.get("/locations/predict", responses = {200 : {"model" : PredictLocationResponse, "description" : "위치 예측 성공" }, 404: {"model": ErrorResponse, "description": "위치 정보 부족"}}, description="보호 대상자의 다음 위치 예측(쿼리 스트링) | 경찰서 정보는 아직임")
async def predict_location(dementiaKey: str):
    _key = dementiaKey

    try:
        loc_list = []
        location_list = session.query(models.location_info).filter_by(dementia_key=_key, date = "2024-05-16").all()



        for location in location_list:
            status = convertor(location.user_status)
            loc_list.append({
                'date' : location.date,
                'time': location.time,
                'latitude': location.latitude,
                'longitude': location.longitude,
                'user_status' : status
            })
        # dataframe으로 변환
        
        loc_list_df = pd.DataFrame(loc_list, columns=['date', 'time', 'latitude', 'longitude', 'user_status'])

        '''meaningful_list = session.query(models.meaningful_location_info).filter_by(dementia_key = _key).all()

        mean_list = []

        for location in meaningful_list:
            mean_list.append({
                'date' : location.date,
                'time': location.time,
                'latitude': location.latitude,
                'longitude': location.longitude,
                'address' : location.address,
                'key' : location.key
            })'''
        
        #meaningful_df = pd.DataFrame(mean_list, columns=['date', 'time', 'latitude', 'longitude', 'address', 'key'])
        pr = Preprocessing(loc_list_df)
        df, meaningful_df= pr.run_analysis()

        test_idx = int(len(df) * 0.8)
        df_train = df.iloc[:test_idx]
        df_test = df.iloc[test_idx:]

        seq_len = 5  # 150개의 데이터를 feature로 사용
        steps = 5  # 향후 150개 뒤의 y를 예측
        single_output = False
        metrics = ["accuracy"]  # 모델 성능 지표
        lstm_params = {
            "seq_len": seq_len,
            "epochs": 100,  # epochs 반복 횟수
            "patience": 30,  # early stopping 조건
            "steps_per_epoch": 5,  # 1 epochs 시 dataset을 5개로 분할하여 학습
            "learning_rate": 0.03,
            "lstm_units": [64, 32],  # Dense Layer: 2, Unit: (64, 32)
            "activation": "softmax",
            "dropout": 0,
            "validation_split": 0.3,  # 검증 데이터셋 30%
        }
        fl = ForecastLSTMClassification(class_num=len(df['y'].unique()))
        model = fl.fit_lstm(
            df=df_train,
            steps=steps,
            single_output=single_output,
            verbose=True,
            metrics=metrics,
            **lstm_params,
        )
        y_pred = fl.pred(df=df_test, 
                    steps=steps, 
                    num_classes=len(df['y'].unique()),
                    seq_len=seq_len, 
                    single_output=single_output)

        print(y_pred)
        print(meaningful_df.iloc[y_pred].iloc[-1])

        pred_loc = meaningful_df.iloc[y_pred].iloc[-1]

        geo = kakao.geo_coord2address(pred_loc.longitude, pred_loc.latitude)

        if not geo['documents'][0]['road_address'] == None:
            xy2addr = geo['documents'][0]['road_address']['address_name'] + " " + geo['documents'][0]['road_address']['building_name']
                    
        else:
            xy2addr = geo['documents'][0]['address']['address_name']

        police = kakao.search_keyword("경찰서", x = pred_loc.longitude, y = pred_loc.latitude, sort = 'distance')\
        
        '''police_list = []
        if police['meta']['total_count'] == 0:
            print(f"[INFO] No police station found near {xy2addr}")
        else:
            for pol in police['documents']:
                if not pol['phone'] == '':
                    new_police = {
                        "policeName" :  pol['place_name'],
                        "policeAddress" : pol['road_address_name'],
                        "policePhoneNumber" : pol['phone'],
                        "distance" : pol['distance'],
                        "latitude" : pol['y'],
                        "longitude" : pol['x']
                        }
                    
                    police_list.append(new_police)
                else:
                    pass'''
        
        pol_info = {
            "policeName" : "이름",
            "policeAddress" : "주소",
            "policePhoneNumber" : "전번",
            "distance" : "거리",
            "latitude" : "위도",
            "longitude" : "경도"
        }
        pred_loc = {
            "latitude" : pred_loc.latitude,
            "longitude" : pred_loc.longitude,
            "address" : xy2addr
        }
        result = {
            "predictLocation" : pred_loc,
            "policeInfo" : pol_info

        }
        response = {
            "status" : "susccess",
            "message" : "predict complete",
            "result" : result
        }

        return response
    finally:
        session.close()

@router.get("/locations/predict/gura")
async def predict_location(dementiaKey : str):
    
    try:
        # db 에서 의미장소 정보 가져오기
        meaningful_location_list = session.query(models.meaningful_location_info).filter_by(dementia_key = dementiaKey, address = '경기도 시흥시 산기대학로 237 한국공학대학교').limit(1).all()
        police_info = session.query(models.police_info).filter_by(key = meaningful_location_list[0].key).order_by(models.police_info.distance).limit(3).all()

        for police in police_info:
            del police.num
            del police.key

        pred_loc = {
            "latitude" : meaningful_location_list[0].latitude,
            "longitude" : meaningful_location_list[0].longitude,
            "address" : meaningful_location_list[0].address
        }

        result = {
            'predictLocation' : pred_loc,
            'policeInfo' : police_info
        }

        response = {
            'status': 'success',
            'message': 'Predict location data sent',
            'result': result
        
        }

        #time.sleep(10)

        return response
    finally:
        session.close()

@router.post("/safeArea/register", status_code=status.HTTP_201_CREATED, responses = {201 : {"model" : CommonResponse, "description" : "안전 지역 등록 성공" }, 404: {"model": ErrorResponse, "description": "보호 대상자 키 조회 실패"}}, description="보호 대상자의 안전 지역을 등록")
async def register_safe_area(request: RegisterSafeAreaRequest):
    try:
        _dementia_key = request.dementiaKey
        _area_name = request.areaName
        _latitude = request.latitude
        _longitude = request.longitude
        _radius = request.radius
        _group_name = request.groupName

        if _group_name == '':
            _group_name = 'notGrouped'
        else:
            pass

        existing_group = session.query(models.safe_area_group_info).filter_by(group_name = _group_name).first()

        if existing_group:
            _group_key = existing_group.group_key
        else:
            rng = RandomNumberGenerator()
            for _ in range(10):
                _group_key = rng.generate_unique_random_number(100000, 999999)

            new_group = models.safe_area_group_info(group_key = _group_key, group_name = _group_name)
            session.add(new_group)
        
        _area_key = int(_dementia_key) + datetime.timestamp(datetime.now(timezone('Asia/Seoul'))) + ord(_area_name[0])
        new_area = models.safe_area_info(area_key = _area_key, dementia_key = _dementia_key, area_name = _area_name, latitude = _latitude, longitude = _longitude, radius = _radius, group_key = _group_key)

        session.add(new_area)
        
        session.commit()

        print(f"[INFO] Safe area registered for {_dementia_key}")

        response = {
            'status': 'success',
            'message': 'Safe area registered'
        }

        return response
    
    finally:
        session.close()

@router.get("/safeArea/info", responses = {200 : {"model" : GetSafeAreaResponse, "description" : "안전 지역 정보 전송 성공" }, 404: {"model": ErrorResponse, "description": "안전 지역 정보 없음"}}, description="보호 대상자의 안전 지역 정보 전달(쿼리 스트링) | 그룹 미지정시 groupName은 큰따옴표로 빈 문자열로 전달할 것")
async def get_safe_area_info(dementiaKey: str):
    try:
        group_list = session.query(models.safe_area_group_info).filter_by(dementia_key = dementiaKey).all()

        group_lists = []

        # 그룹별로 저장
        for group in group_list:
            safe_area_list = session.query(models.safe_area_info).filter_by(group_key = group.group_key).all()

            safe_areas = []
            for safe_area in safe_area_list:
                safe_areas.append({
                    'areaName': safe_area.area_name,
                    'latitude': safe_area.latitude,
                    'longitude': safe_area.longitude,
                    'radius': safe_area.radius
                })
            
            group_lists.append({
                'groupName': group.group_name,
                'safeAreas': safe_areas
            })

        
        result = {
            'safeAreaList': group_lists
        }

        response = {
            'status': 'success',
            'message': 'Safe area information sent',
            'result': result
        }
            
        
        return response
    
    finally:
        session.close()

@router.get("/safeArea/info/group", responses = {200 : {"model" : GetSafeAreaGroupResponse, "description" : "안전 지역 그룹 정보 전송 성공" }, 404: {"model": ErrorResponse, "description": "안전 지역 그룹 정보 없음"}}, description="보호 대상자의 안전 지역 그룹 정보 전달(쿼리 스트링)")
async def get_safe_area_group_info(dementiaKey: str, groupName: str):
    try:
        group_key = session.query(models.safe_area_group_info).filter_by(dementia_key = dementiaKey, group_name = groupName).first().group_key

        if group_key:
            safe_area_list = session.query(models.safe_area_info).filter_by(group_key = group_key).all()

            safe_areas = []
            for safe_area in safe_area_list:
                safe_areas.append({
                    'areaName': safe_area.area_name,
                    'latitude': safe_area.latitude,
                    'longitude': safe_area.longitude,
                    'radius': safe_area.radius
                })

            result = {
                'safeAreas': safe_areas
            }

            response = {
                'status': 'success',
                'message': 'Safe area group information sent',
                'result': result
            }

            return response
    finally:
        session.close()

@router.post("/safeArea/modification/name", responses = {200 : {"model" : CommonResponse, "description" : "안전 지역 정보 수정 성공" }, 404: {"model": ErrorResponse, "description": "안전 지역 정보 없음"}}, description="보호 대상자의 안전 지역 정보 수정")
async def modify_name_safe_area_info(request: ModifySafeAreaName):
    try:
        _dementaia_key = request.dementiaKey
        _before_name = request.beforeAreaName
        _after_name = request.afterAreaName

        existing_area = session.query(models.safe_area_info).filter_by(dementia_key = _dementaia_key, area_name = _before_name).first()

        if existing_area:
            existing_area.area_name = _after_name
            session.commit()

            print(f"[INFO] Safe area name modified for {_dementaia_key}")

            response = {
                'status': 'success',
                'message': 'Safe area name modified'
            }

            return response
        

    finally:
        session.close()

@router.post("/safeArea/modification/group", responses = {200 : {"model" : CommonResponse, "description" : "안전 지역 정보 수정 성공" }, 404: {"model": ErrorResponse, "description": "안전 지역 정보 없음"}}, description="보호 대상자의 안전 지역 그룹 정보 수정")
async def modify_group_safe_area_info(request: ModifySafeAreaGroup):
    try:
        _dementia_key = request.dementiaKey
        _area_name = request.areaName
        _group_name = request.groupName

        existing_area = session.query(models.safe_area_info).filter_by(dementia_key = _dementia_key, area_name = _area_name).first()

        if existing_area:
            before_group = session.query(models.safe_area_group_info).filter_by(group_key = existing_area.group_key).first()
            after_group = session.query(models.safe_area_group_info).filter_by(group_name = _group_name, dementia_key = _dementia_key).first()
            
            if after_group:
                existing_area.group_key = after_group.group_key
            else:
                rng = RandomNumberGenerator()
                for _ in range(10):
                    _group_key = rng.generate_unique_random_number(100000, 999999)

                new_group = models.safe_area_group_info(group_key = _group_key, group_name = _group_name)
                session.add(new_group)
                existing_area.group_key = _group_key
            
            if session.query(models.safe_area_info).filter_by(group_key = before_group.group_key).count() == 0:
                session.delete(before_group)

            else:
                pass

            session.commit()
            
            print(f"[INFO] Safe area group modified for {_dementia_key}")

            response = {
                'status': 'success',
                'message': 'Safe area group modified'
            }

            return response
        
        else:
            return ErrorResponse(status_code=404, message="Safe area information not found")
        
        
    finally:
        session.close()

@router.post("/safeArea/modification/groupName", responses = {200 : {"model" : CommonResponse, "description" : "안전 지역 그룹 정보 수정 성공" }, 404: {"model": ErrorResponse, "description": "안전 지역 그룹 정보 없음"}}, description="보호 대상자의 안전 지역 그룹 이름 수정")
async def modify_group_name_safe_area_info(request: ModifySafeAreaGroupName):
    try:
        _dementia_key = request.dementiaKey
        _before_group_name = request.beforeGroupName
        _after_group_name = request.afterGroupName

        existing_group = session.query(models.safe_area_group_info).filter_by(dementia_key = _dementia_key, group_name = _before_group_name).first()

        if existing_group:
            existing_group.group_name = _after_group_name
            session.commit()

            print(f"[INFO] Safe area group name modified for {_dementia_key}")

            response = {
                'status': 'success',
                'message': 'Safe area group name modified'
            }

            return response
        
        else:
            return ErrorResponse(status_code=404, message="Safe area group information not found")
        
    finally:
        session.close()

'''@sched.scheduled_job('cron', hour=11, minute=57, id = 'analyze_location_data')
def analyzing_location_data():
    asyncio.run(schedFunc.load_analyze_location_data(session))'''