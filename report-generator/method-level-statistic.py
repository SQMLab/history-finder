import math

import numpy as np

from util import toUpperFirst
import config

trace = config.db["trace"]
for dataset in config.datasetList:
    oracleName = dataset['oracleName']
    print(f'{dataset["oracleName"]}')
    scoreMap = {tracerName : {'precision': [], 'recall': [], 'f1Score': []} for tracerName in config.tracerList}


    for r in trace.find({'oracleFileId': {'$gte': dataset['range'][0], '$lte': dataset['range'][-1]}}, sort = [("oracleFileId", 1)], allow_disk_use = True).limit(1000):
        try:
            for tracerName in config.tracerList:
                if tracerName in r.get('analysis'):
                    analysis = r.get('analysis')[tracerName]
                    if math.isnan(analysis['precision']) is not True:
                        scoreMap[tracerName]['precision'].append(analysis['precision'])
                    if math.isnan(analysis['recall']) is not True:
                        scoreMap[tracerName]['recall'].append(analysis['recall'])
                    if math.isnan(analysis['f1Score']) is not True:
                        scoreMap[tracerName]['f1Score'].append(analysis['f1Score'])
                else:
                    #print(f'Warning: {oracleName} {dataset["range"]} {r.get("oracleFileId")} {tracerName} is not in analysis')
                    pass
        except Exception as e:
            oracleFileId = r.get('oracleFileId')
            print(f'Oracle File ID : {oracleFileId} \n{e}')
            raise e
    for tracerName in config.tracerList:
        print(f'    & {toUpperFirst(tracerName)} & {np.mean(np.array(scoreMap[tracerName]["precision"]))*100:.2f} & {np.mean(np.array(scoreMap[tracerName]["recall"]))*100:.2f} & {np.mean(np.array(scoreMap[tracerName]["f1Score"]))*100:.2f} \\\\')
    print(f'\\hline')
