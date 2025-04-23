import os
import json

from util import isWeakCommit

historyFinderOracleDirectory = "../src/main/resources/oracle"
codeShovelOracleDirectory = "../src/main/resources/stubs/input"

# Get only files
historyFinderFiles = [f for f in os.listdir(historyFinderOracleDirectory) if os.path.isfile(os.path.join(historyFinderOracleDirectory, f))]
historyFinderFiles = list(sorted(filter(lambda f: int(f.split('-')[0]) <= 200, historyFinderFiles)))
print(historyFinderFiles)
for f in historyFinderFiles:
    codeShovelWeakCommitSet = set()
    codeTrackerWeakCommitSet = set()
    with open(historyFinderOracleDirectory + f, 'r') as file    :
        trace = json.loads(file)
        for commit in trace['commits']:
            commitHash = commit['commitHash']
            tags = commit['changeTags']
            if isWeakCommit(tags, ['ANNOTATION', 'FORMAT', 'DOCUMENTATION']):
                codeShovelWeakCommitSet.add(commitHash)
            if isWeakCommit(tags, ['FORMAT', 'DOCUMENTATION']):
                codeTrackerWeakCommitSet.add(commitHash)



