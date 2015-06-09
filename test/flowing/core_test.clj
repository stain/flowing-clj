(ns flowing.core-test
  (:require [clojure.test :refer :all]
            [flowing.core :refer :all]))

(deftest simple-workflow
  (testing "step"
    (let [step (step "hello" [a b c] (str "Hello, " a b c))]
      (is (= "hello" (step-name step)))
      (is (= [:a :b :c] (inputs step)))
      (is (not (realized? (:a step))))
      (is (not (realized? (:b step))))
      (is (not (realized? (:c step))))
      (is (future? (output-ref step)))
      (deliver (:a step) "Alice")
      (is (realized? (:a step)))
      (is (not (realized? (output-ref step))))
      (deliver (:b step) (future "Bob"))
      (link "Charlie" (:c step))
;      (deliver (:c step) "Charlie")
      (is (realized? (output-ref step)))
      (is (= "Hello, AliceBobCharlie" (wait-for-output step)))))

  (testing "defstep"
    (def hello)
    (defstep hello [a b c] (str "Hello, " a b c))
    (is (= "hello" (step-name hello))))

  (testing "workflow"
    (def hello)
    (let [wf (workflow
            (defstep hello [name] (str "Hello, " name))
            (link "Alice" (:name hello)))]
         (println wf)
         (is (= "hello" (step-name (:hello wf))))
        (is (= "Hello, Alice") (wait-for-output hello))
        (is (= {:hello "Hello, Alice" } (wait-for-workflow wf)))
    ))
)
