# HaVaCentralTowny

## Documentation

The documentation for commands is located in [docs/wiki.md](docs/wiki.md). This file doubles as the
main page of the GitHub Wiki. It outlines each command provided by this plugin.

### Matching Spigot Towny wording

To mirror Spigot Towny’s chat text verbatim, copy the upstream plugin’s `lang/en_US.yml` into
`config/towny/spigot-lang/en_US.yml` before starting the Sponge remake. The new language importer will
automatically pull every string from that file and map it onto the Sponge command output, ensuring
that help text, error messages, and toggle prompts read exactly like the Spigot experience.

### Updating the GitHub Wiki

The GitHub Actions workflow at `.github/workflows/wiki-sync.yml` automatically copies
`docs/wiki.md` to the repository's wiki as `Home.md` whenever changes to that file are pushed to
the `main` branch. You can also run the sync manually from the **Actions** tab by selecting the
"Sync wiki" workflow and clicking **Run workflow** (available because the workflow exposes a
`workflow_dispatch` trigger).
