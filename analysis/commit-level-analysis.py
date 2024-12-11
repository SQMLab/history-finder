from src.util import toUpperFirst
import config

trace = config.db["trace"]
for dataset in config.datasetList:
    oracleName = dataset['oracleName']
    print(f'{dataset["oracleName"]}', end='')
    for tracerName in config.tracerList:
        tp = 0
        fp = 0
        fn = 0
        for r in trace.find({'oracleFileId': {'$gte': dataset['range'][0], '$lte': dataset['range'][-1]}}, sort = [("oracleFileId", 1)], allow_disk_use = True).limit(1000):
            try:
                if tracerName in r.get('analysis'):
                    analysis = r.get('analysis')[tracerName]
                    tp += len(analysis['correctCommits'])
                    fp += len(analysis['incorrectCommits'])
                    fn += len(analysis['missingCommits'])
                else:
                    #print(f'Warning: {oracleName} {dataset["range"]} {r.get("oracleFileId")} {tracerName} is not in analysis')
                    pass
            except Exception as e:
                oracleFileId = r.get('oracleFileId')
                print(f'Oracle File ID : {oracleFileId} \n{e}')
                raise e
        precision = tp / (tp + fp)
        recall = tp / (tp + fn)
        f1Score = 2 * precision * recall / (precision + recall)
        print(f'    & {toUpperFirst(tracerName)} & {tp} & {fp} & {fn} & {precision*100:.2f} & {recall*100:.2f} & {f1Score*100:.2f} \\\\')
    print(f'\\hline')
