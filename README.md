# HaVaCentralTowny

## Documentation

The documentation for commands is located in [docs/wiki.md](docs/wiki.md). This file doubles as the
main page of the GitHub Wiki. It outlines each command provided by this plugin.

### Updating the GitHub Wiki

The GitHub Actions workflow at `.github/workflows/wiki-sync.yml` automatically copies
`docs/wiki.md` to the repository's wiki as `Home.md` whenever changes to that file are pushed to
the `main` branch. You can also run the sync manually from the **Actions** tab by selecting the
"Sync wiki" workflow and clicking **Run workflow** (available because the workflow exposes a
`workflow_dispatch` trigger).
