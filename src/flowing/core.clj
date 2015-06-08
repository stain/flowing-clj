(ns flowing.core)

(defmacro workflow
  "Define a workflow using a series of
   (defstep) and (link) calls"
  [& steps]
)

(defmacro defstep
  [name args & body]
    (let [input (zipmap args (repeatedly promise))]
      `(def ~name (future
        (let
          ~(vec (interleave args (map #(list 'deref '(input %)) args)))
          ~@body
        )))))
        ;'(name ~input#)))
