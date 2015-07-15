(ns flowing.cwl
  (:require [clj-yaml.core :as yaml]
            [clojure.walk :as walk]
            [clojure.java.io :as io]
            [flowing.core :as flowing]

            ))

(def parse-cwl)

(defn- import-wf [base id]
  (if (.contains id  "#")
    (throw (RuntimeException. (str "Local ids not yet supported: " id)))
    (parse-cwl (java.net.URL. base id))))

(defn- resolve-key [base key wf-part]
  (if (key wf-part)
    (assoc wf-part key (str (java.net.URL. base (key wf-part))))
    wf-part))


(defn- resolve-ids [base wf-part]
  (->> wf-part
    (resolve-key base :id)
    (resolve-key base :source)))


(defn- resolve-imports [base wf-part]
    (if (:import wf-part)
      ;'(import-wf base ~(:import wf-part))
      (import-wf base (:import wf-part))
      wf-part))

(defn- resolve-wf [base wf-part]
  (->> wf-part
      (resolve-imports base)
      (resolve-ids base)
    ))

(defn parse-cwl [src]
  (let [url (io/as-url src)]
    (with-meta
      (walk/postwalk (partial resolve-wf url)
        (yaml/parse-string (slurp src)))
      { :src url })))

(defn uuid []
  (str (java.util.UUID/randomUUID)))


(defn cwl-input->args [step]
  (vector (map #(symbol (:id %)) (:inputs step))))

(defn cwl-step->flowing [step#]

  (let [name (get step# :id (uuid))
        args# (cwl-input->args step#)]
        ; FIXME: Actually use args# as args below
  (flowing/step name [&args]
    ;; TODO: Run actual docker command
      (println "docker run something something" args#)
  )))

(defn compile-cwl [cwl]

  (let [wf (flowing/steps->map (map cwl-step->flowing (:steps cwl)))]
  ; ..
    wf
  ))



(defn- find-all-nested
;; by xsc (Yannick Scherer)
;; http://stackoverflow.com/a/28097404
  [m k]
  (->> (tree-seq map? vals m)
       (filter map?)
       (some k)))
