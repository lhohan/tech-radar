![Scala CI](https://github.com/lhohan/time-tracking/workflows/Scala%20CI/badge.svg)


# Command line Tech Radar

Generate a static Tech Radar similar to Thoughtworks's Technology radar. This version is based on the Zalando Technology radar.

## Motivation

1. Run from a text file that can be put under source control
2. Run locally

Thoughtworks provides Building Your Own Tech Radar using a CSV but this is an online tool.
 
Thoughtworks provides this functionality against a local running container. This requires the container to be running to which you need to provide a CSV from an HTML end-point and have the docker container permanently running.

This tool generates static files that can be run stand-alone. 

## Usage

Download the latest release from [releases](https://github.com/lhohan/tech-radar/releases). 

Command arguments:

- `-s`, `--source` : mandatory,path to local CSV file
- `-t`, `--target` : optional, path to target directory where output will be generated. Default: run directory.
- `-p`, `--template` : optional, path to customized HTML template file. If not a default template file will be generated. 

Using `sbt` : 

```bash
sbt "run -s src/test/resources/example.csv -t out/ -p src/main/resources/index_template.html"
```

## CSV format

```
name,ring,quadrant,moved,description
Decision records,adopt,techniques,none,"..."
Domain Driven Design,trial,techniques,none,"..."
Property-based testing,trial,techniques,none,"..."
Scala,adopt,languages and frameworks,none,"..."
TypeScript,adopt,languages and frameworks,none,"..."
```

Important:

- `ring`: must correspond with the lowercase name in the HTML template
- `quadrant`: must correspond with the lowercase name in the HTML template
- `moved`: valid values: `up`, `down`, `none`


## A new release

Until automated the manual steps how to release (mostly for my own reference):

- Update the version where needed
- Commit an push
- Publish locally
- Use Coursier to build the executable, e.g. `cs bootstrap io.github.lhohan::tech-radar-from-csv:0.1.1 --standalone -o techradar -f`
- Upload the release for the version
