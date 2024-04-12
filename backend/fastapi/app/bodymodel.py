from pydantic import BaseModel, Field
from typing import Optional, Dict, Optional, Union, List
from fastapi import HTTPException

# Define request and response models
class CommonResponse(BaseModel):
    status: str
    message: str

class dementiaInfoRecord(BaseModel):
    dementiaKey : str
    dementiaName : str
    dementiaPhoneNumber : str

class nokInfoRecord(BaseModel):
    nokKey : str
    nokName : str
    nokPhoneNumber : str

class UserRecord(BaseModel):
    dementiaInfoRecord: dementiaInfoRecord
    nokInfoRecord: nokInfoRecord

class nokResult(BaseModel):
    dementiaInfoRecord: dementiaInfoRecord
    nokKey: str

class ReceiveNokInfoRequest(BaseModel):
    keyFromDementia : int = Field(examples=["123456"])
    nokName : str = Field(examples=["홍길동"])
    nokPhoneNumber : str = Field(examples=["010-1234-5678"])

class ReceiveNokInfoResponse(BaseModel):
    status: str
    message: str
    result: nokResult


class dementiaResult(BaseModel):
    dementiaKey: str

class ReceiveDementiaInfoRequest(BaseModel):
    name : str = Field(examples=["성춘향"])
    phoneNumber : str = Field(examples=["010-1234-5678"])

class ReceiveDementiaInfoResponse(BaseModel):
    status: str
    message: str
    result: dementiaResult


class connectionResult(BaseModel):
    nokInfoRecord: nokInfoRecord

class ConnectionRequest(BaseModel):
    dementiaKey : int = Field(examples=["123456"])

class ConnectionResponse(BaseModel):
    status: str
    message: str
    result: connectionResult

class loginRequest(BaseModel):
    key : int = Field(examples=["123456"])
    isDementia : int = Field(examples=["1"])

class ReceiveLocationRequest(BaseModel):
    dementiaKey : int = Field(examples=["123456"])
    date : str = Field(examples=["2024-03-19"])
    time : str = Field(examples=["12:00:00"])
    latitude : float = Field(examples=["37.123456"])
    longitude : float = Field(examples=["127.123456"])
    bearing : float = Field(examples=["0.0"])
    accelerationSensor : List[List[float]] = Field(..., examples=[[-1.84068, 6.68136, 6.0359]])
    gyroSensor : List[float] = Field(..., examples=[[-1.84068, 6.68136, 6.0359]])
    directionSensor : List[float] = Field(..., examples=[[-1.84068, 6.68136, 6.0359]])
    lightSensor : List[float] = Field(examples=[500.0])
    battery : int = Field(examples=["100"])
    isInternetOn : bool = Field(examples=["true"])
    isGpsOn : bool = Field(examples=["true"])
    isRingstoneOn : int = Field(examples=["1"])
    currentSpeed : float = Field(examples=["0.0"])

class LastLoc(BaseModel):
    latitude : float
    longitude : float
    bearing : float
    currentSpeed : float
    userStatus : int
    battery : int
    isInternetOn : bool
    isGpsOn : bool
    isRingstoneOn : int


class GetLocationResponse(BaseModel):
    status: str
    message: str
    result: LastLoc

class ModifyUserInfoRequest(BaseModel):
    key : int = Field(examples=["123456"])
    isDementia : int = Field(examples=["1"])
    name : str = Field(examples=["김이름"])
    phoneNumber : str = Field(examples=["010-1234-5678"])

class ModifyUserUpdateRateRequest(BaseModel):
    key : int = Field(examples=["123456"])
    isDementia : int = Field(examples=["1"])
    updateRate : int = Field(examples=["15"])

class AverageWalkingSpeedRequest(BaseModel):
    dementiaKey : int = Field(examples=["123456"])

class AverageAndLastLoc(BaseModel):
    averageSpeed : float
    lastLatitude : float
    lastLongitude : float

class AverageWalkingSpeedResponse(BaseModel):
    status: str
    message: str
    result: AverageAndLastLoc


class GetUserInfoResponse(BaseModel):
    status: str
    message: str
    result: UserRecord

class MeaningfulLoc(BaseModel):
    dayOfTheWeek : str
    time : str
    latitude : float
    longitude : float

class MeaningfulLocRecord(BaseModel):
    meaningfulLocations : List[MeaningfulLoc]

class MeaningfulLocResponse(BaseModel):
    status: str
    message: str
    result: MeaningfulLocRecord

class LocHis(BaseModel):
    latitude : float
    longitude : float
    time : str

class LocHisRecord(BaseModel):
    locationHistory : List[LocHis]

class LocHistoryResponse(BaseModel):
    status: str
    message: str
    result : LocHisRecord

class CustomHTTPException(HTTPException):
    def __init__(self, status_code: int, detail: str):
        super().__init__(status_code=status_code, detail={"status": "error", "message": detail})

class ErrorResponse(BaseModel):
    status: str
    message: str