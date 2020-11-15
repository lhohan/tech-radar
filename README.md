![Scala CI](https://github.com/lhohan/time-tracking/workflows/Scala%20CI/badge.svg)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bf5691a8653f421cb38997c2aedd7e60)](https://app.codacy.com/gh/lhohan/tech-radar?utm_source=github.com&utm_medium=referral&utm_content=lhohan/tech-radar&utm_campaign=Badge_Grade_Settings)
[![CodeFactor](https://www.codefactor.io/repository/github/lhohan/tech-radar/badge/main)](https://www.codefactor.io/repository/github/lhohan/tech-radar/overview/main)
[![codecov](https://codecov.io/gh/lhohan/tech-radar/branch/main/graph/badge.svg?token=54SEP6EL0D)](https://codecov.io/gh/lhohan/tech-radar)
[![Maintainability](https://api.codeclimate.com/v1/badges/e0f110457584346971ff/maintainability)](https://codeclimate.com/github/lhohan/tech-radar/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/e0f110457584346971ff/test_coverage)](https://codeclimate.com/github/lhohan/tech-radar/test_coverage)

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

The executable is build using Coursier. See the build script for details.

To release push a new tag starting with `v`, the build pipeline, if successful, 
will then publish a new release based on this tag.

### Documentation

- mkdocs + gh-pages (branch)
  - Deploy: `mkdocs gh-deploy`
- Hosted on AWS using [Amplify](https://us-east-2.console.aws.amazon.com/amplify/)

### Some technical notes:

- This project is developed using `Scala` and `sbt`. It is not aimed to be the cleanest project and is used to 

1. serve its purpose: generate a radar from the commandline relatively easy
2. experiment with build tooling and implementation
   1. research: sbt configuration completely managed by a local sbt plugin
   
 
