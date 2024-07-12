# MonkeyCI Infra Plugin

This is a [MonkeyCI](https://www.monkeyci.com) plugin that is specifically
used by the MonkeyCI build itself in order to update the infra project to
do automated deployments.

For **production**, it updates Kustomization files in the infra repo to
update image versions using the GitHub API.

For **staging**, it updates the `versions.edn` file in the `clj/resources`
directory.

The change is then pushed to the infra repo, where depending on the environment,
either ArgoCD performs a sync, or [MonkeyCI](https://monkeyci.com) runs a
build.

This plugin should be included in build scripts.

## License

Copyright (c) 2024 by [Monkey Projects BV](https://www.monkey-projects.be).