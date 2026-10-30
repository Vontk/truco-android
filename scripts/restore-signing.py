"""Restore the encrypted GitHub Actions secret on an ephemeral runner; never print key data."""
import base64
import json
import os
from pathlib import Path
bundle = json.loads(os.environ["TRUCO_SIGNING_BUNDLE"])
path = Path(os.environ["RUNNER_TEMP"]) / "truco-release.jks"
path.write_bytes(base64.b64decode(bundle["keystore"], validate=True))
path.chmod(0o600)
password = bundle["password"]
print(f"::add-mask::{password}")
with open(os.environ["GITHUB_ENV"], "a") as env:
    env.write(f"TRUCO_KEYSTORE={path}\nTRUCO_STORE_PASSWORD={password}\nTRUCO_KEY_PASSWORD={password}\n")
