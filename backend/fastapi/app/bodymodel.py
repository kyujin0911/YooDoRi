from pydantic import BaseModel
from typing import Optional, Dict, Optional, Union, List

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
    keyFromDementia : int
    nokName : str
    nokPhoneNumber : str

class ReceiveNokInfoResponse(BaseModel):
    status: str
    message: str
    result: nokResult


class dementiaResult(BaseModel):
    dementiaKey: str

class ReceiveDementiaInfoRequest(BaseModel):
    name : str
    phoneNumber : str

class ReceiveDementiaInfoResponse(BaseModel):
    status: str
    message: str
    result: dementiaResult


class connectionResult(BaseModel):
    nokInfoRecord: nokInfoRecord

class ConnectionRequest(BaseModel):
    dementiaKey : int

class ConnectionResponse(BaseModel):
    status: str
    message: str
    result: connectionResult

class loginRequest(BaseModel):
    key : int
    isDementia : int

class ReceiveLocationRequest(BaseModel):
    dementiaKey : int
    date : str
    time : str
    latitude : float
    longitude : float
    bearing : float
    accelerationSensor : List[float] # list of 3 floats
    gyroSensor : List[float] #list of 3 floats
    directionSensor : List[float] #list of 3 floats
    lightSensor : List[float]
    battery : int
    isInternetOn : bool
    isGpsOn : bool
    isRingstoneOn : int
    currentSpeed : float

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
    key : int
    isDementia : int
    name : str
    phoneNumber : str

class ModifyUserUpdateRateRequest(BaseModel):
    key : int
    isDementia : int
    updateRate : int

class AverageWalkingSpeedRequest(BaseModel):
    dementiaKey : int

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
