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

with open("../release-suffix.txt", "r") as release_suffix_file:
    release_suffix = release_suffix_file.read().strip()

tag_name=f"prebuilt-{version}"
name=f"Packaged server #{version} {release_suffix}"

print(f'Creating release {tag_name} called "{name}"')
release = repo.create_release(tag_name=tag_name, name=name)

print("Reading launcher JAR")
launcher_asset = open("launcher.jar", "rb").read()

print("Uploading launcher")
release.upload_asset(content_type="application/jar-archive", name="launcher.jar", asset=launcher_asset)

print("Reading server archive")
server_asset = open("server.tar.gz", "rb").read()

print("Uploading server archive")
release.upload_asset(content_type="application/gzip", name="server.tar.gz", asset=server_asset)
