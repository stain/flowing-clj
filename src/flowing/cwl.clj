(ns flowing.cwl
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io ]))

(defn parse-cwl [src]
  (let [wf (yaml/parse-string (slurp src))
        url (io/as-url src)]
      (with-meta wf { :src url })))
  ;; TODO: Something about identifiers?
