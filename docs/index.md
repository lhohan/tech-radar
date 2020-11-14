# Tech Radar generation

Generate a static Tech Radar from the command line using a CSV.  

Tested and reported to be working on:

- MacOS
- Linux

Requires a JDK installation, tested with 1.8 so should work for any JDK with higher version number.

## Motivation

The Tech Radar is a tool popularized by Thoughtworks's Technology radar. This implementation is based on the Zalando Technology radar.

Thoughtworks provides an online tool to Building Your Own Tech Radar from a CSV file.

You can also run this tool locally using a Docker container. This requires the container to be running to which you need to submit a CSV from an HTML end-point.

All these can be combined into script to generate the Radar locally, but I was looking for something more light-weight.

Main features:

1. Run locally from the command line 
1. Run from a text file that can be put under source control
1. Generate static HTML.

## Usage

Download the latest release from [releases](https://github.com/lhohan/tech-radar/releases). 

### Via curl:

Note: for the latest version you may want to check the releases link above.

```bash
curl -L https://github.com/lhohan/tech-radar/releases/download/v0.2.0/techradar > techradar && chmod +x techradar  && ./techradar
```

You should see output similar to:

```
Error: Missing option --source
CSV to Radar 0.2.0
Usage: CSV to Radar [options]

```

### In short

```bash
techradar -s example.csv -t out/ -p index_template.html
```

### Step-by-step

1. Copy following text to `example.csv`

```
name,ring,quadrant,moved,description
Decision records,adopt,techniques,none,"..."
Domain Driven Design,trial,techniques,up,"..."
Property-based testing,trial,techniques,down,"..."
Scala,adopt,languages and frameworks,none,"..."
TypeScript,adopt,languages and frameworks,none,"..."
```

2. Run without template specified:

```bash
techradar -s example.csv -t out/
```
or download the example template, which you probably will want to do anyway, so you can customize:

https://github.com/lhohan/tech-radar/blob/main/src/main/resources/index_template.html

In this case run:

```bash
techradar -s example.csv -t out/ -p index_template.html
```

modifying paths on where you saved the example files and where you want to write the output to.

3. Open the generated `index.html` file

### Commandline  arguments

- `-s`, `--source` : mandatory,path to local CSV file
- `-t`, `--target` : optional, path to target directory where output will be generated. Default: run directory.
- `-p`, `--template` : optional, path to customized HTML template file. If not a default template file will be generated. 


### CSV format

```
name,ring,quadrant,moved,description
Decision records,adopt,techniques,none,"..."
Domain Driven Design,trial,techniques,up,"..."
Property-based testing,trial,techniques,down,"..."
Scala,adopt,languages and frameworks,none,"..."
TypeScript,adopt,languages and frameworks,none,"..."
```

Important:

- `ring`: must correspond with the lowercase name in the HTML template
- `quadrant`: must correspond with the lowercase name in the HTML template
- `moved`: valid values: `up`, `down`, `none`

## Developent

Code for the Tech Radar and these docs are hosted on [github](https://github.com/lhohan/tech-radar).
