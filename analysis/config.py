from pymongo import MongoClient

client = MongoClient("mongodb://cto:cto123@localhost:27017/cto?authSource=admin")
LOCAL_REPOSITORY_ROOT_DIRECTORY = '/home/cs/grad/islams32/dev/project/repository/'
GITHUB_TOKEN = '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'
db = client["cto"]
datasetList = [{'oracleName': 'CodeShovel Training Dataset', 'oracleTye': 'Training', 'range': [1, 100]},
               {'oracleName': 'CodeShovel Testing Dataset', 'oracleTye': 'Testing', 'range': [101, 200]},
               {'oracleName': 'HistoryFinder Testing Dataset', 'oracleTye': 'Training', 'range': [201, 400]}]
tracerList = ['codeShovel', 'codeTracker', 'historyFinder', 'intelliJ', 'gitLineRange', 'gitFuncName']

