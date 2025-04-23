import subprocess
import requests
import os
import re
from util import toUpperFirst
import config

# Function to count Java files
def countJavaFile(repo_path):
    java_file_count = 0
    for root, _, files in os.walk(repo_path):
        for file in files:
            if file.endswith(".java"):
                java_file_count += 1
    return java_file_count

# Function to count Java methods using grep
def countMethod(repo_path):
    method_count = 0
    for root, _, files in os.walk(repo_path):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    # Regex for Java method declarations
                    method_count += len(re.findall(
                        r'\b(public|protected|private|static|\s)*\s[\w<>\[\]]+\s+[\w]+ *\([^\)]*\) *(\{?|[^;])',
                        content))
    return method_count


# Function to count Git commits
def countCommit(repo_path):
    result = subprocess.run(
        ["git", "rev-list", "--all", "--count"],
        cwd=repo_path,
        capture_output=True,
        text=True
    )
    return int(result.stdout.strip()) if result.returncode == 0 else 0


# Function to fetch stars and forks from GitHub API
def fetchStarAndForkCount(owner, repository, github_token=None):
    url = f"https://api.github.com/repos/{owner}/{repository}"
    headers = {"Accept": "application/vnd.github.v3+json"}
    if github_token:
        headers["Authorization"] = f"token {github_token}"

    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        data = response.json()
        stars = data.get("stargazers_count", 0)
        forks = data.get("forks_count", 0)
        return stars, forks
    else:
        raise Exception('Failed to fetch stars and forks')


# Main function
if __name__ == "__main__":
    trace = config.db["trace"]
    dataset = config.datasetList[-1]
    oracleName = dataset['oracleName']
    print(f'{dataset["oracleName"]}')
    repositoryUrls = []
    for r in trace.find({'oracleFileId': {'$gte': dataset['range'][0], '$lte': dataset['range'][-1]}},
                        sort=[("oracleFileId", 1)], allow_disk_use=True).limit(1000):
        repositoryUrls.append(r.get('repositoryUrl'))
    repositoryUrls = list(set(repositoryUrls))
    repositoryUrls.sort(key = lambda url: url.split('/')[-1].lower())

    for repositoryUrl in repositoryUrls:
        # Path to the local Git repository
        _, repositorySpace, repositoryName = repositoryUrl[len('https://'):len(repositoryUrl) - len('.git')].split('/')
        localPath = config.LOCAL_REPOSITORY_ROOT_DIRECTORY + repositoryName


        methods = countMethod(localPath)

        commits = countCommit(localPath)

        java_files = countJavaFile(localPath)
        stars, forks = fetchStarAndForkCount(repositorySpace, repositoryName, config.GITHUB_TOKEN)
        print(f'    {repositoryName} & {commits:,} & {java_files:,} & {methods:,} & {stars:,} & {forks:,} \\\\')