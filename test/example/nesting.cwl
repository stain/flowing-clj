#!/usr/bin/env cwl-runner
#
# This is a workflow that nests the workflow revsort.cwl
#
class: Workflow
description: "Nest revsort.cwl"

# Input and output ports are deliberately
# named the same as in revsort.cwl

inputs:
  - id: "#input"
    type: File
  - id: "#reverse_sort"
    type: boolean
    default: true

outputs:
  - id: "#output"
    type: File
    source: "#nested.output"

steps:
  - inputs:
      - { id: "#nested.input", source: "#input" }
      - { id: "#nested.reverse", source: "#reverse_sort" }
    outputs:
      - { id: "#nested.output" }
    run: { import: revsort.cwl }
