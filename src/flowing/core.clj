(ns flowing.core)

(defn- named-map [vals]
  (dissoc (zipmap (map keyword (map ::name vals)) vals) nil))

(defn workflow
  "Define a workflow using a series of
   (defstep) and (link) calls"
  [& steps]
  (assoc (named-map steps)
    :links (filter ::from steps)))

(defn ref? [val] (instance? clojure.lang.IDeref val))

(defn deref-deep [ref]
  (loop [ref ref]
    (if (not (ref? ref)) ref
      (recur (deref ref)))))


(defmacro step
  [step-name args & body]
    `(let [keys# (map keyword '~args)
           input# (zipmap keys# (repeatedly promise))
           output# (future
             (apply (fn ~args ~@body) (map deref-deep (map input# keys#))))]
          (merge input#
            { ::name ~step-name
              ::inputs keys#
              ::body '~body
              ::output output# })))

(defmacro defstep
  [step-name args & body]
  `(let [step# (step (name '~step-name) ~args ~@body)]
    (def ~step-name step#)
    step#))

(defmacro link [from to]
  `(do
    (deliver ~to (get ~from ::output (delay ~from)))
    {::from '~from ::to '~to }))


(defmacro defworkflow
  [wf-name & steps]
  `(def ~wf-name (workflow ~@steps)))

(defn step-name [step]
  (::name step))
(defn inputs [step]
  (::inputs step))
(defn output-ref [step]
  (::output step))

(defn wait-for-output [step]
  (deref (output-ref step)))

(defn workflow-steps [wf]
  (filter ::output (vals wf)))

(defn- step-names [steps]
  (map keyword (map ::name steps)))

(defn wait-for-workflow [workflow]
  (let [steps (workflow-steps workflow)]
    (zipmap (step-names steps) (map wait-for-output steps))))
