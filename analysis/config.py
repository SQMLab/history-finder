from pymongo import MongoClient

client = MongoClient("mongodb://cto:cto123@localhost:27017/cto?authSource=admin")
db = client["cto"]
datasetList = [{'oracleName': 'CodeShovel\\nTraining', 'oracleTye': 'Training', 'range': [1, 100]},
               {'oracleName': 'CodeShovel\\nTesting', 'oracleTye': 'Testing', 'range': [101, 200]},
               {'oracleName': 'HistoryFinder\\nTesting', 'oracleTye': 'Training', 'range': [201, 400]}]
tracerList = ['codeShovel', 'codeTracker', 'historyFinder', 'intelliJ', 'gitFuncName', 'gitLineRange']

