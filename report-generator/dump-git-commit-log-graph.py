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



def save_git_log_graph(repo_dir, file_path, output_filename, commit_hash=None):
    """
    Runs 'git log --graph <commit_hash> -- <file_path>' and saves the output to output_filename.

    :param repo_dir: Path to the root of the git repository.
    :param file_path: Path to the file (relative to repo_dir) to analyze.
    :param output_filename: Path to the output file to save the graph.
    :param commit_hash: Optional commit hash to start the log from.
    """
    output_path = Path(output_filename).expanduser()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    cmd = ["git", "log", "--graph"]
    if commit_hash:
        cmd.append(commit_hash)
    cmd += ["--", file_path]

    try:
        with open(output_path, 'w') as outfile:
            subprocess.run(
                cmd,
                cwd=repo_dir,
                stdout=outfile,
                stderr=subprocess.PIPE,
                check=True
            )
        print(f"Git graph saved to: {output_path}")
    except subprocess.CalledProcessError as e:
        print(f"Error running git log: {e.stderr.decode()}")

projects = []

for oracleKey, directory in oracleDirectoryMap.items():
    files = os.listdir(directory)
    files.sort()
    for f in files:
        if f.endswith('.json'):
            jsonFile = json.load(open(os.path.join(oracleDirectoryMap[oracleKey], f), 'r'))
            # save_git_log_graph(
            #     repo_dir=os.path.join(REPOSITORY_ROOT_DIRECTORY, jsonFile['repositoryName']),
            #     file_path=jsonFile['file'],
            #     output_filename=f"../cache/graph/{f.replace('.json', '.graph')}",
            #     commit_hash=jsonFile['startCommitHash']
            # )
            if jsonFile['repositoryName'] not in projects:
                projects.append(jsonFile['repositoryName'])
print(projects)
