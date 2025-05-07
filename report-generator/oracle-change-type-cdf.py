import collections
import json
import os
from matplotlib.ticker import MaxNLocator
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
TAGS = ['MOVE', 'RENAME', 'BODY', 'MULTIPLE']
CODE_SHOVEL_TYPE_MAPPING = {'MOVE': 'Ymovefromfile', 'RENAME': 'Yrename', 'BODY': 'Ybodychange',
                            'MULTIPLE': 'Ymultichange'}
CODE_TRACKER_TYPE_MAPPING = {'MOVE': 'moved', 'RENAME': 'rename', 'BODY': 'body change'}
commitCountMap = {
    key: {tag: [] for tag in TAGS} for key in fileMap.keys()
}
for oracleKey in fileMap:
    for file in fileMap[oracleKey]:
        commitSet = set()
        jsonFile = json.load(open(file, 'r'))
        tagCountMap = collections.Counter(TAGS)
        match oracleKey:
            case 'codeShovel':
                for _, changeText in jsonFile['expectedResult'].items():
                    for tag, codeShoveTag in CODE_SHOVEL_TYPE_MAPPING.items():
                        if codeShoveTag in changeText:
                            tagCountMap[tag] += 1
            case 'codeTracker':
                changeCount = collections.Counter()
                for changeItem in jsonFile['expectedChanges']:
                    for tag, codeTrackerTag in CODE_TRACKER_TYPE_MAPPING.items():
                        if codeTrackerTag == changeItem["changeType"]:
                            tagCountMap[tag] += 1
                    changeCount[changeItem['commitId']] += 1
                for _, count in changeCount.items():
                    if count > 1:
                        tagCountMap['MULTIPLE'] += 1
            case 'codeShovelNew' | 'historyFinder':
                for changeItem in jsonFile['commits']:
                    changeTag = changeItem['changeTags']
                    for tag in TAGS:
                        tagCountMap[tag] += 1 if tag in changeTag else 0
                    tagCountMap['MULTIPLE'] += 1 if len(changeTag) > 1 in changeTag else 0
        for tag in TAGS:
            commitCountMap[oracleKey][tag].append(tagCountMap[tag])

boxplotColors = ['#d9a999', '#80c080', '#c080c0', '#a8a8a8', '#e0b88f']
cdfPlotColors = ['brown', 'green', 'purple', 'dimgray', 'peru']

HATCHES = ['xx', '//', '.', 'O.', '*']
MARKERS = ['p', 'd', 's', '>', '*']
LINE_STYLES = [':', '--', '-.', '-', (0, (4, 2, 1, 2))]
cdfFigure, cdfAxes = plt.subplots(2, 2, figsize=(10, 10), sharey=False)
subplotIndex = 0
for oracleKey, changeMap in commitCountMap.items():
    cdfPlot = cdfAxes[subplotIndex // 2][subplotIndex % 2]

    cdfIndex = 0
    maxX = 0
    for changeTag, countSeq in changeMap.items():
        changeCountSeq = list(countSeq)
        changeCountSeq = np.sort(changeCountSeq)
        maxX = max(maxX, changeCountSeq[-1])
        label = toUpperFirst(changeTag)

        # step += tracerIndex + 1
        print(changeCountSeq, end=',')
        # Set x-axis labels and title
        cdf = np.arange(1, len(changeCountSeq) + 1) / len(changeCountSeq)


        cdfPlot.plot(changeCountSeq, cdf, label=label, color=cdfPlotColors[cdfIndex], linewidth=3,
                     markersize=10,
                     markeredgewidth=2,
                     linestyle=LINE_STYLES[cdfIndex], marker=MARKERS[cdfIndex], markevery=0.1)
        cdfIndex += 1

    # cdfPlot.set_xlim(0, cdfPlotRuntimeLimitAndStepSize[datasetIndex][0])
    cdfPlot.tick_params(axis='both', labelsize=18)
    cdfPlot.set_title(toUpperFirst(oracleKey), fontsize=20)
    cdfPlot.set_yticks(np.arange(0, 1.1, 0.1))
    xticks = np.arange(0, maxX, 10)
    cdfPlot.set_xticks(xticks)
    cdfPlot.set_xticklabels([str(t) if i % 2 == 0 or maxX <= 100 else '' for i, t in enumerate(xticks)])
    # cdfPlot.xaxis.set_major_locator(MaxNLocator(nbins=10))
    cdfPlot.grid(axis='both', linestyle='--', alpha=0.5)
    # boxPlot.legend()
    cdfPlot.set_ylabel("CDF", fontsize=20)
    cdfPlot.set_xlabel("Number of revisions", fontsize=20)
    cdfPlot.legend(title="Change Types", loc='lower right', fontsize=14, title_fontsize=16)
    subplotIndex += 1
# cdfFigure.supxlabel('Number of revisions')
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

plt.tight_layout()
cdfFigure.savefig("../cache/change-type-cdf.png", dpi=300, bbox_inches='tight')
# Show the plot
plt.show()
