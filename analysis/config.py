from pymongo import MongoClient

client = MongoClient("mongodb://cto:cto123@localhost:27017/cto?authSource=admin")
db = client["cto"]
datasetList = [{'oracleName': 'CodeShovel Training Dataset', 'oracleTye': 'Training', 'range': [1, 100]},
               {'oracleName': 'CodeShovel Testing Dataset', 'oracleTye': 'Testing', 'range': [101, 200]},
               {'oracleName': 'HistoryFinder Testing Dataset', 'oracleTye': 'Training', 'range': [201, 400]}]
tracerList = ['codeShovel', 'codeTracker', 'historyFinder', 'intelliJ', 'gitLineRange', 'gitFuncName']

