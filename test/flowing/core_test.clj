(ns flowing.core-test
  (:require [clojure.test :refer :all]
            [flowing.core :refer :all]))

(deftest simple-workflow
  (testing "defstep"
    (let [step (defstep hello [name] (str "Hello, " name))]
      (is (future? (get step 'hello)))))
  (testing "workflow"
    (let [wf (workflow
          (defstep hello [name] (str "Hello, " name))
          (link "Alice" (:name hello)))]
        (is (= "Hello, Alice") (deref (:hello wf))))))
