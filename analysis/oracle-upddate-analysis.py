import os
import json

from util import isWeakCommit

historyFinderOracleDirectory = "../src/main/resources/oracle"
codeShovelOracleDirectory = "../src/main/resources/stubs/input"

# Get only files
historyFinderFiles = [f for f in os.listdir(historyFinderOracleDirectory) if os.path.isfile(os.path.join(historyFinderOracleDirectory, f))]
historyFinderFiles = list(sorted(filter(lambda f: int(f.split('-')[0]) <= 200, historyFinderFiles)))
# print(historyFinderFiles)
codeShovelUpdate = []
for f in historyFinderFiles:
    with open(historyFinderOracleDirectory + '/' + f, 'r') as historyFinderFile:

        historyFinderTrace = json.load(historyFinderFile)
        historyFinderCommitSet =  set([commit['commitHash'] for commit in historyFinderTrace['commits']])
        codeShovelRemoved = []
        codeShovelAdded = []
        with open(codeShovelOracleDirectory + '/' + f[4:], 'r') as codeShovelFile:
            codeShovelTrace = json.load(codeShovelFile)
            codeShovelCommitSet = set(codeShovelTrace['expectedResult'].keys())
            for c in codeShovelCommitSet:
                if c not in historyFinderCommitSet:
                    codeShovelRemoved.append(c);

            for commit in historyFinderTrace['commits']:
                commitHash = commit['commitHash']
                tags = commit['changeTags']
                if commitHash not in codeShovelCommitSet and not isWeakCommit(tags, ['ANNOTATION', 'FORMAT', 'DOCUMENTATION']):
                    codeShovelAdded.append(commitHash)
                # if isWeakCommit(tags, ['FORMAT', 'DOCUMENTATION']):
                #     codeTrackerWeakCommitSet.add(commitHash)
            if len(codeShovelAdded) > 0 or len(codeShovelRemoved) > 0:
                codeShovelUpdate.append([f, codeShovelAdded, codeShovelRemoved])
print(codeShovelUpdate)
print(len(codeShovelUpdate))


