(ns flowing.cwl-test
  (:require [clojure.test :refer :all]
            [flowing.cwl :refer :all]
            [flowing.core :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io ]
            ))

(testing "parse-cwl revsort.cwl"
  (let [res (io/resource "example/revsort.cwl")
        wf (parse-cwl res)]
    ;(pprint wf)
    (is (= "Workflow" (:class wf)))
    (is (= res (:src (meta wf))))
    ; Check the :import worked
    (is (= "CommandLineTool" (get-in (first (:steps wf)) [:run :class])))
    ; Check URIs are absolute
;    (print (:id (first (:outputs wf))))
    (is (.startsWith (:id (first (:outputs wf))) "file:" ))
    (is (.endsWith (:id (first (:outputs wf))) "revsort.cwl#output"))
    ; and different filename in imported blocks
    (is (.endsWith (:id (first (get-in (first (:steps wf)) [:run :inputs]))) "revtool.cwl#input"))
    ; :source should also be expanded
    (is (.startsWith (:source (first (:outputs wf))) "file:" ))
    (is (.endsWith (:source (first (:outputs wf))) "revsort.cwl#sorted.output" ))
  ))


  (testing "parse-cwl nesting.cwl"
    (let [res (io/resource "example/nesting.cwl")
          nesting (parse-cwl res)
          wf (:run (first (:steps nesting)))]
      ;(pprint nesting)
      (is (= "Workflow" (:class nesting)))
      ; Check the :import worked
      (is (= "Workflow" (:class wf)))
      ; and nested imports
      (is (= "CommandLineTool" (get-in (first (:steps wf)) [:run :class])))
      ; Check URIs are absolute
      (is (.endsWith (:id (first (:outputs nesting))) "nesting.cwl#output"))
      (is (.endsWith (:id (first (:outputs wf))) "revsort.cwl#output"))
      (is (.endsWith (:id (first (get-in (first (:steps wf)) [:run :inputs]))) "revtool.cwl#input"))
    ))

(testing "compile-cwl revsort.cwl"
  (let [cwl (parse-cwl (io/resource "example/revsort.cwl"))
        wf (compile-cwl cwl)
        steps (workflow-steps wf)]
        (pprint wf)
        ;; TODO: Actually test and run wf

  ))
