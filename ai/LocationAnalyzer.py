import numpy as np
import pandas as pd
import os
import sys
from pyclustering.cluster.gmeans import gmeans

# 파일 읽기
# 데이터 예시 (39.984702,116.318417,0,492,39744.1201851852,2008-10-23,02:53:04)
# (위도, 경도, 0, 고도, 1899년 이후 경과한 시간, 날짜, 시간)
def fileReader(filename):

    latitude = []   # 위도
    longitude = []  # 경도
    date = []       # 날짜
    time = []       # 시간

    with open(filename, 'r') as file:
        data = file.read()

    # 데이터에 불필요한 부분 제거
    # 추후 데이터 형식에 따라 수정 필요 *
    data = data.split('\n')[6:-1]
    for i in range(len(data)):
        line = data[i].split(',')
        latitude.append(line[0])    # 위도
        longitude.append(line[1])   # 경도
        date.append(line[5])        # 날짜
        time.append(line[6])        # 시간

    df = pd.DataFrame({"latitude":latitude, "longitude":longitude, "date":date, "time":time})

    
    df['latitude'] = df['latitude'].astype(float)
    df['longitude'] = df['longitude'].astype(float)
    df['datetime'] = pd.to_datetime(df['date'] + ' ' + df['time'], format='%Y-%m-%d %H:%M:%S')
    df['datetime'] = df['datetime'].dt.floor('T')
    df = df.drop(['date', 'time'], axis=1)
    df = df.drop_duplicates(['datetime'], ignore_index=True)

    return df

# 의미장소 추출
def gmeansFit(df):
    # 두 열을 선택하고 넘파이 배열로 변환
    selectedColumns = ['latitude', 'longitude']
    resultList = df[selectedColumns].values.tolist()    # 리스트로 변환
    
    gmeansInstance = gmeans(resultList).process()       # 클러스터링

    centers = gmeansInstance.get_centers()              # 클러스터의 중심 (의미장소)
    clusters = gmeansInstance.get_clusters()            # 분류된 클러스터


    return clusters, centers
    
# 호출 함수
def gmeansFunc(df):
    
    j = 0

    clusters, centers = gmeansFit(df)

    data_df = pd.DataFrame({"clusters":clusters, "centers":centers})
        
    for k in range(len(data_df.clusters)):
        if (len(data_df.clusters[k]) < 10):
            data_df.drop(index=k, inplace=True)
    data_df = data_df.sort_index(axis=1)
    data_df = data_df.reset_index(drop=True)

    # map(centers, j)

        
    j += 1
    return data_df

if __name__ == '__main__':
    # 파일 경로 가져오기
    # 지금은 데이터가 저장된 파일의 경로를 실행할 때 입력
    file_path = sys.argv[1]

    df = fileReader(file_path)

    dataDict = gmeansFunc(df)
    
    print(dataDict)
