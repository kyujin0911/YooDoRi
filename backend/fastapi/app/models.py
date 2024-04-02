from sqlalchemy import Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class nok_info(Base):
    __tablename__ = 'nok_info'

    num = Column(Integer, index=True)
    nok_key = Column(String, primary_key=True)
    nok_name = Column(String)
    nok_phonenumber = Column(String)
    update_rate = Column(String)
    dementia_info_key = Column(String)

class dementia_info(Base):
    __tablename__ = 'dementia_info'

    num = Column(Integer, index = True)
    dementia_key = Column(String, primary_key=True)
    dementia_name = Column(String)
    dementia_phonenumber = Column(String)
    update_rate = Column(String)

class location_info(Base):
    __tablename__ = 'location_info'

    num = Column(Integer, index=True, primary_key=True)
    dementia_key = Column(String)
    date = Column(String)
    time = Column(String)
    latitude = Column(String)
    longitude = Column(String)
    bearing = Column(String)
    user_status = Column(Integer)
    accelerationsensor_x = Column(String)
    accelerationsensor_y = Column(String)
    accelerationsensor_z = Column(String)
    directionsensor_x = Column(String)
    directionsensor_y = Column(String)
    directionsensor_z = Column(String)
    gyrosensor_x = Column(String)
    gyrosensor_y = Column(String)
    gyrosensor_z = Column(String)
    lightsensor = Column(String)
    battery = Column(Integer)
    isInternetOn = Column(Integer)
    isRingstoneOn = Column(Integer)
    isGpsOn = Column(Integer)
    current_speed = Column(String)

class meaningful_location_info(Base):
    __tablename__ = 'meaningful_location_info'

    num = Column(Integer, index=True, primary_key=True)
    dementia_key = Column(String)
    day_of_the_week = Column(String)
    time = Column(String)
    latitude = Column(String)
    longitude = Column(String)
