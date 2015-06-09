# flowing-clj

A [Clojure](http://clojure.org/) library for building data-driven workflows (dataflows).

## Building

Build or test using [Leiningen](http://leiningen.org/):

```
lein test
lein install
lein repl
```

## Related projects

As flowing-clj is just a prototype, you might prefer trying:

* Prismatic's [Plumbing and Graph](https://github.com/Prismatic/plumbing#graph-the-functional-swiss-army-knife)

## Usage

```clojure
(use 'flowing.core)

(def example-wf
  (workflow
    ; Define a series of steps, which can take 0 or more
    ; arguments. They do not need to be defined in any
    ; particular order.
    ; (The below are dummy examples with Thread/sleep to pretend
    ; to be doing some processing)
    (defstep get-sequence [id]
      (println "Retrieving sequence for id" id)
      (Thread/sleep 1000)
      "GATTAGCAT")
    (defstep alignment [sequence database]
      (println "Aligning" sequence "in" database)
      (Thread/sleep 2000)
      (if (= database "cat")
        (str ">" database " " sequence)))
    (defstep pathways [fasta]
      (println "Finding pathways for" fasta)
      (Thread/sleep 1000)
      { :dog :cat
        :cat :tree
        :fireman :tree})
    (defstep similar [fasta paths]
      (println "Finding similarities in" (count paths) "paths")
      (Thread/sleep 1000)
      [ "tiger" "lion" ])

    ; Now link them together. Links be provided
    ; in any order, but you can only link to an input
    ; parameter once.
    (link "CATGENE15" (:id get-sequence)) ; constant value
    ; Each input parameter linked separately
    (link get-sequence (:sequence alignment))
    (link "cat" (:database alignment))
    ; Same output do multiple destinations
    (link alignment (:fasta pathways))
    (link alignment (:fasta similar))
    ; Steps are executed as soon as all inputs are ready
    ; and in parallell threads, but this link would
    ; cause :similar to run after :alignment
    (link pathways (:paths similar))))
```

The steps in the workflow will start executing in parallel,
as soon as all inputs are received:

```
#'user/example-wf
Retrieving sequence for id CATGENE15
user=> Aligning GATTAGCAT in cat
Finding pathways for >cat GATTAGCAT
Finding similarities in 3 paths
user=> 
```


Outputs can be retrieved from individual steps, while the workflow 
is running (and after):

```clojure
(println (wait-for-output get-sequence))
GATTAGCAT
```

`(wait-for-output)` is so called as it will block until 
the step has received all its required inputs and finished
executing.

```clojure
(println (wait-for-output similar))
["tiger" "lion"]
```

`(wait-for-workflow)` will ensure all steps are complete, and return
a map with all the results values.

```clojure
(println (wait-for-workflow example-wf))
{ :get-sequence GATTAGCAT, 
  :alignment >cat GATTAGCAT, 
  :pathways {:cat :tree, :dog :cat, :fireman :tree}, 
  :similar [tiger lion] }
```

## How does it work?

`(step)` create a 
[promise](http://clojuredocs.org/clojure.core/promise)
for each input parameter. The body of the step is executed in a 
[future](http://clojuredocs.org/clojure.core/future), where it 
call [deref](http://clojuredocs.org/clojure.core/deref) on
the inputs so that the step body use the parameters as if
passed in normally to a function.

`(defstep)` is a shortcut for 
`(def foo (step "foo" [x y] ...))`, the defined symbol means 
the step can be referred to during both definition time and
after execution.

`(link)` will [deliver](http://clojuredocs.org/clojure.core/deliver)
the source value to the input promise, looked up as `(:inputA foo-step)`. 
The source can be either one of the other steps (in which case their 
*future* object is passed as-is) or any other expression (which is 
sent through a [delay](http://clojuredocs.org/clojure.core/delay) object).

Both `(step)` and `(link)` will return a map. The keys of `(step)` will
correspond to the input parameters, but as keywords. Additional special keys 
include `::name`, `::inputs` and `::output`.  Access these as `:flowing.core/name` 
etc., or use the convenience accessor functions `(step-name)`, `(inputs)` and
`(output-ref)`.

The map from `(link)` contain 
`::from` and `::to` showing the quoted source and destination expressions of 
the link, e.g. `(:inputB foo-step)`.

`(workflow)` creates a map of the steps, with keys being the keyword version
of the defined step name, e.g. `:step2`. The links are available under the key
`::links`. While the `(workflow)` call is not strictly needed to group or
execute the steps, it might be required in a future version of this library 
to be combined with a method like 
`(run-workflow)` ([issue #1](https://github.com/stain/flowing-clj/issues/1)).




## License

Copyright © 2015 Stian Soiland-Reyes

Distributed under the
[Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
See the file `LICENSE` for details.


## Contribute

This Clojure library is currently an experimental prototype; the
API and data structures might change at any time. To influence
this project, feel free to **contribute** by
raising [pull requests](https://github.com/stain/flowing-clj/pulls) or 
adding [issues](https://github.com/stain/flowing-clj/issues).

