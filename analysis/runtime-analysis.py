from src.util import toUpperFirst
import config

trace = config.db["trace"]
boxPlotData = []
for dataset in config.datasetList:
    oracleName = dataset['oracleName']
    print(f'{dataset["oracleName"]}', end='')
    scoreMap = {tracerName : [] for tracerName in config.tracerList}


    for r in trace.find({'oracleFileId': {'$gte': dataset['range'][0], '$lte': dataset['range'][-1]}}, sort = [("oracleFileId", 1)], allow_disk_use = True).limit(1000):
        try:
            for tracerName in config.tracerList:
                if tracerName in r.get('analysis'):
                    analysis = r.get('analysis')[tracerName]
                    scoreMap[tracerName].append(analysis['runtime'])
                else:
                    #print(f'Warning: {oracleName} {dataset["range"]} {r.get("oracleFileId")} {tracerName} is not in analysis')
                    pass
        except Exception as e:
            oracleFileId = r.get('oracleFileId')
            print(f'Oracle File ID : {oracleFileId} \n{e}')
            raise e
    boxPlotData.append([oracleName, scoreMap])


