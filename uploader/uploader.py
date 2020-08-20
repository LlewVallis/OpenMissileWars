import os
from github3 import GitHub
from github3.session import GitHubSession

print("Logging into Github")
token = os.environ["GITHUB_TOKEN"]

gh = GitHub(token=token, session=GitHubSession(default_connect_timeout=3600, default_read_timeout=3600))

print("Fetching repository")
repo = gh.repository("LlewVallis", "OpenMissileWars")

print("Fetching latest release")
latest_release = repo.latest_release().tag_name
version = str(int("".join([c for c in latest_release if str.isdigit(c)])) + 1)

print("Creating new release")
release = repo.create_release(tag_name="prebuilt-" + version, name="Prebuilt server #" + version)

print("Reading server archive")
asset = open("server.tar.gz", "rb").read()

print("Uploading server archive")
release.upload_asset(content_type="application/gzip", name="server.tar.gz", asset=asset)