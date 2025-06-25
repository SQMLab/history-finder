import collections
import json
import os
from matplotlib.ticker import MaxNLocator
import matplotlib.pyplot as plt
import numpy as np
import subprocess
from util import toUpperFirst
import javalang
import jpype
import jpype.imports
from jpype.types import *
from matplotlib.ticker import FuncFormatter


ORACLE_KEYS = ['codeShovel', 'historyFinder']
REPOSITORY_PROPERTY_KEYS = ['file', 'commit', 'method', 'line']

oracleDirectoryMap = {
    'codeShovel': "../oracle/codeshovel-oracle-updated",
    'historyFinder': "../oracle/historyfinder-oracle"
}
# REPOSITORY_ROOT_DIRECTORY = "../../../repository"
# repositoryNameMap = {oracleKey : set() for oracleKey in oracleDirectoryMap.keys()}
# for oracleKey, directory in oracleDirectoryMap.items():
#     files = os.listdir(directory)
#     for f in files:
#         if f.endswith('.json'):
#             jsonFile = json.load(open(os.path.join(oracleDirectoryMap[oracleKey], f), 'r'))
#             repositoryNameMap[oracleKey].add(jsonFile['repositoryName'])
#
# repositoryPropertyMap = {oracleKey : {propertyKey: [] for propertyKey in REPOSITORY_PROPERTY_KEYS} for oracleKey in repositoryNameMap.keys()}
#
# def count_java_files(repo_path):
#     java_file_count = 0
#     for root, _, files in os.walk(repo_path):
#         for file in files:
#             if file.endswith(".java"):
#                 java_file_count += 1
#     return java_file_count
#
# def count_commits(repo_path):
#     result = subprocess.run(
#         ["git", "rev-list", "--count", "HEAD"],
#         cwd=repo_path,
#         stdout=subprocess.PIPE,
#         stderr=subprocess.PIPE,
#         text=True
#     )
#     if result.returncode != 0:
#         raise RuntimeError(f"Git error: {result.stderr.strip()}")
#     return int(result.stdout.strip())
#
# def count_lines_in_java_files(repo_path):
#     total_lines = 0
#     for root, _, files in os.walk(repo_path):
#         for file in files:
#             if file.endswith(".java"):
#                 file_path = os.path.join(root, file)
#                 try:
#                     with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
#                         total_lines += sum(1 for _ in f)
#                 except Exception as e:
#                     print(f"Skipping {file_path}: {e}")
#     return total_lines
#
#
#
# def start_jvm(javaparser_jar_path):
#     if not jpype.isJVMStarted():
#         jpype.startJVM(jpype.getDefaultJVMPath(), classpath=[javaparser_jar_path], )
#
# def count_methods_with_javaparser(repo_path, javaparser_jar_path):
#
#     start_jvm(javaparser_jar_path)
#     from java.io import File
#     from com.github.javaparser import StaticJavaParser
#     from com.github.javaparser import ParserConfiguration
#     from com.github.javaparser.ParserConfiguration import LanguageLevel
#
#     # Set parser config to support Java 21
#     config = ParserConfiguration()
#     config.setLanguageLevel(LanguageLevel.JAVA_21)  # or JAVA_17, JAVA_14 depending on your code
#     StaticJavaParser.setConfiguration(config)
#     total_methods = 0
#     totalJavaFiles = 0
#     totalParseFailedJavaFiles = 0
#     for root, _, files in os.walk(repo_path):
#         for file in files:
#             if file.endswith(".java"):
#                 totalJavaFiles += 1
#                 file_path = os.path.abspath(os.path.join(root, file))
#                 try:
#                     cu = StaticJavaParser.parse(File(file_path))
#                     class_list = cu.getTypes()
#
#                     method_count = 0
#                     for class_or_interface in class_list:
#                         method_count += class_or_interface.getMethods().size()
#
#                     total_methods += method_count
#                     #print(f"{file_path}: {method_count} method(s)")
#
#                 except Exception as e:
#                     totalParseFailedJavaFiles +=1
#                     #print(f"Failed to parse {file_path}: {e}")
#
#     print(f"Failed to parse file {os.path.basename(os.path.normpath(repo_path))}: {totalParseFailedJavaFiles}/{totalJavaFiles}")
#     return total_methods
# for oracleKey, repositorySet in repositoryNameMap.items():
#     for repositoryName in repositorySet:
#         repositoryDirectory = os.path.join(REPOSITORY_ROOT_DIRECTORY, repositoryName)
#         repositoryPropertyMap[oracleKey]['file'].append(count_java_files(repositoryDirectory))
#         repositoryPropertyMap[oracleKey]['commit'].append(count_commits(repositoryDirectory))
#         repositoryPropertyMap[oracleKey]['method'].append(count_methods_with_javaparser(repositoryDirectory, '../lib/javaparser-core-3.26.3.jar'))
#         repositoryPropertyMap[oracleKey]['line'].append(count_lines_in_java_files(repositoryDirectory))


# print(repositoryPropertyMap)
repositoryPropertyMap = {'codeShovel': {'file': [2925, 774, 14163, 1454, 7486, 500, 5583, 4907, 12112, 978, 48, 84948, 5310, 0, 4973, 515, 1912, 3619, 14915, 471], 'commit': [28377, 1117, 35756, 8395, 50436, 8061, 29778, 14027, 27497, 6126, 5691, 453507, 15484, 34921, 30008, 5127, 9484, 1028, 19261, 2513], 'method': [12158, 6410, 89826, 7816, 34423, 7963, 40867, 20416, 108564, 5019, 128, 355530, 24707, 0, 34898, 5025, 19577, 26463, 90403, 2419], 'line': [223261, 174240, 2479985, 176015, 757660, 182277, 1026186, 541523, 2888540, 97259, 3859, 6102599, 547745, 0, 1001860, 104392, 336363, 563779, 1795155, 45350]}, 'historyFinder': {'file': [615, 1436, 1581, 1526, 7765, 1973, 724, 2997, 4153, 223, 5119, 4632, 499, 415, 1884, 2705, 4926, 317, 3713, 1779], 'commit': [2052, 3830, 7429, 17908, 42464, 11589, 2869, 3864, 12641, 2065, 5789, 12558, 3568, 2081, 6017, 26153, 29651, 1703, 7201, 34752], 'method': [3785, 5649, 13617, 11740, 24356, 17141, 6527, 10833, 32067, 2059, 63775, 42923, 2513, 2547, 17282, 28886, 43650, 1871, 21954, 12048], 'line': [106049, 161599, 244820, 249453, 531601, 361235, 115315, 243398, 836264, 42872, 1860203, 1029641, 67815, 78866, 473130, 641116, 1068230, 56471, 422528, 311151]}}
print(repositoryPropertyMap)
cdfPlotColors = ['brown', 'green', 'purple', 'dimgray', 'peru']
HATCHES = ['xx', '//', '.', 'O.', '*']
MARKERS = ["^", "d", "o", "v", "p", "s", "<", ">"]
LINE_STYLES = ["-", "--", "-.", ":", "--", "--", "-.", ":"]
def thousands_formatter(x, pos):
    return f"{int(x/1000)}k" if x >= 1000 else str(int(x))
def ecdf(a):
    x, counts = np.unique(a, return_counts=True)
    cusum = np.cumsum(counts)
    return x, cusum / cusum[-1]

cdfFigure, cdfAxes = plt.subplots(2, 2, figsize=(10, 10), sharey=False)
cdfPlotRuntimeLimitAndStepSize = [[60, 5], [60, 5], [60, 5]]

for propertyKeyIndex, propertyKey in enumerate(REPOSITORY_PROPERTY_KEYS):
    cdfPlot = cdfAxes[propertyKeyIndex//2][propertyKeyIndex%2]
    for oracleIndex, oracleKey in enumerate(ORACLE_KEYS):
        label = toUpperFirst(oracleKey) if propertyKeyIndex == 0 else ''

        # x = repositoryPropertyMap[oracleKey][propertyKey]
        # x = np.sort(x)
        # y = np.arange(1, len(x) + 1) / len(x)
        x, y = ecdf(repositoryPropertyMap[oracleKey][propertyKey])
        cdfPlot.plot(x, y, label=label, color=cdfPlotColors[oracleIndex],
                     linewidth=4,
                     linestyle=LINE_STYLES[oracleIndex],
                     #
                     # markersize=12,
                     # markeredgewidth=2,
                     # marker=MARKERS[oracleIndex], markevery=0.1
                     )
        cdfPlot.xaxis.set_major_formatter(FuncFormatter(thousands_formatter))
    cdfPlot.tick_params(axis='both', labelsize=18)
    # cdfPlot.set_xlim(0, cdfPlotRuntimeLimitAndStepSize[propertyKeyIndex][0])
    # cdfPlot.set_title(datasetLabels[propertyKeyIndex], fontsize=20)
    #cdfPlot.set_yticks(np.arange(0, 1.1, 0.1))
    # cdfPlot.set_xticks(np.arange(0, cdfPlotRuntimeLimitAndStepSize[propertyKeyIndex][0] + 1, cdfPlotRuntimeLimitAndStepSize[propertyKeyIndex][1]))
    cdfPlot.grid(axis='both', linestyle='--', alpha=0.5)
    if propertyKeyIndex == 0:
        cdfPlot.set_ylabel("CDF", fontsize=20)
        cdfPlot.legend(title="Oracle", loc='lower right', fontsize=18, title_fontsize=20)
    cdfPlot.set_xlabel("#" + propertyKey.capitalize(), fontsize=24)
# cdfFigure.supxlabel('Execution Time (seconds)', fontsize=20)
plt.tight_layout()
#cdfFigure.savefig("../cache/oracle-property-cdf.png", dpi=300, bbox_inches='tight')
plt.show()
