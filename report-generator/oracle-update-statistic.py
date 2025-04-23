import os
import json

from util import isWeakCommit, toUpperFirst

baseDirectorMap = {
    'historyFinder': "../src/main/resources/oracle/",
    'codeShovel': "../src/main/resources/stubs/input/",
    'codeTracker': '/home/cs/grad/islams32/dev/rnd/history finder/oracle/codetracker/'
}

OLD_ORACLES = ['codeShovel', 'codeTracker']

WEAK_TAG_MAP = {'codeShovel': ['ANNOTATION', 'FORMAT', 'DOCUMENTATION'],
                'codeTracker': ['FORMAT', 'DOCUMENTATION']}
# Get only files
historyFinderFiles = [f for f in os.listdir(baseDirectorMap['historyFinder']) if
                      os.path.isfile(os.path.join(baseDirectorMap['historyFinder'], f))]
historyFinderFiles = list(sorted(filter(lambda f: int(f.split('-')[0]) <= 200, historyFinderFiles)))
assert len(historyFinderFiles) == 200, f'There should be 200 method histories but found {len(historyFinderFiles)}'
updateList = []
commitCountMap = {
    'historyFinder': 0,
    'codeShovel': 0,
    'codeTracker': 0
}
for f in historyFinderFiles:
    jsonFileMap = {
        'historyFinder': json.load(open(baseDirectorMap['historyFinder'] + f, 'r')),
        'codeShovel': json.load(open(baseDirectorMap['codeShovel'] + f[4:], 'r')),
        'codeTracker': json.load(open(baseDirectorMap['codeTracker'] + (f[6:] if f[4:6].upper() == 'Z_' else f[4:]), 'r'))
    }
    commitSetMap = {
        'historyFinder': set([commit['commitHash'] for commit in jsonFileMap['historyFinder']['commits']]),
        'codeShovel': set(jsonFileMap['codeShovel']['expectedResult'].keys()),
        'codeTracker': set([commit['commitId'] for commit in jsonFileMap['codeTracker']['expectedChanges']])
    }
    for oracleKey, commitSet in commitSetMap.items():
        commitCountMap[oracleKey] += len(commitSet)

    update = {oldOracleName: {'stronglyAddedCommits': set(), 'addedCommits': set(), 'removedCommits': set()} for oldOracleName in OLD_ORACLES}
    update['file'] = f

    for oracleName in OLD_ORACLES:
        for c in commitSetMap[oracleName]:
            if c not in commitSetMap['historyFinder']:
                update[oracleName]['removedCommits'].add(c)

    for commit in jsonFileMap['historyFinder']['commits']:
        commitHash = commit['commitHash']
        tags = commit['changeTags']
        for oracleName in OLD_ORACLES:
            if commitHash not in commitSetMap[oracleName]:
                update[oracleName]['addedCommits'].add(commitHash)
                if not isWeakCommit(tags, WEAK_TAG_MAP[oracleName]):
                    update[oracleName]['stronglyAddedCommits'].add(commitHash)
    updateList.append(update)

updateStatistics = {oracleName: {'stronglyChangedMethodCount': 0,
                                 'changedMethodCount': 0,
                                 'stronglyAddedCommitCount': 0,
                                 'addedCommitCount': 0,
                                 'removedCommitCount': 0}
                    for oracleName in OLD_ORACLES}

for update in updateList:
    for oracleName in OLD_ORACLES:
        stronglyAddedCommitSet = update[oracleName]['stronglyAddedCommits']
        addedCommitSet = update[oracleName]['addedCommits']
        removedCommitSet = update[oracleName]['removedCommits']
        updateStatistics[oracleName]['stronglyAddedCommitCount'] += len(stronglyAddedCommitSet)
        updateStatistics[oracleName]['addedCommitCount'] += len(addedCommitSet)
        updateStatistics[oracleName]['removedCommitCount'] += len(removedCommitSet)
        if len(stronglyAddedCommitSet) > 0 or len(removedCommitSet) > 0:
            updateStatistics[oracleName]['stronglyChangedMethodCount'] += 1
        if len(addedCommitSet) > 0 or len(removedCommitSet) > 0:
            updateStatistics[oracleName]['changedMethodCount'] += 1

for oracleName in OLD_ORACLES:
    print(f'{toUpperFirst(oracleName)} & {updateStatistics[oracleName]["changedMethodCount"]} & {updateStatistics[oracleName]["stronglyChangedMethodCount"]} &  {updateStatistics[oracleName]["addedCommitCount"]} & {updateStatistics[oracleName]["stronglyAddedCommitCount"]} & {updateStatistics[oracleName]["removedCommitCount"]} \\\\')
for oracleKey, commitCount in commitCountMap.items():
    print(f'{toUpperFirst(oracleKey)}  {commitCount}')
#print(updateList)
