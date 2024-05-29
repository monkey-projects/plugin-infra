# MonkeyCI Infra Plugin

This is a [MonkeyCI](https://www.monkeyci.com) plugin that is specifically
used by the MonkeyCI build itself in order to update the infra project to
do automated deployments.

It updates Kustomization files in the infra repo to update image versions
using the GitHub API.  This plugin should be included in build scripts.

## License

Copyright (c) 2024 by [Monkey Projects BV](https://www.monkey-projects.be).