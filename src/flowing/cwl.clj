(ns flowing.cwl
  (:require [clj-yaml.core :as yaml]
            [clojure.walk :as walk]
            [clojure.java.io :as io ]))

(def parse-cwl)

(defn import-wf [base id]
  (if (.contains id  "#")
    (throw (RuntimeException. (str "Local ids not yet supported: " id)))
    (parse-cwl (java.net.URL. base id))))


(defn resolve-imports [base wf]
    (if (:import wf)
      ;'(import-wf base ~(:import wf))
      (import-wf base (:import wf))
      wf))


(defn parse-cwl [src]
  (let [url (io/as-url src)]
    (with-meta
      (walk/postwalk (partial resolve-imports url)
        (yaml/parse-string (slurp src)))
      { :src url })))
  ;; TODO: Something about identifiers?


(defn find-all-nested
;; by xsc (Yannick Scherer)
;; http://stackoverflow.com/a/28097404
  [m k]
  (->> (tree-seq map? vals m)
       (filter map?)
       (some k)))
