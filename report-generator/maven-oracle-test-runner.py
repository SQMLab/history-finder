import subprocess

# Tracer names to test
tracer_names = ['historyFinder']
# tracer_names = ['codeShovel', 'codeTracker', 'historyFinder']

# Oracle file IDs to test
oracle_ids = range(201, 401)

for tracer_name in tracer_names:
    print(f"\n[INFO] Running tests for Tracer: {tracer_name}")

    for oracle_id in oracle_ids:
        print(f"[INFO]   Oracle ID: {oracle_id}")

        cmd = [
            "mvn", "test",
            "-Dtest=OracleTest#executeTest",
            f"-Drun-config.oracle-file-ids={oracle_id}",
            f"-Drun-config.tracer-name={tracer_name}",
            "-Drun-config.force-compute=True"
        ]

        result = subprocess.run(cmd, capture_output=True, text=True)

        print("[STDOUT]:")
        print(result.stdout)
        print("[STDERR]:")
        print(result.stderr)

        if result.returncode != 0:
            print(f"[ERROR] Test failed for tracer={tracer_name}, oracle-id={oracle_id}")
