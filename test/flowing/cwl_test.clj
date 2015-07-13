(ns flowing.cwl-test
  (:require [clojure.test :refer :all]
            [flowing.core :refer :all]
            [flowing.cwl :refer :all]
            [clojure.java.io :as io ]
            ))

(testing "parse-cwl"
  (let [res (io/resource "example/revsort.cwl")
        wf (parse-cwl res)]
    (println wf)
    (is (= "Workflow" (:class wf)))
    ; Check the :import worked
    (is (= "CommandLineTool" (get-in (first (:steps wf)) [:run :class])))
    
  ))
