(ns flowing.core)

(defmacro workflow
  "Define a workflow using a series of
   (defstep) and (link) calls"
  [& steps]
)

(defmacro step
  [step-name args & body]
    `(let [keys# (map keyword '~args)
           input# (zipmap keys# (repeatedly promise))
           output# (future
             (apply (fn ~args ~@body) (map deref (map input# keys#))))]
          (merge input#
            { ::name ~step-name
              ::inputs keys#
              ::output output# })))

(defmacro defstep
  [step-name args & body]
  `(def ~step-name (step (name '~step-name) ~args ~body))
)

(defmacro defworkflow
  [wf-name & steps]
  `(def ~wf-name (workflow ~@steps)))
