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

    update = {oldOracleName: {'addedCommits': set(), 'removedCommits': set()} for oldOracleName in OLD_ORACLES}
    update['file'] = f

    for oracleName in OLD_ORACLES:
        for c in commitSetMap[oracleName]:
            if c not in commitSetMap['historyFinder']:
                update[oracleName]['removedCommits'].add(c)

    for commit in jsonFileMap['historyFinder']['commits']:
        commitHash = commit['commitHash']
        tags = commit['changeTags']
        for oracleName in OLD_ORACLES:
            if commitHash not in commitSetMap[oracleName] and not isWeakCommit(tags, WEAK_TAG_MAP[oracleName]):
                update[oracleName]['addedCommits'].add(commitHash)
    updateList.append(update)

updateStatistics = {oracleName: {'changedMethodCount': 0, 'addedCommitCount': 0, 'removedCommitCount': 0}
                    for oracleName in OLD_ORACLES}

for update in updateList:
    for oracleName in OLD_ORACLES:
        addedCommitSet = update[oracleName]['addedCommits']
        removedCommitSet = update[oracleName]['removedCommits']
        updateStatistics[oracleName]['addedCommitCount'] += len(addedCommitSet)
        updateStatistics[oracleName]['removedCommitCount'] += len(removedCommitSet)
        if len(addedCommitSet) > 0 or len(removedCommitSet) > 0:
            updateStatistics[oracleName]['changedMethodCount'] += 1

for oracleName in OLD_ORACLES:
    print(f'{toUpperFirst(oracleName)} & {updateStatistics[oracleName]["changedMethodCount"]} & {updateStatistics[oracleName]["addedCommitCount"]} & {updateStatistics[oracleName]["removedCommitCount"]} \\\\')

print(updateList)
