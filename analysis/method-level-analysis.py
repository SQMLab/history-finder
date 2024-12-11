from src.util import toUpperFirst
import config

trace = config.db["trace"]
for dataset in config.datasetList:
    oracleName = dataset['oracleName']
    print(f'{dataset["oracleName"]}', end='')
    scoreMap = {tracerName : {'precision': 0, 'recall': 0, 'f1Score': 0, 'count': 0} for tracerName in config.tracerList}


    for r in trace.find({'oracleFileId': {'$gte': dataset['range'][0], '$lte': dataset['range'][-1]}}, sort = [("oracleFileId", 1)], allow_disk_use = True).limit(1000):
        try:
            for tracerName in config.tracerList:
                if tracerName in r.get('analysis'):
                    analysis = r.get('analysis')[tracerName]
                    scoreMap[tracerName]['precision'] += analysis['precision']
                    scoreMap[tracerName]['recall'] += analysis['recall']
                    #scoreMap[tracerName]['f1Score'] += analysis['f1Score']
                    scoreMap[tracerName]['count'] += 1
                else:
                    #print(f'Warning: {oracleName} {dataset["range"]} {r.get("oracleFileId")} {tracerName} is not in analysis')
                    pass
        except Exception as e:
            oracleFileId = r.get('oracleFileId')
            print(f'Oracle File ID : {oracleFileId} \n{e}')
            raise e
    for tracerName in config.tracerList:
        count = scoreMap[tracerName]['count']
        print(f'    & {toUpperFirst(tracerName)} & {scoreMap[tracerName]["precision"]/count:.2f} & {scoreMap[tracerName]["recall"]/count:.2f} & {scoreMap[tracerName]["f1Score"]/count:.2f} \\\\')
    print(f'\\hline')
