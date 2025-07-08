import json
import os.path

import config

trace = config.db["trace"]

OUTPUT_DIRECTORY = "../cache/output"
for tracerName in ['codeShovel', 'historyFinder', 'codeTracker']:
    traceDirectory = os.path.join(OUTPUT_DIRECTORY, tracerName)
    if os.path.exists(traceDirectory):
        files = os.listdir(traceDirectory)
        files.sort()
        for fileName in files:
            file = os.path.join(OUTPUT_DIRECTORY, tracerName, fileName)
            if fileName.endswith('.json'):
                oracleFileId = int(fileName.split('-')[0].strip())
                jsonFile = json.load(open(file, 'r'))
                if tracerName == 'codeShovel':
                    runtime = jsonFile['timeTaken']
                elif tracerName == 'historyFinder' or tracerName == 'codeTracker':
                    runtime = jsonFile['executionTime']
                trace.update_one(
                    {"oracleFileId": oracleFileId},
                    {"$set": {f"analysis.{tracerName}.runtime": runtime}}
                )
                # traceDocument = trace.find_one(
                #     {"oracleFileId": oracleFileId}
                # )
                # oldRuntime = traceDocument.get("analysis", {}).get(tracerName, {}).get("runtime")
                # trace.update_one(
                #     {"oracleFileId": oracleFileId},
                #     {"$set": {
                #         f"analysis.{tracerName}.runtimeBk":   oldRuntime
                #     }}
                # )



# for id in [125, 303, 304, 366 ]:
#     trace.update_one(
#                     {"oracleFileId": id},
#                     {"$set": {f"analysis.codeShovel.runtime": None}})