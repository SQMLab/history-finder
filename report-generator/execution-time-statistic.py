import matplotlib.pyplot as plt
import config
import numpy as np
from util import toUpperFirst

trace = config.db["trace"]
runtimeStatisticsList = []
tracerList = [t for t in config.tracerList if t != 'intelliJ']
for dataset in config.datasetList:
    oracleName = dataset['oracleName']
    runtimeStatistics = {tracerName: [] for tracerName in tracerList}

    for r in trace.find({'oracleFileId': {'$gte': dataset['range'][0], '$lte': dataset['range'][-1]}},
                        sort=[("oracleFileId", 1)], allow_disk_use=True).limit(1000):
        try:
            for tracerName in tracerList:
                if tracerName in r.get('analysis'):
                    analysis = r.get('analysis')[tracerName]
                    runtimeStatistics[tracerName].append(analysis['runtime'] / 1000)
                else:
                    # print(f'Warning: {oracleName} {dataset["range"]} {r.get("oracleFileId")} {tracerName} is not in analysis')
                    pass
        except Exception as e:
            oracleFileId = r.get('oracleFileId')
            print(f'Oracle File ID : {oracleFileId} \n{e}')
            raise e
    runtimeStatisticsList.append(runtimeStatistics)

datasetLabels = [dataset['oracleName'] for dataset in config.datasetList]

for datasetIndex, runtimeStatistics in enumerate(runtimeStatisticsList):
    print(f'{datasetLabels[datasetIndex]}')
    for tracerIndex, tracerName in enumerate(tracerList):
        runtimes = np.array(runtimeStatistics[tracerName])
        print(
            f'    & {toUpperFirst(tracerName)} & {np.mean(runtimes):.2f} & {np.median(runtimes):.2f} & {np.min(runtimes):.2f} & {np.max(runtimes):.2f} \\\\')
    print(f'\\hline')

# boxplotColors = ['pink', 'lightgreen', 'lightblue', 'silver', 'peru']
boxplotColors = ['#d9a999', '#80c080', '#c080c0', '#a8a8a8', '#e0b88f']
cdfPlotColors = ['brown', 'green', 'purple', 'dimgray', 'peru']

HATCHES = ['xx', '//', '.', 'O.', '*']
MARKERS = ['h', 'd', 'x', '>', '*']
LINE_STYLES = [':', '--', '-.', '-', (0, (4, 2, 1, 2))]
datasetLength = len(datasetLabels)
# Create a box plot
boxPlotFigure, boxPlotAxes = plt.subplots(1, datasetLength, figsize=(20, 5), constrained_layout=True, sharey=False)
cdfFigure, cdfAxes = plt.subplots(1, datasetLength, figsize=(20, 5), sharey=False)
boxPlotRuntimeLimit = [50, 50, 50]
cdfPlotRuntimeLimitAndStepSize = [[40, 5], [40, 5], [40, 5]]
for datasetIndex, runtimeStatistics in enumerate(runtimeStatisticsList):
    boxPlot = boxPlotAxes[datasetIndex]
    cdfPlot = cdfAxes[datasetIndex]
    boxPlot.set_ylim(0, boxPlotRuntimeLimit[datasetIndex])
    boxPlot.set_yticks(np.arange(0, boxPlotRuntimeLimit[datasetIndex] + 1, 5))

    step = 10
    for tracerIndex, tracerName in enumerate(tracerList):
        runtimes = runtimeStatistics[tracerName]
        runtimes = np.sort(runtimes)
        label = toUpperFirst(tracerName) if datasetIndex == 0 else ''
        boxPlot.boxplot(
            runtimes,
            label=label,
            positions=[tracerIndex + 1],
            patch_artist=True,
            widths=0.5,
            boxprops=dict(facecolor=boxplotColors[tracerIndex], hatch=HATCHES[tracerIndex]),
            medianprops=dict(color='black', linewidth=1.5)
        )
        # step += tracerIndex + 1
        print(runtimes, end=',')
        # Set x-axis labels and title
        cdf = np.arange(1, len(runtimes) + 1) / len(runtimes)

        x_sub = runtimes[tracerIndex + 10::step]
        y_sub = cdf[tracerIndex + 10::step]
        # Append last point if it's not already included
        if runtimes[-1] > x_sub[-1]:
            x_sub = np.append(x_sub, runtimes[-1])
            y_sub = np.append(y_sub, cdf[-1])
        cdfPlot.plot(x_sub, y_sub, label=label, color=cdfPlotColors[tracerIndex], linewidth=3,
                     markersize=12,
                     markeredgewidth=2,
                     linestyle=LINE_STYLES[tracerIndex], marker=MARKERS[tracerIndex])

    # boxPlot.set_xticks([tracerIndex + 1 for tracerIndex in range(len(tracerList))])
    # boxPlot.set_xticklabels([toUpperFirst(tracerName) for tracerName in tracerList], rotation=45, ha="right")
    boxPlot.set_title(datasetLabels[datasetIndex])

    cdfPlot.set_xlim(0, cdfPlotRuntimeLimitAndStepSize[datasetIndex][0])
    cdfPlot.set_title(datasetLabels[datasetIndex])
    cdfPlot.set_yticks(np.arange(0, 1.1, 0.1))
    cdfPlot.set_xticks(np.arange(0, cdfPlotRuntimeLimitAndStepSize[datasetIndex][0] + 1,
                                 cdfPlotRuntimeLimitAndStepSize[datasetIndex][1]))
    cdfPlot.grid(axis='both', linestyle='--', alpha=0.5)
    boxPlot.grid(axis='both', linestyle='--', alpha=0.5)
    # boxPlot.legend()
    boxPlot.set_xticks([])
    if datasetIndex == 0:
        boxPlot.set_ylabel("Execution Time (seconds)")
        cdfPlot.set_ylabel("CDF")

        cdfPlot.legend(title="Tools", loc='lower right', fontsize=18, title_fontsize=20)
        boxPlot.legend(title="Tools", loc='upper left', fontsize=18, title_fontsize=20)
boxPlotFigure.supxlabel('Tools')
cdfFigure.supxlabel('Execution Time (seconds)')
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

cdfFigure.savefig("../cache/execution-time-cdf.png", dpi=300, bbox_inches='tight')
boxPlotFigure.savefig("../cache/execution-time-box-plot.png", dpi=300, bbox_inches='tight')
# Show the plot
plt.tight_layout()
plt.show()
