import json
import os

import matplotlib.pyplot as plt
import numpy as np

from util import toUpperFirst

baseDirectorMap = {
    'codeShovel': "../src/main/resources/stubs/input/",
    'codeTracker': '/home/cs/grad/islams32/dev/rnd/history finder/oracle/codetracker/',
    'codeShovelNew': "../src/main/resources/oracle/",
    'historyFinder': "../src/main/resources/oracle/"
}

fileMap = {oracleKey: [os.path.join(baseDirectory, f) for f in os.listdir(baseDirectory) if
                       os.path.isfile(os.path.join(baseDirectory, f))] for oracleKey, baseDirectory in
           baseDirectorMap.items()}
fileMap['codeShovelNew'] = list(
    sorted(filter(lambda f: int(f.split('/')[-1].split('-')[0]) <= 200, fileMap['codeShovelNew'])))
fileMap['historyFinder'] = list(
    sorted(filter(lambda f: 201 <= int(f.split('/')[-1].split('-')[0]) <= 400, fileMap['historyFinder'])))
TAGS = ['MOVE', 'RENAME', 'BODY', 'MULTI']
commitCountMap = {
    key: {tag: [] for tag in TAGS} for key in fileMap.keys()
}
for oracleKey in fileMap:
    for file in fileMap[oracleKey]:
        commitSet = set()
        jsonFile = json.load(open(file, 'r'))
        tagCountMap = {tag: 0 for tag in TAGS}
        match oracleKey:
            case 'codeShovel':
                commitSet |= jsonFile['expectedResult'].keys()
            case 'codeTracker':
                for changeItem in jsonFile['expectedChanges']:
                    commitSet.add(changeItem['commitId'])
            case 'codeShovelNew' | 'historyFinder':
                for changeItem in jsonFile['commits']:
                    changeTag = changeItem['changeTag']
                    for tag in TAGS:
                        tagCountMap[tag] += 1 if tag in changeTag else 0
                    tagCountMap['MULTI'] += 1 if len(changeTag) > 1 in changeTag else 0
        for tag in TAGS:
            commitCountMap[oracleKey][tag].append(tagCountMap[tag])

boxplotColors = ['#d9a999', '#80c080', '#c080c0', '#a8a8a8', '#e0b88f']
cdfPlotColors = ['brown', 'green', 'purple', 'dimgray', 'peru']

HATCHES = ['xx', '//', '.', 'O.', '*']
MARKERS = ['h', 'd', 'x', '>', '*']
LINE_STYLES = [':', '--', '-.', '-', (0, (4, 2, 1, 2))]
cdfFigure, cdfAxes = plt.subplots(2, 2, figsize=(10, 10), sharey=False)
for subplotIndex in range(2):
    cdfPlot = cdfAxes[subplotIndex] if isinstance(cdfAxes, list) else cdfAxes

    step = 10
    cdfIndex = 0
    for oracleKey, commitCountSeq in commitCountMap.items():
        commitCounts = list(commitCountSeq)
        commitCounts = np.sort(commitCounts)
        label = toUpperFirst(oracleKey) if subplotIndex == 0 else ''

        # step += tracerIndex + 1
        print(commitCounts, end=',')
        # Set x-axis labels and title
        cdf = np.arange(1, len(commitCounts) + 1) / len(commitCounts)

        x_sub = commitCounts[cdfIndex::step]
        y_sub = cdf[cdfIndex::step]
        # x_sub = commitCounts[::]
        # y_sub = cdf[::]

        cdfPlot.plot(x_sub, y_sub, label=label, color=cdfPlotColors[cdfIndex], linewidth=3,
                     markersize=10,
                     markeredgewidth=2,
                     linestyle=LINE_STYLES[cdfIndex], marker=MARKERS[cdfIndex])
        cdfIndex += 1

    # cdfPlot.set_xlim(0, cdfPlotRuntimeLimitAndStepSize[datasetIndex][0])
    # cdfPlot.set_title(datasetLabels[datasetIndex])
    # cdfPlot.set_yticks(np.arange(0, 1.1, 0.1))
    # cdfPlot.set_xticks(np.arange(0, cdfPlotRuntimeLimitAndStepSize[datasetIndex][0] + 1,
    #                              cdfPlotRuntimeLimitAndStepSize[datasetIndex][1]))
    cdfPlot.grid(axis='both', linestyle='--', alpha=0.5)
    # boxPlot.legend()
    if subplotIndex == 0:
        cdfPlot.set_ylabel("CDF")
        cdfPlot.legend(title="Change Types", loc='lower right')

cdfFigure.supxlabel('Number of revisions')
#
# # Apply consistent colors to boxes
# for i, patch in enumerate(box['boxes']):
#     patch.set_facecolor(boxColors[i % len(boxColors)])  # Cycle through colors
#
# # Set x-axis labels for groups with spacing
# ax.set_xticks(datasetLabelPositions)
# ax.set_xticklabels(datasetLabels, rotation=45, ha="right")
#
# # Add legend for color mapping
# for i, color in enumerate(boxColors):
#     ax.plot([], [], color=color, label=f'{toUpperFirst(tracerList[i])}')

# # Add legend, title, and axis labels
# ax.legend(title="Tools", bbox_to_anchor=(1.05, 1), loc='upper left')
# ax.set_title("Method Tracking Execution time in seconds")
# ax.set_xlabel("Dataset")
# ax.set_ylabel("Time (s)")
#
# # Add grid for better readability
# ax.grid(axis='y', linestyle='--', alpha=0.7)

cdfFigure.savefig("../cache/change-type-cdf.png", dpi=300, bbox_inches='tight')
# Show the plot
plt.tight_layout()
plt.show()
