import os
import json
import matplotlib.pyplot as plt
import numpy as np

from util import isWeakCommit, toUpperFirst

baseDirectorMap = {
    'codeShovel': "../src/main/resources/stubs/input/",
    'codeTracker': '/home/cs/grad/islams32/dev/rnd/history finder/oracle/codetracker/',
    'codeShovelNew': "../src/main/resources/oracle/",
    'historyFinder': "../src/main/resources/oracle/"
}


fileMap = {oracleKey : [os.path.join(baseDirectory, f) for f in os.listdir(baseDirectory) if
                      os.path.isfile(os.path.join(baseDirectory, f))] for oracleKey, baseDirectory in baseDirectorMap.items()}
fileMap['codeShovelNew'] = list(sorted(filter(lambda f: int(f.split('/')[-1].split('-')[0]) <= 200, fileMap['codeShovelNew'])))
fileMap['historyFinder'] = list(sorted(filter(lambda f: 201 <= int(f.split('/')[-1].split('-')[0]) <= 400, fileMap['historyFinder'])))

commitCountMap = {
    key : [] for key in fileMap.keys()
}
for oracleKey in fileMap:
    for file in fileMap[oracleKey]:
        commitSet = set()
        jsonFile = json.load(open(file, 'r'))
        match oracleKey:
            case 'codeShovel':
                commitSet |= jsonFile['expectedResult'].keys()
            case 'codeTracker':
                for changeItem in jsonFile['expectedChanges']:
                    commitSet.add(changeItem['commitId'])
            case 'codeShovelNew' | 'historyFinder':
                for changeItem in jsonFile['commits']:
                    commitSet.add(changeItem['commitHash'])
        commitCountMap[oracleKey].append(max(len(commitSet) - 1, 0))


boxplotColors = ['#d9a999', '#80c080', '#c080c0', '#a8a8a8', '#e0b88f']
cdfPlotColors = ['brown', 'green', 'purple', 'dimgray', 'peru']

HATCHES = ['xx', '//', '.', 'O.', '*']
MARKERS = ['p', 'd', 's', '>', '*']
LINE_STYLES = [':', '--', '-.', '-', (0, (4, 2, 1, 2))]
cdfFigure, cdfAxes = plt.subplots(1, 1, figsize=(6, 6), sharey=False)
for subplotIndex in range(1):
    cdfPlot = cdfAxes[subplotIndex] if isinstance(cdfAxes,list) else cdfAxes

    cdfIndex = 0
    for oracleKey, commitCountSeq in commitCountMap.items():
        commitCounts = list(commitCountSeq)
        commitCounts = np.sort(commitCounts)
        label = toUpperFirst(oracleKey) if subplotIndex == 0 else ''

        # step += tracerIndex + 1
        print(commitCounts, end=',')
        # Set x-axis labels and title
        cdf = np.arange(1, len(commitCounts) + 1) / len(commitCounts)

        cdfPlot.plot(commitCounts, cdf, label=label, color=cdfPlotColors[cdfIndex], linewidth=3,
                     markersize=10,
                     markeredgewidth=2,
                     linestyle=LINE_STYLES[cdfIndex], marker=MARKERS[cdfIndex], markevery=0.1)
        cdfIndex += 1
    cdfPlot.tick_params(axis='both', labelsize=18)
    # cdfPlot.set_xlim(0, cdfPlotRuntimeLimitAndStepSize[datasetIndex][0])
    # cdfPlot.set_title(datasetLabels[datasetIndex])
    cdfPlot.set_yticks(np.arange(0, 1.1, 0.1))
    xticks = np.arange(0, 160,10)
    cdfPlot.set_xticks(xticks)
    cdfPlot.set_xticklabels([str(t) if i % 2 == 0 else '' for i, t in enumerate(xticks)])
    cdfPlot.grid(axis='both', linestyle='--', alpha=0.5)
    # boxPlot.legend()
    if subplotIndex == 0:
        cdfPlot.set_ylabel("CDF", fontsize=20)
        cdfPlot.legend(title="Oracles", loc='lower right', fontsize=14, title_fontsize=16)


cdfFigure.supxlabel('Number of revisions of method', fontsize=20)
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
cdfFigure.savefig("../cache/method-revisions-cdf.png", dpi=300, bbox_inches='tight')
# Show the plot
plt.show()
