(ns flowing.core-test
  (:require [clojure.test :refer :all]
            [flowing.core :refer :all]))

(deftest simple-workflow
  (testing "step"
    (let [step (step "hello" [a b c] (str "Hello, " a b c))]
      (is (= "hello" (step-name step)))
      (is (= [:a :b :c] (inputs step)))
      (is (ref? (:a step)))
      (is (ref? (:b step)))
      (is (ref? (:c step)))
      (is (future? (output-ref step)))
      (link "Alice" (:a step))
      (is (not (realized? (output-ref step))))
      (link "Bob" (:b step))
      (link "Charlie" (:c step))
      (is (= "Hello, AliceBobCharlie" (wait-for-output step)))
      (is (realized? (output-ref step)))))

  (testing "defstep"
    (def hello)
    (defstep hello [a b c] (str "Hello, " a b c))
    (is (= "hello" (step-name hello))))

  (testing "workflow"
    (def hello)
    (let [wf (workflow
            (defstep hello [name] (str "Hello, " name))
            (link "Alice" (:name hello)))]
         ;(println wf)
         (is (= "hello" (step-name (:hello wf))))
        (is (= "Hello, Alice") (wait-for-output hello))
        (is (= {:hello "Hello, Alice" } (wait-for-workflow wf)))
    ))

  (testing "example workflow"
    (println "Example workflow")
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

    ; TODO: Explicit (run-workflow)?
    ; TODO: Ability to run a workflow definition more than once

    ; Outputs can be retrieved from any of the steps:
    (is (= "GATTAGCAT") (wait-for-output get-sequence))
    ; (wait-for-output) block until the step
    ; has received all its upstream inputs
    ; and finished execution
    (is (= ["tiger" "lion"] (wait-for-output similar)))

    ; Or wait for the whole workflow to complete and get
    ; a handy map of the outputs:
    (let [results (wait-for-workflow example-wf)]
      (is (= #{:get-sequence :alignment :pathways :similar} (set (keys results))))
      (is (= "GATTAGCAT" (:get-sequence results)))
      (is (= :tree (:fireman (:pathways results)))))

  )

)
