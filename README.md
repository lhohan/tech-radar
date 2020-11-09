![Scala CI](https://github.com/lhohan/time-tracking/workflows/Scala%20CI/badge.svg)
[![CodeFactor](https://www.codefactor.io/repository/github/lhohan/tech-radar/badge/main)](https://www.codefactor.io/repository/github/lhohan/tech-radar/overview/main)
[![codecov](https://codecov.io/gh/lhohan/tech-radar/branch/main/graph/badge.svg?token=54SEP6EL0D)](https://codecov.io/gh/lhohan/tech-radar)

# Tech Radar generation

Generate a static Tech Radar from the command line using a CSV.  

For usage see the [documentation page](https://gh-pages.d29iz8jq65dy3y.amplifyapp.com/).

## Dev guide

### Local dev

Run the radar from source:

```
sbt "run -s src/test/resources/example.csv -t out/ -p src/main/resources/index_template.html"
```

### Releasing

Until automated the manual steps how to release (mostly for my own reference):

- Update the version where needed
- Commit an push
- Publish locally
- Use Coursier to build the executable, e.g. `cs bootstrap io.github.lhohan::tech-radar-from-csv:0.1.1 --standalone -o techradar -f`
- Upload the release for the version

### Documentation

- mkdocs + gh-pages (branch)
  - Deploy: `mkdocs gh-deploy`
- Hosted on AWS using [Amplify](https://us-east-2.console.aws.amazon.com/amplify/)

### Some technical notes:

- This project is developed using `Scala` and `sbt`. It is not aimed to be the cleanest project and is used to 

1. serve its purpose: generate a radar from the commandline relatively easy
2. experiment with build tooling and implementation
   1. research: sbt configuration completely managed by a local sbt plugin
   
 
