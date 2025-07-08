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
import subprocess
from pathlib import Path


ORACLE_KEYS = ['codeShovel', 'historyFinder']

oracleDirectoryMap = {
    'codeShovel': "../oracle/codeshovel-oracle-updated",
    'historyFinder': "../oracle/historyfinder-oracle"
}
REPOSITORY_ROOT_DIRECTORY = "../../../repository"

OUTPUT_DIRECTORY = "../cache/output"
for toolName in ['codeShovel', 'historyFinder', 'codeTracker']:
    for oracleKey, directory in oracleDirectoryMap.items():
        files = os.listdir(directory)
        files.sort()
        for f in files:
            if f.endswith('.json'):
                jsonFile = json.load(open(os.path.join(oracleDirectoryMap[oracleKey], f), 'r'))
                repositoryPath = os.path.join(REPOSITORY_ROOT_DIRECTORY, jsonFile['repositoryName'])
                startCommit = jsonFile['startCommitHash']
                file = jsonFile['file']
                methodName = jsonFile['element']
                startLine = jsonFile['startLine']
                outputFile = os.path.join(OUTPUT_DIRECTORY, toolName, f)
                repositoryUrl = jsonFile['repositoryUrl']

                if toolName == 'codeShovel':
                    cmd = [
                        "java", "-jar", '../history-aggregator/lib/codeshovel-0.3.1-SNAPSHOT.jar',
                        "-repopath", repositoryPath,
                        "-startcommit", startCommit,
                        "-filepath", file,
                        "-methodname", methodName,
                        "-startline", str(startLine),
                        "-outfile", outputFile
                    ]
                if toolName == 'historyFinder':
                    cmd = [
                        "java", "-jar", '../history-aggregator/lib/history-miner-1.0.3-SNAPSHOT.jar',
                        "-clone-directory", REPOSITORY_ROOT_DIRECTORY,
                        "-repository-url", repositoryUrl,
                        "-start-commit", startCommit,
                        "-file", file,
                        "-method-name", methodName,
                        "-start-line", str(startLine),
                        "-output-file", outputFile
                    ]
                if toolName == 'codeTracker':
                    cmd = [
                        "java", "-jar", '../history-aggregator/lib/code-tracker-2.4.jar',
                        "-clone-directory", REPOSITORY_ROOT_DIRECTORY,
                        "-repository-url", repositoryUrl,
                        "-start-commit", startCommit,
                        "-file", file,
                        "-method-name", methodName,
                        "-start-line", str(startLine),
                        "-output-file", outputFile
                    ]

                if not os.path.exists(outputFile):
                    print(f"Executing .. {f}")
                    try:
                        subprocess.run(cmd, check=True, timeout=1000)
                    except subprocess.CalledProcessError as e:
                        print(f"Execution failed: {toolName} {f} {e} ")
