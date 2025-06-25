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
repositoryCommitHashMap = {}
REPOSITORY_ROOT_DIRECTORY = "../../../repository"
repositoryNameMap = {oracleKey : set() for oracleKey in oracleDirectoryMap.keys()}
# for oracleKey, directory in oracleDirectoryMap.items():
#     files = os.listdir(directory)
#     for f in files:
#         if f.endswith('.json'):
#             jsonFile = json.load(open(os.path.join(oracleDirectoryMap[oracleKey], f), 'r'))
#             repositoryNameMap[oracleKey].add(jsonFile['repositoryName'])
#             repositoryCommitHashMap[jsonFile['repositoryName']] = commitHash = jsonFile['startCommitHash']
#
# repositoryPropertyMap = {oracleKey : {propertyKey: [] for propertyKey in REPOSITORY_PROPERTY_KEYS} for oracleKey in repositoryNameMap.keys()}
#
# def checkout_repo_to_commit(repo_path, commit_hash):
#     try:
#         subprocess.run(["git", "checkout", commit_hash],
#                        cwd=repo_path,
#                        check=True,
#                        stdout=subprocess.PIPE,
#                        stderr=subprocess.PIPE)
#     except subprocess.CalledProcessError as e:
#         print(f"✖ Failed to checkout: {repo_path} {e.stderr.decode().strip()}")
#         #raise e
#
# def count_java_files(repo_path):
#     java_file_count = 0
#     for root, _, files in os.walk(repo_path):
#         for file in files:
#             if file.endswith(".java"):
#                 java_file_count += 1
#     print(f'{repo_path}: {java_file_count} java files found')
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
#         checkout_repo_to_commit(repositoryDirectory, repositoryCommitHashMap[repositoryName])
#         repositoryPropertyMap[oracleKey]['file'].append(count_java_files(repositoryDirectory))
#         repositoryPropertyMap[oracleKey]['commit'].append(count_commits(repositoryDirectory))
#         repositoryPropertyMap[oracleKey]['method'].append(count_methods_with_javaparser(repositoryDirectory, '../lib/javaparser-core-3.26.3.jar'))
#         repositoryPropertyMap[oracleKey]['line'].append(count_lines_in_java_files(repositoryDirectory))
#
# for oracleKey in ORACLE_KEYS:
#     for propertyKey in REPOSITORY_PROPERTY_KEYS:
#         repositoryPropertyMap[oracleKey][propertyKey].sort()
#
# print(repositoryPropertyMap)
repositoryPropertyMap = {'codeShovel': {'file': [246, 320, 327, 465, 863, 911, 1364, 1429, 1730, 1950, 2497, 2695, 4319, 6400, 6884, 7661, 9038, 9542, 10399, 59580], 'commit': [2121, 2228, 3253, 4695, 4781, 4811, 5230, 6065, 6172, 7622, 9100, 13359, 14366, 15990, 17041, 17818, 19805, 30500, 40353, 226106], 'method': [2385, 2387, 4202, 4276, 5176, 5980, 9925, 10592, 12748, 13396, 14067, 18709, 19211, 37929, 48345, 53178, 55861, 66099, 88211, 282655], 'line': [44691, 57790, 74859, 83212, 99170, 143776, 176747, 207921, 246973, 278637, 371089, 431133, 541820, 1031118, 1094879, 1127836, 1575512, 1671057, 2402075, 4942529]}, 'historyFinder': {'file': [223, 319, 414, 486, 621, 725, 1436, 1539, 1579, 1783, 1884, 1973, 2705, 3121, 3656, 4157, 4632, 4999, 5119, 7523], 'commit': [1786, 2014, 2065, 2074, 2901, 3363, 3830, 3961, 5782, 6017, 7422, 7465, 11589, 12558, 12660, 18086, 26153, 29883, 34978, 42386], 'method': [1901, 2059, 2433, 2525, 3823, 5649, 6530, 11786, 12056, 12583, 13596, 17141, 17282, 21702, 23483, 28886, 32115, 42923, 44280, 63768], 'line': [42872, 57466, 66161, 78173, 106737, 115468, 161599, 244458, 251321, 254101, 311241, 361235, 417010, 473130, 512843, 641116, 838208, 1029641, 1083947, 1860096]}}

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
    if propertyKeyIndex %2 ==0:
        cdfPlot.set_ylabel("CDF", fontsize=20)
    if propertyKeyIndex == 0:
        cdfPlot.legend(title="Oracle", loc='lower right', fontsize=18, title_fontsize=20)
    cdfPlot.set_xlabel("#" + propertyKey.capitalize(), fontsize=24)
# cdfFigure.supxlabel('Execution Time (seconds)', fontsize=20)
plt.tight_layout()
#cdfFigure.savefig("../cache/oracle-property-cdf.png", dpi=300, bbox_inches='tight')
plt.show()
