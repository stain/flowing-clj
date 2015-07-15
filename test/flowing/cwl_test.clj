(ns flowing.cwl-test
  (:require [clojure.test :refer :all]
            [flowing.core :refer :all]
            [clojure.pprint :refer [pprint]]
            [flowing.cwl :refer :all]
            [clojure.java.io :as io ]
            ))

(testing "parse-cwl"
  (let [res (io/resource "example/revsort.cwl")
        wf (parse-cwl res)]
    (pprint wf)
    (is (= "Workflow" (:class wf)))
    ; Check the :import worked
    (is (= "CommandLineTool" (get-in (first (:steps wf)) [:run :class])))
    ; Check URIs are absolute
;    (print (:id (first (:outputs wf))))
    (is (.startsWith (:id (first (:outputs wf))) "file:" ))
    (is (.endsWith (:id (first (:outputs wf))) "revsort.cwl#output"))
    ; and different filename in imported blocks
    (is (.endsWith (:id (first (get-in (first (:steps wf)) [:run :inputs]))) "revtool.cwl#input"))
  ))
