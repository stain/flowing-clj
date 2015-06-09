(ns flowing.core-test
  (:require [clojure.test :refer :all]
            [flowing.core :refer :all]))

(deftest simple-workflow
  (testing "step"
    (let [step (step "hello" [a b c] (str "Hello, " a b c))]
      (is (= "hello" (:flowing.core/name step)))
      (is (= [:a :b :c] (:flowing.core/inputs step)))
      (is (not (realized? (:a step))))
      (is (not (realized? (:b step))))
      (is (not (realized? (:c step))))
      (is (future? (:flowing.core/output step)))))
  (testing "defstep"
    (def hello)
    (defstep hello [a b c] (str "Hello, " a b c))
    (is (= "hello" (:flowing.core/name hello))))

;  (testing "workflow"
;    (let [wf (workflow
;          (defstep hello [name] (str "Hello, " name))
;          (link "Alice" (:name hello)))]
;        (is (= "Hello, Alice") (deref (:hello wf))))))
)
